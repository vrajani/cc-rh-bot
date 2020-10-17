package pl.vrajani.model;

import java.util.Optional;

public class StopLossConfig {
    private String tranId;
    private String symbol;
    private double quantity;
    private double buyPrice;

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
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

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double lastBuyPrice) {
        this.buyPrice = lastBuyPrice;
    }

    @Override
    public String toString() {
        return "StopLossConfig{" +
                "tranId=" + tranId +
                ", symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", lastBuyPrice=" + buyPrice +
                '}';
    }
}
