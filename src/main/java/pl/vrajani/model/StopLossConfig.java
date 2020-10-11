package pl.vrajani.model;

import java.util.Optional;

public class StopLossConfig {
    private Optional<String> tranId;
    private String symbol;
    private double quantity;
    private double lastBuyPrice;

    public Optional<String> getTranId() {
        return tranId;
    }

    public void setTranId(Optional<String> tranId) {
        this.tranId = tranId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    @Override
    public String toString() {
        return "StopLossConfig{" +
                "tranId=" + tranId +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", lastBuyPrice=" + lastBuyPrice +
                '}';
    }
}
