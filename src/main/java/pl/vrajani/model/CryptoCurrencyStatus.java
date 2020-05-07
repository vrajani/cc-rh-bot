package pl.vrajani.model;

public class CryptoCurrencyStatus extends CryptoStatusBase {
    int stopCounter;
    int regularSell;
    int stopLossSell;
    double quantity;
    double lastBuyPrice;
    double lastSellPrice;

    public double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    public double getLastSellPrice() {
        return lastSellPrice;
    }

    public void setLastSellPrice(double lastSellPrice) {
        this.lastSellPrice = lastSellPrice;
    }

    @Override
    public String toString() {
        return "{[symbol=" + symbol +
                "],[stopCounter=" + stopCounter +
                "],[regularSell=" + regularSell +
                "],[stopLossSell=" + stopLossSell +
                "],[shouldBuy=" + shouldBuy +
                "],[power=" + power +
                "],[profitPercent=" + profitPercent +
                "],[lastBuyPrice=" + lastBuyPrice +
                "],[lastSellPrice=" + lastSellPrice +
                "],[buyAmount=" + buyAmount +
                "],[quantity=" + quantity +
                "],[profit=" + profit +
                "],[buyPercent=" + buyPercent +
                "]}";
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
