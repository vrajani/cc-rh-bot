package pl.vrajani.service;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.model.DataConfig;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerService {
    private APIService apiService;
    private ActionService actionService;
    private DaoService daoService;
    private HashMap<String, String> pendingOrdersBySymbol;
    private List<CryptoCurrencyStatus> updatedStatus;

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
                    System.out.println("Pending order to be checked: " + split[0] + ":" + split[1]);
                });

            this.apiService = apiService(dataConfig.getToken());
            String acquiredToken = apiService.acquireToken();
            this.actionService = new ActionService(apiService);
            boolean updatedPendingOrders = false;

            for (CryptoCurrencyStatus currencyStatus : dataConfig.getCryptoCurrencyStatuses()) {
                String symbol = currencyStatus.getSymbol();
                if (currencyStatus.isPower()) {
                    if (pendingOrdersBySymbol.containsKey(symbol)) {
                        String previousOrderId = pendingOrdersBySymbol.get(symbol);
                        CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
                        if ("filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                            dataConfig.removePendingOrder(symbol, pendingOrdersBySymbol.get(symbol));
                            dataConfig.removePendingOrder(symbol, pendingOrdersBySymbol.get(symbol));
                            updatedPendingOrders = true;
                            updatedStatus.add(getUpdatedCurrencyStatus(currencyStatus, cryptoOrderStatusResponse));
                        } else {
                            System.out.println("Skipping crypto as there is a pending order: " + symbol + " with order Id: " + previousOrderId);
                        }
                    } else {
                        if(processCrypto(currencyStatus)) {
                            dataConfig.addPendingOrder(symbol, pendingOrdersBySymbol.get(symbol));
                            updatedPendingOrders = true;
                        }
                    }
                }
            }

            if(!updatedStatus.isEmpty() || updatedPendingOrders || updatedToken(acquiredToken, dataConfig.getToken())){
                if(!updatedStatus.isEmpty()) {
                    List<String> updatedSymbols = updatedStatus.stream().map(CryptoCurrencyStatus::getSymbol).collect(Collectors.toList());
                    List<CryptoCurrencyStatus> updatedCurrencyStatuses = dataConfig.getCryptoCurrencyStatuses().stream()
                            .filter(cryptoCurrencyStatus -> !updatedSymbols.contains(cryptoCurrencyStatus.getSymbol()))
                            .collect(Collectors.toList());
                    updatedCurrencyStatuses.addAll(updatedStatus);

                    dataConfig.setCryptoCurrencyStatuses(updatedCurrencyStatuses);
                }
                dataConfig.setToken(acquiredToken);
                daoService.updateConfig(dataConfig);
            }
        } else {
            System.out.println("It is DownTime. Waiting...");
        }
    }

    private CryptoCurrencyStatus getUpdatedCurrencyStatus(CryptoCurrencyStatus currencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse) {
        Double lastOrderPrice = Double.valueOf(cryptoOrderStatusResponse.getPrice());
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

            currencyStatus.addProfit(lastOrderPrice - currencyStatus.getLastBuyPrice());
        }
        return currencyStatus;
    }

    private boolean updatedToken(String acquiredToken, String token) {
        return acquiredToken != null && !acquiredToken.equals(token);
    }

    private boolean processCrypto(CryptoCurrencyStatus cryptoCurrencyStatus) {
        try {
            String symbol = cryptoCurrencyStatus.getSymbol();
            System.out.println("Crypto Details: " + cryptoCurrencyStatus.toString());

            CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "5minute");
            Double initialPrice = Double.valueOf(cryptoHistHourData.getDataPoints().get(cryptoHistHourData.getDataPoints().size() - 18).getClosePrice());

            CryptoHistPrice cryptoHistDayData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "hour");
            Double midNightPrice = Double.valueOf(cryptoHistDayData.getDataPoints().get(0).getClosePrice());

            Double lastPrice = Double.valueOf(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice());

            System.out.println("1.5 Hour ago Value: " + initialPrice);
            System.out.println("Current Value: " + lastPrice);

            String orderId;
            if(cryptoCurrencyStatus.isShouldBuy()) {
                orderId = actionService.analyseBuy(initialPrice, lastPrice, midNightPrice, cryptoCurrencyStatus);
            } else {
                orderId = actionService.analyseSell(lastPrice, midNightPrice, cryptoCurrencyStatus);
            }

            if(cryptoCurrencyStatus.getStopCounter() > 0) {
                cryptoCurrencyStatus.decStopCounter();
                updatedStatus.add(cryptoCurrencyStatus);
            }
            if(orderId != null) {
                pendingOrdersBySymbol.put(symbol, orderId);
                return true;
            }
        } catch (Exception ex) {
            System.out.println("Exception occured::: ");
            ex.printStackTrace();
        }

        return false;
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
