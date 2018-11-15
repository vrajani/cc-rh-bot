package pl.vrajani.model;

public class CryptoCurrencyStatus {

    String symbol;
    Double lastBuyPrice;
    Double lastSalePrice;
    boolean shouldBuy;
    boolean shouldSell;


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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public boolean isShouldBuy() {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy) {
        this.shouldBuy = shouldBuy;
    }

    public boolean isShouldSell() {
        return shouldSell;
    }

    public void setShouldSell(boolean shouldSell) {
        this.shouldSell = shouldSell;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{[symbol=").append(symbol)
                .append("],[lastBuyPrice=").append(lastBuyPrice)
                .append("],[lastSalePrice=").append(lastSalePrice)
                .append("]}").toString();
    }
}
