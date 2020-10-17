package pl.vrajani.service;

import pl.vrajani.Application;
import pl.vrajani.model.*;
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

    public void checkAllCrypto() throws IOException {
        System.out.println("Initiating the check::::");

        if(!TimeUtil.isDownTime()) {
            DataConfig dataConfig = daoService.getMainConfig();
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
                daoService.updateMainConfig(dataConfig);
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
        try {
            String ccId = currencyStatus.getCcId();
            System.out.println("Working with Crypto :: " + ccId);
            if (pendingOrdersBySymbol.containsKey(ccId)) {
                    return processPendingOrder(currencyStatus);
            } else if(currencyStatus.isShouldBuy()) {
                    System.out.println("Checking Buy Order:: " + ccId);
                    Optional<String> orderId = Optional.ofNullable(actionService.executeBuyIfPriceDown(currencyStatus));
                    orderId.ifPresent(s -> pendingOrdersBySymbol.put(ccId, s));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<CryptoCurrencyStatus> processPendingOrder(CryptoCurrencyStatus currencyStatus) throws InterruptedException, IOException {
        String symbol = currencyStatus.getSymbol();
        String ccId = currencyStatus.getCcId();
        String previousOrderId = pendingOrdersBySymbol.get(ccId);
        CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
        if (CryptoOrderState.getState(cryptoOrderStatusResponse.getState()).equals(CryptoOrderState.FILLED)) {
            return Optional.of(processFilledOrder(currencyStatus, cryptoOrderStatusResponse, true));
        } else if(CryptoOrderState.getState(cryptoOrderStatusResponse.getState()).equals(CryptoOrderState.CANCELED) ||
                CryptoOrderState.getState(cryptoOrderStatusResponse.getState()).equals(CryptoOrderState.REJECTED)) {
            System.out.println("The order was cancelled: " + ccId + " with order Id: " + previousOrderId);
            if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("sell") &&
                    !isPendingOrderAlreadyReduced(cryptoOrderStatusResponse, currencyStatus)) {
                double percent = 100.0 + (currencyStatus.getProfitPercent() / 3);
                double sellPrice = MathUtil.getAmount(currencyStatus.getLastBuyPrice(), percent);
                System.out.println("Resetting the sell order with lesser profit. symbol - " + ccId + " with limit price " + sellPrice);
                setSellOrder(currencyStatus, cryptoOrderStatusResponse, sellPrice);
            } else if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("sell") &&
                    isPendingOrderAlreadyReduced(cryptoOrderStatusResponse, currencyStatus)) {
                StopLossConfigBase stopLossConfigBase = daoService.getStopLossConfig();
                StopLossConfig stopLossConfig = new StopLossConfig();
                stopLossConfig.setTranId("");
                stopLossConfig.setSymbol(symbol.toUpperCase());
                stopLossConfig.setQuantity(Double.parseDouble(cryptoOrderStatusResponse.getQuantity()));
                stopLossConfig.setBuyPrice(Double.parseDouble(cryptoOrderStatusResponse.getPrice()));
                stopLossConfigBase.getStopLossConfigs().add(stopLossConfig);
                daoService.updateStoplossConfig(stopLossConfigBase);
            }
            pendingOrdersBySymbol.remove(ccId);
        } else if (TimeUtil.isPendingOrderForLong(cryptoOrderStatusResponse.getCreatedAt(), currencyStatus.getWaitInMinutes())) {
            if(cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy") ||
                    !isPendingOrderAlreadyReduced(cryptoOrderStatusResponse, currencyStatus) ||
            MathUtil.getPercentAmount(Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice()),
                    Double.valueOf(cryptoOrderStatusResponse.getPrice())) <= 90.0) {
                System.out.println("Cancelling the order as it has been pending for long or dropped by more than 10% since last buy. symbol - "
                        + ccId + " last "+ cryptoOrderStatusResponse.getSide() +" order price - " + cryptoOrderStatusResponse.getPrice());
                apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
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

    public CryptoCurrencyStatus processFilledOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, boolean shouldExecute) throws InterruptedException {
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

    public void setSellOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, double price) throws InterruptedException {
        CryptoOrderResponse cryptoSellOrderResponse = sellWithRetry(currencyStatus, cryptoOrderStatusResponse, price);
        System.out.println("Setting sell order:: " + currencyStatus.getCcId() + " with price: " + cryptoSellOrderResponse.getPrice());
        pendingOrdersBySymbol.put(currencyStatus.getCcId(), cryptoSellOrderResponse.getId());
    }

    public CryptoOrderResponse sellWithRetry(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse, double price) throws InterruptedException {
        int retryCounter = 0;
        while(retryCounter < 3) {
            try {
                return apiService.sellCrypto(currencyStatus.getSymbol(), cryptoOrderStatusResponse.getQuantity(),
                        String.valueOf(price));
            } catch (Exception ex) {
                retryCounter++;
                ex.printStackTrace();
                Thread.sleep(5000);
            }
        }
        throw new RuntimeException("Failed to set sell order after all retries");
    }
}
