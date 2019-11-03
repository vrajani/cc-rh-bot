package pl.vrajani.model;

public class CryptoCurrencyStatus {

    String symbol;
    Double buyTotal;
    Double sellTotal;
    ActionConfig range;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{[symbol=").append(symbol)
                .append("],[range=").append(range)
                .append("],[buyTotal=").append(buyTotal)
                .append("],[sellTotal=").append(sellTotal)
                .append("]}").toString();
    }

    public Double getBuyTotal() {
        return buyTotal;
    }

    public void setBuyTotal(Double buyTotal) {
        this.buyTotal = buyTotal;
    }

    public Double getSellTotal() {
        return sellTotal;
    }

    public void setSellTotal(Double sellTotal) {
        this.sellTotal = sellTotal;
    }

    public ActionConfig getRange() {
        return range;
    }

    public void setRange(ActionConfig range) {
        this.range = range;
    }
}
