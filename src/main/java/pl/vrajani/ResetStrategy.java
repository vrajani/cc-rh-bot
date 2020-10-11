package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import pl.vrajani.model.*;
import pl.vrajani.service.DaoService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResetStrategy implements RequestHandler<Object, String> {
    @Override
    public String handleRequest(Object o, Context context) {
        DaoService daoService = new DaoService();
        try {
            DataConfig mainDataConfig = daoService.getMainConfig();
            StopLossConfigBase stopLossConfig = daoService.getStopLossConfig();

            Set<StopLossConfig> pendingOrders = stopLossConfig.getStopLossConfigs().stream()
                    .filter(config -> config.getTranId().isPresent())
                    .collect(Collectors.toSet());

            Set<StopLossConfig> soldCryptos = handlePendingOrders(pendingOrders);
            stopLossConfig.getStopLossConfigs().removeAll(soldCryptos);
            for(StopLossConfig soldCrypto: soldCryptos){
                stopLossConfig.getStopLossConfigs()
                        .stream()
                        .filter(config -> config.getSymbol().equalsIgnoreCase(soldCrypto.getSymbol()))
                        .filter(config -> config.getTranId().isEmpty())
                        .min(Comparator.comparingDouble(StopLossConfig::getLastBuyPrice))
                        .map(this::setSellOrder)
                        .ifPresent(cryptoOrderStatusResponse ->
                                stopLossConfig.getStopLossConfigs().add(getStopLossConfig(cryptoOrderStatusResponse, soldCrypto.getSymbol())));
            }

            // run back test and update strategy
            runBackTest(daoService, mainDataConfig);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return "Completed";
    }

    private void runBackTest(DaoService daoService, DataConfig mainDataConfig) throws JsonProcessingException {
        BackTest backTest = new BackTest(mainDataConfig.getToken());
        List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
        mainDataConfig.getCryptoCurrencyStatuses()
            .forEach(cryptoCurrencyStatus -> {
                try {
                    List<CryptoCurrencyStatus> cryptoCurrencyStatuses = backTest.processCrypto(cryptoCurrencyStatus.getSymbol());
                    cryptoCurrencyStatus.setBuyPercent(getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getBuyPercent));
                    cryptoCurrencyStatus.setProfitPercent(getMedianPercent(cryptoCurrencyStatuses, CryptoStatusBase::getProfitPercent));
                    updatedStatuses.add(cryptoCurrencyStatus);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        mainDataConfig.setCryptoCurrencyStatuses(updatedStatuses);
        daoService.updateMainConfig(mainDataConfig);
    }

    private double getMedianPercent(List<CryptoCurrencyStatus> cryptoCurrencyStatuses, Function<CryptoCurrencyStatus, Double> mapperFunction) {
        return cryptoCurrencyStatuses.stream().map(mapperFunction).sorted().collect(Collectors.toList()).get(BackTest.TOP_K / 2);
    }

    private StopLossConfig getStopLossConfig(CryptoOrderStatusResponse cryptoOrderStatusResponse, String symbol) {
        StopLossConfig stopLossConfig = new StopLossConfig();
        stopLossConfig.setSymbol(symbol);
        stopLossConfig.setLastBuyPrice(Double.parseDouble(cryptoOrderStatusResponse.getPrice()));
        return null;
    }

    private CryptoOrderStatusResponse setSellOrder(StopLossConfig toBeSold) {
        return null;
    }

    private Set<StopLossConfig> handlePendingOrders(Set<StopLossConfig> pendingOrders) {

        return Set.of();
    }
}
