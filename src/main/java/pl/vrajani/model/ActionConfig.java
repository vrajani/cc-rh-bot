package pl.vrajani.model;

public class ActionConfig {

    boolean power;
    Double lastBuyPrice;
    Double lastSalePrice;
    boolean shouldBuy;
    Double profitPercent;
    Double buyAmount;

    public Double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(Double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    public Double getLastSalePrice() {
        return lastSalePrice;
    }

    public void setLastSalePrice(Double lastSalePrice) {
        this.lastSalePrice = lastSalePrice;
    }

    public boolean isShouldBuy() {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy) {
        this.shouldBuy = shouldBuy;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{ActionConfig:[shouldBuy=").append(shouldBuy)
                .append("],[power=").append(power)
                .append("],[profitPercent=").append(profitPercent)
                .append("],[lastBuyPrice=").append(lastBuyPrice)
                .append("],[lastSalePrice=").append(lastSalePrice)
                .append("],[buyAmount=").append(buyAmount)
                .append("]}").toString();
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

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }
}
