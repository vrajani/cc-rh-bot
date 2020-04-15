package pl.vrajani.model;

public class CryptoCurrencyStatus extends CryptoStatusBase {
    int stopCounter;
    int regularSell;
    int stopLossSell;
    double quantity;
    Double lastBuyPrice;
    Double lastSellPrice;

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
}
