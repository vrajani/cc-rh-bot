package pl.vrajani.service;

import pl.vrajani.model.*;
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

    public ControllerService(APIService apiService, ActionService actionService, DaoService daoService){
        this.apiService = apiService;
        this.actionService = actionService;
        this.daoService = daoService;

        this.pendingOrdersBySymbol = new HashMap<>();
        this.updatedStatus = new ArrayList<>();
    }

    public void checkAllCrypto() throws IOException {
        System.out.println("Initiating the check::::");

        if(!TimeUtil.isDownTime()) {
            DataConfig dataConfig = daoService.getDataConfig();
            dataConfig.getPendingOrders().stream()
                .map(pendingOrder -> pendingOrder.split("\\|"))
                .forEach(split -> pendingOrdersBySymbol.put(split[0], split[1]));
            boolean updatedPendingOrders = false;
            for (CryptoCurrencyStatus currencyStatus : dataConfig.getCryptoCurrencyStatuses()) {
                if (currencyStatus.isPower()) {
                    String symbol = currencyStatus.getSymbol();
                    if (pendingOrdersBySymbol.containsKey(symbol)) {
                        String previousOrderId = pendingOrdersBySymbol.get(symbol);
                        CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrderId);
                        if ("filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                            dataConfig.removePendingOrder(symbol, pendingOrdersBySymbol.get(symbol));
                            updatedPendingOrders = true;
                            daoService.registerCompletedTransaction(cryptoOrderStatusResponse);
                            processCrypto(currencyStatus);
                        } else {
                            System.out.println("Skipping crypto as there is a pending order: " + symbol + " with order Id: " + previousOrderId);
                        }
                    } else {
                        processCrypto(currencyStatus);
                    }
                }
            }

            if(!updatedStatus.isEmpty() || updatedPendingOrders){
                if(!updatedStatus.isEmpty()) {
                    List<String> updatedSymbols = updatedStatus.stream().map(CryptoCurrencyStatus::getSymbol).collect(Collectors.toList());
                    List<CryptoCurrencyStatus> updatedCurrencyStatuses = dataConfig.getCryptoCurrencyStatuses().stream()
                            .filter(cryptoCurrencyStatus -> !updatedSymbols.contains(cryptoCurrencyStatus.getSymbol()))
                            .collect(Collectors.toList());
                    updatedCurrencyStatuses.addAll(updatedStatus);

                    dataConfig.setCryptoCurrencyStatuses(updatedCurrencyStatuses);
                }
                daoService.updateConfig(dataConfig);
            }
        } else {
            System.out.println("It is DownTime. Waiting...");
        }
    }

    private void processCrypto(CryptoCurrencyStatus cryptoCurrencyStatus) {
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

            TransactionUpdate transactionUpdate;
            if(cryptoCurrencyStatus.isShouldBuy()) {
                transactionUpdate = actionService.analyseBuy(initialPrice, lastPrice, midNightPrice, cryptoCurrencyStatus);
            } else {
                transactionUpdate = actionService.analyseSell(lastPrice, cryptoCurrencyStatus);
            }

            if (transactionUpdate != null || cryptoCurrencyStatus.getStopCounter() > 0) {
                if(cryptoCurrencyStatus.getStopCounter() > 0) {
                    cryptoCurrencyStatus.decStopCounter();
                } else if (transactionUpdate != null) {
                    // update Cryto currency as well
                    pendingOrdersBySymbol.put(symbol, transactionUpdate.getOrderId());
                }
                updatedStatus.add(cryptoCurrencyStatus);
            }
        } catch (Exception ex) {
            System.out.println("Exception occured::: ");
            ex.printStackTrace();
        }
    }
}
