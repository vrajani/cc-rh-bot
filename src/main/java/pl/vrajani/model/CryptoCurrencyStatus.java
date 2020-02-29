package pl.vrajani.model;

public class CryptoCurrencyStatus {

    String symbol;
    boolean power;
    int stopCounter;
    int regularSell;
    int stopLossSell;
    double quantity;
    double profit;
    Double lastBuyPrice;
    Double lastSellPrice;
    boolean shouldBuy;
    Double profitPercent;
    Double buyAmount;

    public Double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(Double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    public Double getLastSellPrice() {
        return lastSellPrice;
    }

    public void setLastSellPrice(Double lastSellPrice) {
        this.lastSellPrice = lastSellPrice;
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

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{[symbol=").append(symbol)
                .append("],[stopCounter=").append(stopCounter)
                .append("],[regularSell=").append(regularSell)
                .append("],[stopLossSell=").append(stopLossSell)
                .append("],[shouldBuy=").append(shouldBuy)
                .append("],[power=").append(power)
                .append("],[profitPercent=").append(profitPercent)
                .append("],[lastBuyPrice=").append(lastBuyPrice)
                .append("],[lastSellPrice=").append(lastSellPrice)
                .append("],[buyAmount=").append(buyAmount)
                .append("],[quantity=").append(quantity)
                .append("],[profit=").append(profit)
                .append("]}").toString();
    }

    public int getStopCounter() {
        return stopCounter;
    }

    public void setStopCounter(int stopCounter) {
        this.stopCounter = stopCounter;
    }

    public int getRegularSell() {
        return regularSell;
    }

    public void setRegularSell(int regularSell) {
        this.regularSell = regularSell;
    }

    public int getStopLossSell() {
        return stopLossSell;
    }

    public void setStopLossSell(int stopLossSell) {
        this.stopLossSell = stopLossSell;
    }

    public void incRegularSell(){
        this.regularSell++;
    }

    public void incStopLossSell(){
        this.stopLossSell++;
    }

    public void decStopCounter() {
        this.stopCounter--;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
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
