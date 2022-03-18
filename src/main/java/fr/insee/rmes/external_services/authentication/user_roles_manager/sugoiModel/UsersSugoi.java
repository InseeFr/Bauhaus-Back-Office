package fr.insee.rmes.external_services.authentication.user_roles_manager.sugoiModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "results",
    "totalElements",
    "nextStart",
    "hasMoreResult",
    "pageSize",
    "searchToken"
})
@Generated("jsonschema2pojo")
public class UsersSugoi {

    @JsonProperty("results")
    private List<UserSugoi> results = null;
    @JsonProperty("totalElements")
    private Integer totalElements;
    @JsonProperty("nextStart")
    private Integer nextStart;
    @JsonProperty("hasMoreResult")
    private Boolean hasMoreResult;
    @JsonProperty("pageSize")
    private Integer pageSize;
    @JsonProperty("searchToken")
    private Object searchToken;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public UsersSugoi() {
    }

    /**
     * 
     * @param pageSize
     * @param nextStart
     * @param hasMoreResult
     * @param results
     * @param totalElements
     * @param searchToken
     */
    public UsersSugoi(List<UserSugoi> results, Integer totalElements, Integer nextStart, Boolean hasMoreResult, Integer pageSize, Object searchToken) {
        super();
        this.results = results;
        this.totalElements = totalElements;
        this.nextStart = nextStart;
        this.hasMoreResult = hasMoreResult;
        this.pageSize = pageSize;
        this.searchToken = searchToken;
    }

    @JsonProperty("results")
    public List<UserSugoi> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<UserSugoi> results) {
        this.results = results;
    }

    @JsonProperty("totalElements")
    public Integer getTotalElements() {
        return totalElements;
    }

    @JsonProperty("totalElements")
    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    @JsonProperty("nextStart")
    public Integer getNextStart() {
        return nextStart;
    }

    @JsonProperty("nextStart")
    public void setNextStart(Integer nextStart) {
        this.nextStart = nextStart;
    }

    @JsonProperty("hasMoreResult")
    public Boolean getHasMoreResult() {
        return hasMoreResult;
    }

    @JsonProperty("hasMoreResult")
    public void setHasMoreResult(Boolean hasMoreResult) {
        this.hasMoreResult = hasMoreResult;
    }

    @JsonProperty("pageSize")
    public Integer getPageSize() {
        return pageSize;
    }

    @JsonProperty("pageSize")
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @JsonProperty("searchToken")
    public Object getSearchToken() {
        return searchToken;
    }

    @JsonProperty("searchToken")
    public void setSearchToken(Object searchToken) {
        this.searchToken = searchToken;
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
        StringBuilder sb = new StringBuilder();
        sb.append(UsersSugoi.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("results");
        sb.append('=');
        sb.append(((this.results == null)?"<null>":this.results));
        sb.append(',');
        sb.append("totalElements");
        sb.append('=');
        sb.append(((this.totalElements == null)?"<null>":this.totalElements));
        sb.append(',');
        sb.append("nextStart");
        sb.append('=');
        sb.append(((this.nextStart == null)?"<null>":this.nextStart));
        sb.append(',');
        sb.append("hasMoreResult");
        sb.append('=');
        sb.append(((this.hasMoreResult == null)?"<null>":this.hasMoreResult));
        sb.append(',');
        sb.append("pageSize");
        sb.append('=');
        sb.append(((this.pageSize == null)?"<null>":this.pageSize));
        sb.append(',');
        sb.append("searchToken");
        sb.append('=');
        sb.append(((this.searchToken == null)?"<null>":this.searchToken));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null)?"<null>":this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.pageSize == null)? 0 :this.pageSize.hashCode()));
        result = ((result* 31)+((this.nextStart == null)? 0 :this.nextStart.hashCode()));
        result = ((result* 31)+((this.hasMoreResult == null)? 0 :this.hasMoreResult.hashCode()));
        result = ((result* 31)+((this.additionalProperties == null)? 0 :this.additionalProperties.hashCode()));
        result = ((result* 31)+((this.results == null)? 0 :this.results.hashCode()));
        result = ((result* 31)+((this.totalElements == null)? 0 :this.totalElements.hashCode()));
        result = ((result* 31)+((this.searchToken == null)? 0 :this.searchToken.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UsersSugoi) == false) {
            return false;
        }
        UsersSugoi rhs = ((UsersSugoi) other);
        return ((((((((this.pageSize == rhs.pageSize)||((this.pageSize!= null)&&this.pageSize.equals(rhs.pageSize)))&&((this.nextStart == rhs.nextStart)||((this.nextStart!= null)&&this.nextStart.equals(rhs.nextStart))))&&((this.hasMoreResult == rhs.hasMoreResult)||((this.hasMoreResult!= null)&&this.hasMoreResult.equals(rhs.hasMoreResult))))&&((this.additionalProperties == rhs.additionalProperties)||((this.additionalProperties!= null)&&this.additionalProperties.equals(rhs.additionalProperties))))&&((this.results == rhs.results)||((this.results!= null)&&this.results.equals(rhs.results))))&&((this.totalElements == rhs.totalElements)||((this.totalElements!= null)&&this.totalElements.equals(rhs.totalElements))))&&((this.searchToken == rhs.searchToken)||((this.searchToken!= null)&&this.searchToken.equals(rhs.searchToken))));
    }

}
