package pl.vrajani.service;

import pl.vrajani.model.StopLossConfig;

import java.io.IOException;

public class LimitManager {
    private static final double LIMIT_DOLLAR_STOP_LOSS = 1500.0;
    private final DaoService daoService;

    public LimitManager(DaoService daoService) {
        this.daoService = daoService;
    }

    boolean shouldBuyMore(String symbol, double lastPrice) throws IOException {
        double pendingStopLossQuantity = daoService.getStopLossConfig().getStopLossConfigs()
                .stream()
                .filter(stopLossConfig -> stopLossConfig.getSymbol().equalsIgnoreCase(symbol))
                .mapToDouble(StopLossConfig::getQuantity)
                .sum();
        return lastPrice * pendingStopLossQuantity < LIMIT_DOLLAR_STOP_LOSS;
    }
}
