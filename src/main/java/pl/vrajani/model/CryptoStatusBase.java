package pl.vrajani.model;

public class CryptoStatusBase {
    String symbol;
    boolean power;
    double profit;
    boolean shouldBuy;
    Double profitPercent;
    Double buyAmount;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean isShouldBuy() {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy) {
        this.shouldBuy = shouldBuy;
    }

    public Double getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(Double profitPercent) {
        this.profitPercent = profitPercent;
    }

    public Double getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(Double buyAmount) {
        this.buyAmount = buyAmount;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void addProfit(double change) {
        this.profit = this.profit + change;
    }
}
