package pl.vrajani.model;

import java.util.List;

public class DataConfig {

    private String token;
    private List<String> pendingOrders = null;
    private List<CryptoCurrencyStatus> cryptoCurrencyStatuses = null;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getPendingOrders() {
        return pendingOrders;
    }
    public void setPendingOrders(List<String> pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public List<CryptoCurrencyStatus> getCryptoCurrencyStatuses() {
        return cryptoCurrencyStatuses;
    }
    public void setCryptoCurrencyStatuses(List<CryptoCurrencyStatus> cryptoCurrencyStatuses) {
        this.cryptoCurrencyStatuses = cryptoCurrencyStatuses;
    }

    @Override
    public String toString() {
        return "DataConfig{" +
                "token='" + token + '\'' +
                ", pendingOrders=" + pendingOrders +
                ", cryptoCurrencyStatuses=" + cryptoCurrencyStatuses.toString() +
                '}';
    }

    public void addPendingOrder(String symbol, String orderId) {
        this.pendingOrders.add(symbol + "," + orderId);
    }

    public void clearPendingOrder() {
        this.pendingOrders.clear();
    }
}