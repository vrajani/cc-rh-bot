package pl.vrajani.model;

import java.util.List;
import java.util.Map;

public class StopLossConfigBase {
    private List<StopLossConfig> stopLossConfigs;
    private Map<String, Double> profits;

    public List<StopLossConfig> getStopLossConfigs() {
        return stopLossConfigs;
    }

    public void setStopLossConfigs(List<StopLossConfig> stopLossConfigs) {
        this.stopLossConfigs = stopLossConfigs;
    }

    public Map<String, Double> getProfits() {
        return profits;
    }

    public void setProfits(Map<String, Double> profits) {
        this.profits = profits;
    }
}
