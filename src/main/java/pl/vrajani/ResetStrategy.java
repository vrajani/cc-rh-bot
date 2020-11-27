package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import pl.vrajani.model.*;
import pl.vrajani.request.APIService;
import pl.vrajani.service.DaoService;
import pl.vrajani.utility.MathUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ResetStrategy implements RequestHandler<Object, String> {
    private DaoService daoService;
    @Override
    public String handleRequest(Object o, Context context) {
        this.daoService = new DaoService();
        String functionName = context.getFunctionName();
        try {
            if(functionName.contains("back-test")){
                // run back test and update strategy
                runBackTest();
            } else {
                // handle stop loss Order
                handleStopLossOrders();
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return "Completed";
    }

    private void handleStopLossOrders() throws Exception {
        System.out.println("Handling Stop Loss Orders -- ");

        StopLossConfigBase stopLossConfigBase = daoService.getStopLossConfig();
        APIService apiService = Application.getApiService(daoService.getMainConfig().getToken());
        List<StopLossConfig> updatedConfigs = new ArrayList<>();
        Map<String, Double> profits = stopLossConfigBase.getProfits();

        for (Map.Entry<String, List<StopLossConfig>> entry : stopLossConfigBase.getStopLossConfigs().stream()
                .collect(Collectors.groupingBy(StopLossConfig::getSymbol)).entrySet()) {
            String symbol = entry.getKey();
            List<StopLossConfig> stopLossConfigs = entry.getValue();
            Optional<StopLossConfig> pendingOrder = stopLossConfigs.stream().filter(stopLossConfig -> !stopLossConfig.getTranId().isEmpty())
                    .findFirst();

            if (pendingOrder.isPresent()) {
                StopLossConfig pendingOrderValue = pendingOrder.get();
                System.out.println("Pending order - " + symbol + " at buy price - " + pendingOrderValue.getBuyPrice());
                CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(pendingOrderValue.getTranId());
                if (OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.FILLED)) {
                    stopLossConfigs.remove(pendingOrderValue);
                    double profit = (Double.parseDouble(cryptoOrderStatusResponse.getPrice())
                            - pendingOrderValue.getBuyPrice()) * pendingOrderValue.getQuantity();
                    profits.put(symbol, profits.getOrDefault(symbol, 0.0) + profit);
                    updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, apiService));
                } else if (OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.CANCELED)) {
                    System.out.println("Pending order is cancelled. Rechecking to find the next best order to sell! - " + symbol);
                    stopLossConfigs.remove(pendingOrderValue);
                    pendingOrderValue.setTranId("");
                    stopLossConfigs.add(pendingOrderValue);
                    updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, apiService));
                } else {
                    updatedConfigs.addAll(stopLossConfigs);

                    Optional<StopLossConfig> min = stopLossConfigs.stream().min(Comparator.comparingDouble(StopLossConfig::getBuyPrice));
                    if(min.isPresent() && min.get().getBuyPrice() < pendingOrderValue.getBuyPrice()) {
                        System.out.println("Cancelling the order as there is possibility to recover smaller stop loss order "
                                + min.get().getBuyPrice() + " < " + pendingOrderValue.getBuyPrice());
                        apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
                    }
                }
            } else {
                System.out.println("No pending order - " + symbol);
                updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, apiService));
            }
        }
        stopLossConfigBase.setStopLossConfigs(updatedConfigs);
        daoService.updateStoplossConfig(stopLossConfigBase);
    }

    private List<StopLossConfig> setAnotherOrder(List<StopLossConfig> stopLossConfigs, APIService apiService) {
        Optional<StopLossConfig> stopLossConfig = stopLossConfigs.stream().min(Comparator.comparingDouble(StopLossConfig::getBuyPrice));
        if(stopLossConfig.isPresent()){
            StopLossConfig nextSell = stopLossConfig.get();
            CryptoOrderResponse cryptoOrderResponse = apiService.sellCrypto(nextSell.getSymbol(), String.valueOf(nextSell.getQuantity()),
                    String.valueOf(MathUtil.getAmount(nextSell.getBuyPrice(), 102.0)));
            stopLossConfigs.remove(nextSell);
            nextSell.setTranId(cryptoOrderResponse.getId());
            stopLossConfigs.add(nextSell);
        }
        return stopLossConfigs;
    }

    private void runBackTest() throws IOException {
        DataConfig mainDataConfig = daoService.getMainConfig();
        BackTest backTest = new BackTest(mainDataConfig.getToken());
        Map<String, CryptoCurrencyStatus> updatedStatuses = new HashMap<>();
        mainDataConfig.getCryptoCurrencyStatuses()
            .parallelStream().forEach(cryptoCurrencyStatus -> {
                try {
                    List<CryptoCurrencyStatus> cryptoCurrencyStatuses = backTest.processCrypto(cryptoCurrencyStatus.getSymbol());
                    cryptoCurrencyStatus.setBuyPercent(MathUtil.getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getBuyPercent));
                    cryptoCurrencyStatus.setProfitPercent(MathUtil.getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getProfitPercent));
                    updatedStatuses.put(cryptoCurrencyStatus.getSymbol(), cryptoCurrencyStatus);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        DataConfig recentDataConfig = daoService.getMainConfig();
        if(isConfigUnchanged(recentDataConfig, mainDataConfig)){
            mainDataConfig.setCryptoCurrencyStatuses(new ArrayList<>(updatedStatuses.values()));
            daoService.updateMainConfig(mainDataConfig);
        } else {
            System.out.println("CryptoStatus changed since back test ran, skipping this update!");
        }
    }

    private boolean isConfigUnchanged(DataConfig recentDataConfig, DataConfig mainDataConfig) {
        return recentDataConfig.getToken().equalsIgnoreCase(mainDataConfig.getToken()) &&
                recentDataConfig.getPendingOrders().containsAll(mainDataConfig.getPendingOrders()) &&
                recentDataConfig.getCryptoCurrencyStatuses().containsAll(mainDataConfig.getCryptoCurrencyStatuses());
    }
}
