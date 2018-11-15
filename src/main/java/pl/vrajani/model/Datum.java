
package pl.vrajani.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "time",
    "close",
    "high",
    "low",
    "open",
    "volumefrom",
    "volumeto"
})
public class Datum {

    @JsonProperty("time")
    private Integer time;
    @JsonProperty("close")
    private Double close;
    @JsonProperty("high")
    private Double high;
    @JsonProperty("low")
    private Double low;
    @JsonProperty("open")
    private Double open;
    @JsonProperty("volumefrom")
    private Double volumefrom;
    @JsonProperty("volumeto")
    private Double volumeto;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("time")
    public Integer getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(Integer time) {
        this.time = time;
    }

    @JsonProperty("close")
    public Double getClose() {
        return close;
    }

    @JsonProperty("close")
    public void setClose(Double close) {
        this.close = close;
    }

    @JsonProperty("high")
    public Double getHigh() {
        return high;
    }

    @JsonProperty("high")
    public void setHigh(Double high) {
        this.high = high;
    }

    @JsonProperty("low")
    public Double getLow() {
        return low;
    }

    @JsonProperty("low")
    public void setLow(Double low) {
        this.low = low;
    }

    @JsonProperty("open")
    public Double getOpen() {
        return open;
    }

    @JsonProperty("open")
    public void setOpen(Double open) {
        this.open = open;
    }

    @JsonProperty("volumefrom")
    public Double getVolumefrom() {
        return volumefrom;
    }

    @JsonProperty("volumefrom")
    public void setVolumefrom(Double volumefrom) {
        this.volumefrom = volumefrom;
    }

    @JsonProperty("volumeto")
    public Double getVolumeto() {
        return volumeto;
    }

    @JsonProperty("volumeto")
    public void setVolumeto(Double volumeto) {
        this.volumeto = volumeto;
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
        return new ToStringBuilder(this).append("time", time).append("close", close).append("high", high).append("low", low).append("open", open).append("volumefrom", volumefrom).append("volumeto", volumeto).append("additionalProperties", additionalProperties).toString();
    }

}
