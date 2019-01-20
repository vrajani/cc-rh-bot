package pl.vrajani.model;

public class CryptoCurrencyStatus {

    String symbol;
    Double buyTotal;
    Double sellTotal;
    ActionConfig highRange; // >10
    ActionConfig mediumRange; // 5-10
    ActionConfig lowRange; // 2-5
    ActionConfig dailyRange; // usual

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("{[symbol=").append(symbol)
                .append("],[highRange=").append(highRange)
                .append("],[mediumRange=").append(mediumRange)
                .append("],[lowRange=").append(lowRange)
                .append("],[dailyRange=").append(dailyRange)
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

    public ActionConfig getHighRange() {
        return highRange;
    }

    public void setHighRange(ActionConfig highRange) {
        this.highRange = highRange;
    }

    public ActionConfig getMediumRange() {
        return mediumRange;
    }

    public void setMediumRange(ActionConfig mediumRange) {
        this.mediumRange = mediumRange;
    }

    public ActionConfig getLowRange() {
        return lowRange;
    }

    public void setLowRange(ActionConfig lowRange) {
        this.lowRange = lowRange;
    }

    public ActionConfig getDailyRange() {
        return dailyRange;
    }

    public void setDailyRange(ActionConfig dailyRange) {
        this.dailyRange = dailyRange;
    }
}
