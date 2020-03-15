package pl.vrajani.model;

import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "token",
        "pendingOrders",
        "cryptoCurrencyStatuses"
})
public class DataConfig {

    @JsonProperty("token")
    private String token;
    @JsonProperty("pendingOrders")
    private List<String> pendingOrders = null;
    @JsonProperty("cryptoCurrencyStatuses")
    private List<CryptoCurrencyStatus> cryptoCurrencyStatuses = null;

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("pendingOrders")
    public List<String> getPendingOrders() {
        return pendingOrders;
    }

    @JsonProperty("pendingOrders")
    public void setPendingOrders(List<String> pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    @JsonProperty("cryptoCurrencyStatuses")
    public List<CryptoCurrencyStatus> getCryptoCurrencyStatuses() {
        return cryptoCurrencyStatuses;
    }

    @JsonProperty("cryptoCurrencyStatuses")
    public void setCryptoCurrencyStatuses(List<CryptoCurrencyStatus> cryptoCurrencyStatuses) {
        this.cryptoCurrencyStatuses = cryptoCurrencyStatuses;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("token", token)
                .append("pendingOrders", pendingOrders.toString())
                .append("cryptoCurrencyStatuses", cryptoCurrencyStatuses.toString())
                .toString();
    }

    public void addPendingOrder(String symbol, String orderId) {
        this.pendingOrders.add(symbol + "," + orderId);
    }

    public void clearPendingOrder() {
        this.pendingOrders.clear();
    }
}