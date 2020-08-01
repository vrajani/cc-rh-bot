package pl.vrajani.service;

import pl.vrajani.Application;
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
    private final DaoService daoService;
    private final HashMap<String, String> pendingOrdersBySymbol;
    private final List<CryptoCurrencyStatus> updatedStatus;
    private APIService apiService;
    private ActionService actionService;

    public ControllerService(DaoService daoService) {
        this.daoService = daoService;

        this.pendingOrdersBySymbol = new HashMap<>();
        this.updatedStatus = new ArrayList<>();
    }

    private boolean shouldProcess() {
        return !TimeUtil.isDownTime() && !TimeUtil.isBadTimeOfTheWeek();
    }

    public void checkAllCrypto() throws IOException {
        System.out.println("Initiating the check::::");

        if(shouldProcess()) {
            DataConfig dataConfig = daoService.getDataConfig();
            String acquiredToken = initAndAcquireToken(dataConfig);
            boolean didUpdateCurrencyStatus = false;
            HashMap<String, String> pendingOrdersBySymbolBefore = new HashMap<>(pendingOrdersBySymbol);

            for (CryptoCurrencyStatus currencyStatus : dataConfig.getCryptoCurrencyStatuses()) {
                if (currencyStatus.isPower()) {
                    if (isValidState(currencyStatus, pendingOrdersBySymbolBefore.containsKey(currencyStatus.getCcId()))) {
                        Optional<CryptoCurrencyStatus> updatedCurrencyStatus = processCrypto(currencyStatus);
                        if (updatedCurrencyStatus.isPresent()) {
                            didUpdateCurrencyStatus = true;
                            updatedStatus.add(updatedCurrencyStatus.get());
                        } else {
                            updatedStatus.add(currencyStatus);
                        }
                    } else {
                        System.out.println("Currency status is not in a valid state: " + currencyStatus.toString());
                        currencyStatus.setShouldBuy(true);
                        updatedStatus.add(currencyStatus);
                        didUpdateCurrencyStatus = true;
                    }
                } else {
                    updatedStatus.add(currencyStatus);
                }
            }

            if (isConfigUpdated(acquiredToken, didUpdateCurrencyStatus, pendingOrdersBySymbolBefore, dataConfig.getToken())) {
                dataConfig.setCryptoCurrencyStatuses(updatedStatus);
                dataConfig.clearPendingOrder();
                this.pendingOrdersBySymbol.keySet()
                        .forEach(ccId -> dataConfig.addPendingOrder(ccId, this.pendingOrdersBySymbol.get(ccId)));
                dataConfig.setToken(acquiredToken);
                daoService.updateConfig(dataConfig);
            }
        } else {
            System.out.println("It is DownTime. Waiting...");
        }
    }

    public boolean isConfigUpdated(String acquiredToken, boolean didUpdateCurrencyStatus, HashMap<String, String> pendingOrdersBySymbolBefore, String newToken) {
        boolean tokenUpdated = acquiredToken != null && !acquiredToken.equals(newToken);
        return didUpdateCurrencyStatus || tokenUpdated || !pendingOrdersBySymbolBefore.equals(pendingOrdersBySymbol);
    }

    public String initAndAcquireToken(DataConfig dataConfig) {
        dataConfig.getPendingOrders().stream()
            .map(pendingOrder -> pendingOrder.split(","))
            .forEach(split -> {
                System.out.println("Pending order to be checked: " + split[0] + ":" + split[1]);
                pendingOrdersBySymbol.put(split[0], split[1]);
            });

        this.apiService = Application.getApiService(dataConfig.getToken());
        this.actionService = new ActionService(apiService);
        return apiService.acquireToken();
    }

    private boolean isValidState(CryptoCurrencyStatus currencyStatus, boolean hasPendingOrder) {
        return hasPendingOrder || currencyStatus.isShouldBuy();
    }

    private Optional<CryptoCurrencyStatus> processCrypto(CryptoCurrencyStatus currencyStatus) {
        String ccId = currencyStatus.getCcId();
        System.out.println("Working with Crypto :: " + ccId);
        if (pendingOrdersBySymbol.containsKey(ccId)) {
            return processPendingOrder(currencyStatus);
        } else if(currencyStatus.isShouldBuy()) {
                System.out.println("Checking Buy Order:: " + ccId);
                Optional<String> orderId = Optional.ofNullable(actionService.executeBuyIfPriceDown(currencyStatus));
                orderId.ifPresent(s -> pendingOrdersBySymbol.put(ccId, s));
        }
        return Optional.empty();
    }

    private Optional<CryptoCurrencyStatus> processPendingOrder(CryptoCurrencyStatus currencyStatus) {
        String symbol = currencyStatus.getSymbol();
        String ccId = currencyStatus.getCcId();
        String previousOrderId = pendingOrdersBySymbol.get(ccId);
        CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
        if ("filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
            return Optional.of(processFilledOrder(currencyStatus, cryptoOrderStatusResponse, true));
        } else if("Canceled".equalsIgnoreCase(cryptoOrderStatusResponse.getState()) ||
                "Rejected".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
            System.out.println("The order was cancelled so removing from pending order: " + ccId + " with order Id: " + previousOrderId);
            pendingOrdersBySymbol.remove(ccId);
        } else if (TimeUtil.isPendingOrderForLong(cryptoOrderStatusResponse.getCreatedAt(), currencyStatus.getWaitInMinutes())) {

            if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")) {
                System.out.println("Cancelling buy order as it was pending for a long time. Symbol " + ccId + " at Price: " + cryptoOrderStatusResponse.getPrice());
                apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
            } else if(!isPendingOrderAlreadyReduced(cryptoOrderStatusResponse, currencyStatus)) {
                double percent = 100.0 + (currencyStatus.getProfitPercent() / 2);
                double sellPrice = MathUtil.getAmount(currencyStatus.getLastBuyPrice(), percent);
                System.out.println("Selling as the order has been pending for long. symbol - " + ccId + " with limit price " + sellPrice);
                apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
                setSellOrder(currencyStatus, cryptoOrderStatusResponse, sellPrice);
            } else {
                System.out.println("Skipping crypto as there is a pending order: " + ccId +
                        " with order Id: " + previousOrderId + " at price: " + cryptoOrderStatusResponse.getPrice());
            }
        } else {
            System.out.println("Skipping crypto as there is a pending order: " + ccId +
                    " with order Id: " + previousOrderId + " at price: " + cryptoOrderStatusResponse.getPrice());
        }
        return Optional.empty();
    }

    private boolean isPendingOrderAlreadyReduced(CryptoOrderStatusResponse cryptoOrderStatusResponse, CryptoCurrencyStatus currencyStatus) {
        return cryptoOrderStatusResponse.getSide().equalsIgnoreCase("sell") &&
                Double.parseDouble(cryptoOrderStatusResponse.getPrice()) <
                        MathUtil.getAmount(currencyStatus.getLastBuyPrice(), 100 + currencyStatus.getProfitPercent());
    }

    public CryptoCurrencyStatus processFilledOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, boolean shouldExecute) {
        double lastOrderPrice = Double.parseDouble(cryptoOrderStatusResponse.getPrice());
        String ccId = currencyStatus.getCcId();
        System.out.println("Processing Filled " + cryptoOrderStatusResponse.getSide() + " order:: " + ccId);
        if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")){
            currencyStatus.setShouldBuy(false);
            currencyStatus.setLastBuyPrice(lastOrderPrice);
            currencyStatus.setQuantity(Double.parseDouble(cryptoOrderStatusResponse.getQuantity()));
            if(shouldExecute) {
                double amount = MathUtil.getAmount(Double.parseDouble(cryptoOrderStatusResponse.getPrice()),
                        100 + currencyStatus.getProfitPercent());
                setSellOrder(currencyStatus, cryptoOrderStatusResponse, amount);
            }
        } else {
            currencyStatus.setShouldBuy(true);
            currencyStatus.addProfit((lastOrderPrice - currencyStatus.getLastBuyPrice()) * currencyStatus.getQuantity());
            currencyStatus.incRegularSell();

            pendingOrdersBySymbol.remove(ccId);
        }
        return currencyStatus;
    }

    public void setSellOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, double price) {
        CryptoOrderResponse cryptoSellOrderResponse = apiService.sellCrypto(currencyStatus.getSymbol(), cryptoOrderStatusResponse.getQuantity(),
                String.valueOf(price));
        System.out.println("Setting sell order:: " + currencyStatus.getCcId() + " with price: " + cryptoSellOrderResponse.getPrice());
        pendingOrdersBySymbol.put(currencyStatus.getCcId(), cryptoSellOrderResponse.getId());
    }
}
