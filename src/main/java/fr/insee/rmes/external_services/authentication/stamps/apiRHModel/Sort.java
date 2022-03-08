
package fr.insee.rmes.external_services.authentication.stamps.apiRHModel;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sorted",
    "unsorted",
    "empty"
})
@Generated("jsonschema2pojo")
public class Sort {

    @JsonProperty("sorted")
    private Boolean sorted;
    @JsonProperty("unsorted")
    private Boolean unsorted;
    @JsonProperty("empty")
    private Boolean empty;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Sort() {
    }

    /**
     * 
     * @param unsorted
     * @param sorted
     * @param empty
     */
    public Sort(Boolean sorted, Boolean unsorted, Boolean empty) {
        super();
        this.sorted = sorted;
        this.unsorted = unsorted;
        this.empty = empty;
    }

    @JsonProperty("sorted")
    public Boolean getSorted() {
        return sorted;
    }

    @JsonProperty("sorted")
    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }

    public Sort withSorted(Boolean sorted) {
        this.sorted = sorted;
        return this;
    }

    @JsonProperty("unsorted")
    public Boolean getUnsorted() {
        return unsorted;
    }

    @JsonProperty("unsorted")
    public void setUnsorted(Boolean unsorted) {
        this.unsorted = unsorted;
    }

    public Sort withUnsorted(Boolean unsorted) {
        this.unsorted = unsorted;
        return this;
    }

    @JsonProperty("empty")
    public Boolean getEmpty() {
        return empty;
    }

    @JsonProperty("empty")
    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Sort withEmpty(Boolean empty) {
        this.empty = empty;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Sort withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
