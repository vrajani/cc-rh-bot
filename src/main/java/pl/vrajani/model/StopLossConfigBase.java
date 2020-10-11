package pl.vrajani.model;

import java.util.List;

public class StopLossConfigBase {
    private List<StopLossConfig> stopLossConfigs;

    public List<StopLossConfig> getStopLossConfigs() {
        return stopLossConfigs;
    }

    public void setStopLossConfigs(List<StopLossConfig> stopLossConfigs) {
        this.stopLossConfigs = stopLossConfigs;
    }
}
