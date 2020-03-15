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
                    pendingOrdersBySymbol.put(split[0], split[1]);
                    pendingOrdersBySymbol.put(split[0], split[1]);
                    System.out.println("Pending order to be checked: " + split[0] + ":" + split[1]);
                });

            this.apiService = apiService(dataConfig.getToken());
            String acquiredToken = apiService.acquireToken();
            this.actionService = new ActionService(apiService);
            boolean updatedPendingOrders = false;

            for (CryptoCurrencyStatus currencyStatus : dataConfig.getCryptoCurrencyStatuses()) {
                if (currencyStatus.isPower()) {
                    updatedPendingOrders = processCrypto(currencyStatus);
                } else {
                    updatedStatus.add(currencyStatus);
                }
            }

            if(updatedPendingOrders || updatedToken(acquiredToken, dataConfig.getToken())){
                dataConfig.setCryptoCurrencyStatuses(updatedStatus);
                dataConfig.clearPendingOrder();
                pendingOrdersBySymbol.keySet()
                        .forEach(symbol -> dataConfig.addPendingOrder(symbol, pendingOrdersBySymbol.get(symbol)));
                dataConfig.setToken(acquiredToken);
                daoService.updateConfig(dataConfig);
            }
        } else {
            System.out.println("It is DownTime. Waiting...");
        }
    }

    private boolean processCrypto(CryptoCurrencyStatus currencyStatus) {
        String symbol = currencyStatus.getSymbol();
        System.out.println("Working with Crypto :: " + symbol);
        boolean updatedPendingOrders = false;
        if (pendingOrdersBySymbol.containsKey(symbol)) {
            String previousOrderId = pendingOrdersBySymbol.get(symbol);
            CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
            if ("filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                updatedPendingOrders = true;
                pendingOrdersBySymbol.remove(symbol);
                getUpdatedCurrencyStatus(currencyStatus, cryptoOrderStatusResponse);
                processFilledOrder(currencyStatus, cryptoOrderStatusResponse);
            } else if("Canceled".equalsIgnoreCase(cryptoOrderStatusResponse.getState()) || "Rejected".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                System.out.println("The order was cancelled so removing from pending order: " + symbol + " with order Id: " + previousOrderId);
                pendingOrdersBySymbol.remove(symbol);
                updatedPendingOrders = true;
            } else {
                if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("sell")){
                    Optional<String> stopLossOrderId = actionService.executeStopLossOrderIfPriceDown(currencyStatus, cryptoOrderStatusResponse);
                    if(stopLossOrderId.isPresent()) {
                        pendingOrdersBySymbol.put(symbol, stopLossOrderId.get());
                        updatedPendingOrders = true;
                    }
                }
                System.out.println("Skipping crypto as there is a pending order: " + symbol +
                        " with order Id: " + previousOrderId + " at price: " + cryptoOrderStatusResponse.getPrice());
            }
        } else {
            if(currencyStatus.isShouldBuy()) {
                Optional<String> orderId = actionService.executeBuyIfPriceDown(currencyStatus);
                if(orderId.isPresent()) {
                    pendingOrdersBySymbol.put(symbol, orderId.get());
                    updatedPendingOrders = true;
                }
            }

            if(currencyStatus.getStopCounter() > 0) {
                currencyStatus.decStopCounter();
                updatedPendingOrders = true;
            }
        }
        updatedStatus.add(currencyStatus);
        return updatedPendingOrders;
    }

    private void processFilledOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse) {
        if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")) {
            String symbol = currencyStatus.getSymbol();
            CryptoOrderResponse cryptoOrderResponse = apiService.sellCrypto(symbol, cryptoOrderStatusResponse.getQuantity(),
                    String.valueOf(MathUtil.getAmount(
                            Double.parseDouble(cryptoOrderStatusResponse.getPrice()),
                            Double.parseDouble("100") + currencyStatus.getProfitPercent())));
            System.out.println("Setting sell order:: " + symbol + " with price: " + cryptoOrderResponse.getPrice());
            pendingOrdersBySymbol.put(symbol, cryptoOrderResponse.getId());
        }
    }

    private void getUpdatedCurrencyStatus(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse) {
        double lastOrderPrice = Double.parseDouble(cryptoOrderStatusResponse.getPrice());
        if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")){
            currencyStatus.setShouldBuy(false);
            currencyStatus.setLastBuyPrice(lastOrderPrice);
            currencyStatus.setQuantity(Double.parseDouble(cryptoOrderStatusResponse.getQuantity()));
        } else {
            currencyStatus.setShouldBuy(true);
            currencyStatus.setLastSellPrice(lastOrderPrice);
            if(currencyStatus.getLastBuyPrice() < lastOrderPrice) {
                currencyStatus.incRegularSell();
                currencyStatus.setStopCounter(0);
            } else {
                currencyStatus.incStopLossSell();
                currencyStatus.setStopCounter(120);
            }

            currencyStatus.addProfit((lastOrderPrice - currencyStatus.getLastBuyPrice()) * currencyStatus.getQuantity());
        }
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
