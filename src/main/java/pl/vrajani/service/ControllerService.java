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
    private OrderService orderService;

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

    private boolean isConfigUpdated(String acquiredToken, boolean didUpdateCurrencyStatus, HashMap<String, String> pendingOrdersBySymbolBefore, String newToken) {
        boolean tokenUpdated = acquiredToken != null && !acquiredToken.equals(newToken);
        return didUpdateCurrencyStatus || tokenUpdated || !pendingOrdersBySymbolBefore.equals(pendingOrdersBySymbol);
    }

    private String initAndAcquireToken(DataConfig dataConfig) {
        dataConfig.getPendingOrders().stream()
            .map(pendingOrder -> pendingOrder.split(","))
            .forEach(split -> {
                System.out.println("Pending order to be checked: " + split[0] + ":" + split[1]);
                pendingOrdersBySymbol.put(split[0], split[1]);
            });

        this.apiService = Application.getApiService(dataConfig.getToken());
        this.orderService = new OrderService(apiService, daoService);
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
                Optional<CryptoOrderResponse> mayBeCryptoOrderResponse = executeBuyIfPriceDown(currencyStatus);
                mayBeCryptoOrderResponse.ifPresent(cryptoOrderResponse -> pendingOrdersBySymbol.put(ccId, cryptoOrderResponse.getId()));
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
        if (OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.FILLED)) {
            return Optional.of(processFilledOrder(currencyStatus, cryptoOrderStatusResponse, true));
        } else if(OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.CANCELED) ||
                OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.REJECTED)) {
            System.out.println("The order was cancelled: " + ccId + " with order Id: " + previousOrderId);
            if (!cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy")) {
                System.out.println("Order cancelled as the price is down by over 10% OR pending for long - " + symbol);
                addToStopLossConfig(symbol, cryptoOrderStatusResponse.getQuantity(), currencyStatus.getLastBuyPrice());
            }
            pendingOrdersBySymbol.remove(ccId);
        } else if (shouldCancelPendingOrder(symbol, cryptoOrderStatusResponse)) {
            System.out.println("Cancelling the order as it has been pending for long or dropped by more than 10% since last buy. symbol - "
                    + ccId + " last "+ cryptoOrderStatusResponse.getSide() +" order price - " + cryptoOrderStatusResponse.getPrice());
            apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
        } else {
            System.out.println("Skipping crypto as there is a pending order: " + ccId +
                    " with order Id: " + previousOrderId + " at price: " + cryptoOrderStatusResponse.getPrice());
        }
        return Optional.empty();
    }

    private boolean shouldCancelPendingOrder(String symbol, CryptoOrderStatusResponse cryptoOrderStatusResponse) {
        boolean isBuy = cryptoOrderStatusResponse.getSide().equalsIgnoreCase("buy");
        return TimeUtil.isPendingOrderForLong(cryptoOrderStatusResponse.getCreatedAt(), isBuy) ||
                (!isBuy &&
                    MathUtil.getPercentAmount(Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice()),
                        Double.valueOf(cryptoOrderStatusResponse.getPrice())) <= 93.0);
    }

    private void addToStopLossConfig(String symbol, String quantity, double price) throws IOException {
        StopLossConfigBase stopLossConfigBase = daoService.getStopLossConfig();
        StopLossConfig stopLossConfig = new StopLossConfig();
        stopLossConfig.setTranId("");
        stopLossConfig.setSymbol(symbol.toUpperCase());
        stopLossConfig.setQuantity(Double.parseDouble(quantity));
        stopLossConfig.setBuyPrice(price);
        stopLossConfigBase.getStopLossConfigs().add(stopLossConfig);
        daoService.updateStoplossConfig(stopLossConfigBase);
    }

    public CryptoCurrencyStatus processFilledOrder(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse filledOrderStatus, boolean shouldExecute) throws InterruptedException {
        double lastOrderPrice = Double.parseDouble(filledOrderStatus.getPrice());
        String ccId = currencyStatus.getCcId();
        System.out.println("Processing Filled " + filledOrderStatus.getSide() + " order:: " + ccId);
        if(filledOrderStatus.getSide().equalsIgnoreCase("buy")){
            currencyStatus.setShouldBuy(false);
            currencyStatus.setLastBuyPrice(lastOrderPrice);
            currencyStatus.setQuantity(Double.parseDouble(filledOrderStatus.getQuantity()));
            if(shouldExecute) {
                double amount = MathUtil.getAmount(Double.parseDouble(filledOrderStatus.getPrice()),
                        100 + currencyStatus.getProfitPercent());
                CryptoOrderResponse cryptoOrderResponse = orderService.setSellOrder(currencyStatus, filledOrderStatus, amount);
                pendingOrdersBySymbol.put(currencyStatus.getCcId(), cryptoOrderResponse.getId());
            }
        } else {
            currencyStatus.setShouldBuy(true);
            currencyStatus.addProfit((lastOrderPrice - currencyStatus.getLastBuyPrice()) * currencyStatus.getQuantity());
            currencyStatus.incRegularSell();

            pendingOrdersBySymbol.remove(ccId);
        }
        return currencyStatus;
    }

    private Optional<CryptoOrderResponse> executeBuyIfPriceDown(CryptoCurrencyStatus cryptoCurrencyStatus) throws IOException, InterruptedException {
        String symbol = cryptoCurrencyStatus.getSymbol();
        CryptoHistPrice cryptoDayData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "5minute");
        double lastPrice = Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice());

        List<DataPoint> dataPoints = cryptoDayData.getDataPoints();
        int duration = Integer.parseInt(System.getenv("duration"));
        final List<DataPoint> subDataPoints = dataPoints.subList(dataPoints.size() - duration, dataPoints.size());
        return Optional.ofNullable(orderService.executeBuy(cryptoCurrencyStatus, subDataPoints, lastPrice, true));
    }
}
