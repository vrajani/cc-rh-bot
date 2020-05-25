package pl.vrajani.model;

public class CryptoCurrencyStatus extends CryptoStatusBase {
    int regularSell;
    double quantity;
    double lastBuyPrice;
    int waitInMinutes;

    public double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    @Override
    public String toString() {
        return "{[symbol=" + symbol +
                "],[regularSell=" + regularSell +
                "],[shouldBuy=" + shouldBuy +
                "],[power=" + power +
                "],[profitPercent=" + profitPercent +
                "],[lastBuyPrice=" + lastBuyPrice +
                "],[buyAmount=" + buyAmount +
                "],[quantity=" + quantity +
                "],[profit=" + profit +
                "],[buyPercent=" + buyPercent +
                "],[waitInMinutes=" + waitInMinutes +
                "],[ccId=" + ccId +
                "]}";
    }

    public int getRegularSell() {
        return regularSell;
    }

    public void setRegularSell(int regularSell) {
        this.regularSell = regularSell;
    }

    public void incRegularSell(){
        this.regularSell++;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getWaitInMinutes() {
        return waitInMinutes;
    }

}
