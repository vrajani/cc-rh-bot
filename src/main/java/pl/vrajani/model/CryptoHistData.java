package pl.vrajani.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Response",
    "Type",
    "Aggregated",
    "Data",
    "TimeTo",
    "TimeFrom",
    "FirstValueInArray",
})
public class CryptoHistData {

    @JsonProperty("Response")
    private String response;
    @JsonProperty("Type")
    private Integer type;
    @JsonProperty("Aggregated")
    private Boolean aggregated;
    @JsonProperty("Data")
    private List<Datum> data = null;
    @JsonProperty("TimeTo")
    private Integer timeTo;
    @JsonProperty("TimeFrom")
    private Integer timeFrom;
    @JsonProperty("FirstValueInArray")
    private Boolean firstValueInArray;

    @JsonProperty("Response")
    public String getResponse() {
        return response;
    }

    @JsonProperty("Response")
    public void setResponse(String response) {
        this.response = response;
    }

    @JsonProperty("Type")
    public Integer getType() {
        return type;
    }

    @JsonProperty("Type")
    public void setType(Integer type) {
        this.type = type;
    }

    @JsonProperty("Aggregated")
    public Boolean getAggregated() {
        return aggregated;
    }

    @JsonProperty("Aggregated")
    public void setAggregated(Boolean aggregated) {
        this.aggregated = aggregated;
    }

    @JsonProperty("Data")
    public List<Datum> getData() {
        return data;
    }

    @JsonProperty("Data")
    public void setData(List<Datum> data) {
        this.data = data;
    }

    @JsonProperty("TimeTo")
    public Integer getTimeTo() {
        return timeTo;
    }

    @JsonProperty("TimeTo")
    public void setTimeTo(Integer timeTo) {
        this.timeTo = timeTo;
    }

    @JsonProperty("TimeFrom")
    public Integer getTimeFrom() {
        return timeFrom;
    }

    @JsonProperty("TimeFrom")
    public void setTimeFrom(Integer timeFrom) {
        this.timeFrom = timeFrom;
    }

    @JsonProperty("FirstValueInArray")
    public Boolean getFirstValueInArray() {
        return firstValueInArray;
    }

    @JsonProperty("FirstValueInArray")
    public void setFirstValueInArray(Boolean firstValueInArray) {
        this.firstValueInArray = firstValueInArray;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("response", response)
                .append("type", type)
                .append("aggregated", aggregated)
                .append("data", data)
                .append("timeTo", timeTo)
                .append("timeFrom", timeFrom)
                .append("firstValueInArray", firstValueInArray)
                .toString();
    }

}
