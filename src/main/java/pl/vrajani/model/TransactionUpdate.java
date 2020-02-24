package pl.vrajani.model;

public class TransactionUpdate {
    private String symbol;
    private CryptoCurrencyStatus cryptoCurrencyStatus;
    private String orderId;

    public TransactionUpdate(String symbol, CryptoCurrencyStatus cryptoCurrencyStatus, String orderId) {
        this.symbol = symbol;
        this.cryptoCurrencyStatus = cryptoCurrencyStatus;
        this.orderId = orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public CryptoCurrencyStatus getCryptoCurrencyStatus() {
        return cryptoCurrencyStatus;
    }

    public String getOrderId() {
        return orderId;
    }
}
