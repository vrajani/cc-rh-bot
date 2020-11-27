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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CryptoCurrencyStatus) {
            CryptoCurrencyStatus other = (CryptoCurrencyStatus) obj;
            return this.ccId.equals(other.ccId) &&
                    this.symbol.equals(other.symbol) &&
                    this.buyAmount == other.buyAmount &&
                    this.shouldBuy == other.shouldBuy &&
                    this.power == other.power &&
                    this.profit == other.profit &&
                    this.lastBuyPrice == other.lastBuyPrice &&
                    this.quantity == other.quantity &&
                    this.regularSell == other.regularSell &&
                    this.buyPercent == other.buyPercent &&
                    this.profitPercent == other.profitPercent &&
                    this.waitInMinutes == other.waitInMinutes;
        } else {
            return false;
        }
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

    public void setWaitInMinutes(int waitInMinutes) {
        this.waitInMinutes = waitInMinutes;
    }
}
