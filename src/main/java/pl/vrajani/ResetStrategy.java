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
                System.out.println("Pending order - " + symbol + " at buy price - " + pendingOrder.get().getBuyPrice());
                CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(pendingOrder.get().getTranId());
                if (OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.FILLED)) {
                    stopLossConfigs.remove(pendingOrder.get());
                    double profit = (Double.parseDouble(cryptoOrderStatusResponse.getPrice())
                            - pendingOrder.get().getBuyPrice()) * pendingOrder.get().getQuantity();
                    profits.put(symbol, profits.getOrDefault(symbol, 0.0) + profit);
                    updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, apiService));
                } else if (OrderState.getState(cryptoOrderStatusResponse.getState()).equals(OrderState.CANCELED)) {
                    System.out.println("Pending order is cancelled. Rechecking to find the next best order to sell! - " + symbol);
                    pendingOrder.get().setTranId("");
                    updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, apiService));
                } else {
                    updatedConfigs.addAll(stopLossConfigs);
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
        List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
        mainDataConfig.getCryptoCurrencyStatuses()
            .parallelStream().forEach(cryptoCurrencyStatus -> {
                try {
                    List<CryptoCurrencyStatus> cryptoCurrencyStatuses = backTest.processCrypto(cryptoCurrencyStatus.getSymbol());
                    cryptoCurrencyStatus.setBuyPercent(MathUtil.getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getBuyPercent));
                    cryptoCurrencyStatus.setProfitPercent(MathUtil.getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getProfitPercent));
                    updatedStatuses.add(cryptoCurrencyStatus);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        mainDataConfig.setCryptoCurrencyStatuses(updatedStatuses);
        daoService.updateMainConfig(mainDataConfig);
    }
}
