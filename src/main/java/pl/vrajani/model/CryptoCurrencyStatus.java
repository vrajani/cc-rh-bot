package pl.vrajani.model;

public class CryptoCurrencyStatus {

    String symbol;
    Double buyTotal;
    Double sellTotal;
    int stopCounter;
    ActionConfig range;
    int regularSell;
    int stopLossSell;

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
                .append("],[range=").append(range)
                .append("],[buyTotal=").append(buyTotal)
                .append("],[sellTotal=").append(sellTotal)
                .append("],[regularSell=").append(regularSell)
                .append("],[stopLossSell=").append(stopLossSell)
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
}
