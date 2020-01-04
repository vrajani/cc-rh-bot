package pl.vrajani.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cryptoCurrencyStatuses"
})
public class CryptoConfig {

    @JsonProperty("cryptoCurrencyStatuses")
    private List<CryptoCurrencyStatus> cryptoCurrencyStatuses = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("cryptoCurrencyStatuses")
    public List<CryptoCurrencyStatus> getCryptoCurrencyStatuses() {
        return cryptoCurrencyStatuses;
    }

    @JsonProperty("cryptoCurrencyStatuses")
    public void setCryptoCurrencyStatuses(List<CryptoCurrencyStatus> cryptoCurrencyStatuses) {
        this.cryptoCurrencyStatuses = cryptoCurrencyStatuses;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cryptoCurrencyStatuses", cryptoCurrencyStatuses).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cryptoCurrencyStatuses).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CryptoConfig) == false) {
            return false;
        }
        CryptoConfig rhs = ((CryptoConfig) other);
        return new EqualsBuilder().append(cryptoCurrencyStatuses, rhs.cryptoCurrencyStatuses).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
