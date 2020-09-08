package pl.vrajani.model;

public class CryptoStatusBase {
    String ccId;
    String symbol;
    boolean power;
    double profit;
    boolean shouldBuy;
    double profitPercent;
    double buyPercent;
    double buyAmount;

    public String getSymbol() {
        return symbol;
    }

    public boolean isPower() {
        return power;
    }

    public boolean isShouldBuy() {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy) {
        this.shouldBuy = shouldBuy;
    }

    public double getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(double profitPercent) {
        this.profitPercent = profitPercent;
    }

    public double getBuyAmount() {
        return buyAmount;
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

    public double getBuyPercent() {
        return buyPercent;
    }

    public void setBuyPercent(double buyPercent) {
        this.buyPercent = buyPercent;
    }

    public String getCcId() {
        return ccId;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setCcId(String ccId) {
        this.ccId = ccId;
    }

    public void setBuyAmount(double buyAmount) {
        this.buyAmount = buyAmount;
    }
}
