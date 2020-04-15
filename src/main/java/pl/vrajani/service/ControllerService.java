package pl.vrajani.service;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.model.DataConfig;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;
import pl.vrajani.utility.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ControllerService {
    private APIService apiService;
    private DaoService daoService;
    private HashMap<String, String> pendingOrdersBySymbol;
    private List<CryptoCurrencyStatus> updatedStatus;
    private ActionService actionService;

    public ControllerService(DaoService daoService) {
        this.daoService = daoService;

        this.pendingOrdersBySymbol = new HashMap<>();
        this.updatedStatus = new ArrayList<>();
    }

    public void checkAllCrypto() throws IOException {
        System.out.println("Initiating the check::::");

        if(!TimeUtil.isDownTime()) {
            DataConfig dataConfig = daoService.getDataConfig();
            dataConfig.getPendingOrders().stream()
                .map(pendingOrder -> pendingOrder.split(","))
                .forEach(split -> {
                    System.out.println("Pending order to be checked: " + split[0] + ":" + split[1]);
                    pendingOrdersBySymbol.put(split[0], split[1]);
                });

            this.apiService = apiService(dataConfig.getToken());
            String acquiredToken = apiService.acquireToken();
            this.actionService = new ActionService(apiService);
            boolean didUpdateCurrencyStatus = false;

            HashMap<String, String> pendingOrdersBySymbolBefore = new HashMap<>(pendingOrdersBySymbol);
            for (CryptoCurrencyStatus currencyStatus : dataConfig.getCryptoCurrencyStatuses()) {
                if (currencyStatus.isPower()) {
                    if(currencyStatus.getStopCounter() > 0) {
                        currencyStatus.decStopCounter();
                        didUpdateCurrencyStatus = true;
                    }
                    Optional<CryptoCurrencyStatus> updatedCurrencyStatus = processCrypto(currencyStatus);
                    if(updatedCurrencyStatus.isPresent()) {
                        didUpdateCurrencyStatus = true;
                        updatedStatus.add(updatedCurrencyStatus.get());
                    } else {
                        updatedStatus.add(currencyStatus);
                    }
                } else {
                    updatedStatus.add(currencyStatus);
                }
            }

            if(didUpdateCurrencyStatus || updatedToken(acquiredToken, dataConfig.getToken()) || !pendingOrdersBySymbolBefore.equals(pendingOrdersBySymbol)){
                dataConfig.setCryptoCurrencyStatuses(updatedStatus);
                dataConfig.clearPendingOrder();
                this.pendingOrdersBySymbol.keySet()
                        .forEach(symbol -> dataConfig.addPendingOrder(symbol, this.pendingOrdersBySymbol.get(symbol)));
                dataConfig.setToken(acquiredToken);
                daoService.updateConfig(dataConfig);
            }
        } else {
            System.out.println("It is DownTime. Waiting...");
        }
    }

    private Optional<CryptoCurrencyStatus> processCrypto(CryptoCurrencyStatus currencyStatus) {
        String symbol = currencyStatus.getSymbol();
        System.out.println("Working with Crypto :: " + symbol);
        if (pendingOrdersBySymbol.containsKey(symbol)) {
            String previousOrderId = pendingOrdersBySymbol.get(symbol);
            CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
            if ("filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                return Optional.of(processFilledOrder(currencyStatus, cryptoOrderStatusResponse, true));
            } else if("Canceled".equalsIgnoreCase(cryptoOrderStatusResponse.getState()) ||
                    "Rejected".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                System.out.println("The order was cancelled so removing from pending order: " + symbol + " with order Id: " + previousOrderId);
                pendingOrdersBySymbol.remove(symbol);
            } else {
                System.out.println("Skipping crypto as there is a pending order: " + symbol +
                        " with order Id: " + previousOrderId + " at price: " + cryptoOrderStatusResponse.getPrice());
                if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("sell")){
                    Optional<String> stopLossOrderId = Optional.ofNullable(actionService.executeStopLossOrderIfPriceDown(currencyStatus, cryptoOrderStatusResponse));
                    stopLossOrderId.ifPresent(s -> pendingOrdersBySymbol.put(symbol, s));
                }
            }
        } else {
            if(currencyStatus.isShouldBuy()) {
                System.out.println("Checking Buy Order:: " + symbol);
                Optional<String> orderId = Optional.ofNullable(actionService.executeBuyIfPriceDown(currencyStatus));
                orderId.ifPresent(s -> pendingOrdersBySymbol.put(symbol, s));
            }
        }
        return Optional.empty();
    }

    public CryptoCurrencyStatus processFilledOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, boolean shouldExecute) {
        double lastOrderPrice = Double.parseDouble(cryptoOrderStatusResponse.getPrice());
        String symbol = currencyStatus.getSymbol();
        System.out.println("Processing Filled " + cryptoOrderStatusResponse.getSide() + " order:: " + symbol);
        if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")){
            currencyStatus.setShouldBuy(false);
            currencyStatus.setLastBuyPrice(lastOrderPrice);
            currencyStatus.setQuantity(Double.parseDouble(cryptoOrderStatusResponse.getQuantity()));
            if(shouldExecute) {
                setSellOrder(currencyStatus, cryptoOrderStatusResponse, symbol);
            }
        } else {
            currencyStatus.setShouldBuy(true);
            currencyStatus.setLastSellPrice(lastOrderPrice);
            if(currencyStatus.getLastBuyPrice() < lastOrderPrice) {
                currencyStatus.incRegularSell();
                currencyStatus.setStopCounter(0);
            } else {
                currencyStatus.incStopLossSell();
                currencyStatus.setStopCounter(60);
            }
            pendingOrdersBySymbol.remove(symbol);

            currencyStatus.addProfit((lastOrderPrice - currencyStatus.getLastBuyPrice()) * currencyStatus.getQuantity());
        }
        return currencyStatus;
    }

    private void setSellOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, String symbol) {
        CryptoOrderResponse cryptoSellOrderResponse = apiService.sellCrypto(symbol, cryptoOrderStatusResponse.getQuantity(),
                String.valueOf(MathUtil.getAmount(
                        Double.parseDouble(cryptoOrderStatusResponse.getPrice()),
                        Double.parseDouble("100") + currencyStatus.getProfitPercent())));
        System.out.println("Setting sell order:: " + symbol + " with price: " + cryptoSellOrderResponse.getPrice());
        pendingOrdersBySymbol.put(symbol, cryptoSellOrderResponse.getId());
    }

    private boolean updatedToken(String acquiredToken, String token) {
        return acquiredToken != null && !acquiredToken.equals(token);
    }

    public static APIService apiService(String token) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("username", System.getenv("username"));
        properties.put("password", System.getenv("password"));
        properties.put("grant_type", "password");
        properties.put("client_id", System.getenv("client_id"));
        properties.put("account_id", System.getenv("account_id"));
        properties.put("accountId", System.getenv("accountId"));
        properties.put("token", token);

        return new APIService(properties);
    }
}
