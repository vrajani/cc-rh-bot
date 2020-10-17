package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import pl.vrajani.model.*;
import pl.vrajani.request.APIService;
import pl.vrajani.service.DaoService;
import pl.vrajani.utility.MathUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ResetStrategy implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object o, Context context) {
        DaoService daoService = new DaoService();
        try {
            DataConfig mainDataConfig = daoService.getMainConfig();
            // handle stop loss Order
            handleStopLossOrders(daoService, mainDataConfig.getToken());

            // run back test and update strategy
            runBackTest(daoService, mainDataConfig);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return "Completed";
    }

    private void handleStopLossOrders(DaoService daoService, String token) throws IOException {
        System.out.println("Handling Stop Loss Orders -- ");
        StopLossConfigBase stopLossConfigBase = daoService.getStopLossConfig();
        APIService apiService = Application.getApiService(token);
        List<StopLossConfig> updatedConfigs = new ArrayList<>();

        stopLossConfigBase.getStopLossConfigs().stream()
                .collect(Collectors.groupingBy(StopLossConfig::getSymbol))
                .forEach((symbol, stopLossConfigs) -> {
                    Optional<StopLossConfig> pendingOrder = stopLossConfigs.stream().filter(stopLossConfig -> !stopLossConfig.getTranId().isEmpty())
                            .findFirst();

                    if(pendingOrder.isPresent()){
                        System.out.println("Pending order - "+ symbol + " at buy price - " + pendingOrder.get().getBuyPrice());
                        CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(pendingOrder.get().getTranId());
                        if(CryptoOrderState.getState(cryptoOrderStatusResponse.getState()).equals(CryptoOrderState.FILLED)) {
                            updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, pendingOrder, apiService));
                        } else {
                            updatedConfigs.addAll(stopLossConfigs);
                        }
                    } else {
                        System.out.println("No pending order - "+ symbol);
                        updatedConfigs.addAll(setAnotherOrder(stopLossConfigs, pendingOrder, apiService));
                    }
                });
        stopLossConfigBase.setStopLossConfigs(updatedConfigs);
        daoService.updateStoplossConfig(stopLossConfigBase);
    }

    private List<StopLossConfig> setAnotherOrder(List<StopLossConfig> stopLossConfigs, Optional<StopLossConfig> filledOrder, APIService apiService) {
        filledOrder.ifPresent(stopLossConfigs::remove);
        stopLossConfigs.stream().min(Comparator.comparingDouble(StopLossConfig::getBuyPrice)).ifPresent(nextSell -> {
            CryptoOrderResponse cryptoOrderResponse = apiService.sellCrypto(nextSell.getSymbol(), String.valueOf(nextSell.getQuantity()),
                    String.valueOf(MathUtil.getAmount(nextSell.getBuyPrice(), 102.0)));
            nextSell.setTranId(cryptoOrderResponse.getId());
        });
        return stopLossConfigs;
    }

    private void runBackTest(DaoService daoService, DataConfig mainDataConfig) throws JsonProcessingException {
        BackTest backTest = new BackTest(mainDataConfig.getToken());
        List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
        mainDataConfig.getCryptoCurrencyStatuses()
            .forEach(cryptoCurrencyStatus -> {
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
