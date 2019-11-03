package pl.vrajani.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "USD"
})
public class LatestPrice {

    @JsonProperty("USD")
    private Double uSD;

    @JsonProperty("USD")
    public Double getUSD() {
        return uSD;
    }

    @JsonProperty("USD")
    public void setUSD(Double uSD) {
        this.uSD = uSD;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("uSD", uSD).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uSD).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LatestPrice) == false) {
            return false;
        }
        LatestPrice rhs = ((LatestPrice) other);
        return new EqualsBuilder().append(uSD, rhs.uSD).isEquals();
    }

}