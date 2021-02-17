package pl.vrajani.service;

import java.io.IOException;

public class LimitManager {
    private static final int LIMIT_STOP_LOSS_PENDING_ORDER = 4;
    private final DaoService daoService;

    public LimitManager(DaoService daoService) {
        this.daoService = daoService;
    }

    boolean shouldBuyMore(String symbol, double lastPrice) throws IOException {
        return daoService.getStopLossConfig().getStopLossConfigs()
                .stream()
                .filter(stopLossConfig -> stopLossConfig.getSymbol().equalsIgnoreCase(symbol))
                .count() < LIMIT_STOP_LOSS_PENDING_ORDER;
    }
}
