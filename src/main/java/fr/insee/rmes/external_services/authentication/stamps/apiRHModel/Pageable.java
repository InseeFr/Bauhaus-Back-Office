
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
    "sort",
    "pageNumber",
    "pageSize",
    "offset",
    "unpaged",
    "paged"
})
@Generated("jsonschema2pojo")
public class Pageable {

    @JsonProperty("sort")
    @Valid
    private Sort sort;
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    @JsonProperty("pageSize")
    private Integer pageSize;
    @JsonProperty("offset")
    private Integer offset;
    @JsonProperty("unpaged")
    private Boolean unpaged;
    @JsonProperty("paged")
    private Boolean paged;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pageable() {
    }

    /**
     * 
     * @param paged
     * @param pageNumber
     * @param offset
     * @param pageSize
     * @param unpaged
     * @param sort
     */
    public Pageable(Sort sort, Integer pageNumber, Integer pageSize, Integer offset, Boolean unpaged, Boolean paged) {
        super();
        this.sort = sort;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.offset = offset;
        this.unpaged = unpaged;
        this.paged = paged;
    }

    @JsonProperty("sort")
    public Sort getSort() {
        return sort;
    }

    @JsonProperty("sort")
    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Pageable withSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    @JsonProperty("pageNumber")
    public Integer getPageNumber() {
        return pageNumber;
    }

    @JsonProperty("pageNumber")
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Pageable withPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    @JsonProperty("pageSize")
    public Integer getPageSize() {
        return pageSize;
    }

    @JsonProperty("pageSize")
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Pageable withPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @JsonProperty("offset")
    public Integer getOffset() {
        return offset;
    }

    @JsonProperty("offset")
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Pageable withOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    @JsonProperty("unpaged")
    public Boolean getUnpaged() {
        return unpaged;
    }

    @JsonProperty("unpaged")
    public void setUnpaged(Boolean unpaged) {
        this.unpaged = unpaged;
    }

    public Pageable withUnpaged(Boolean unpaged) {
        this.unpaged = unpaged;
        return this;
    }

    @JsonProperty("paged")
    public Boolean getPaged() {
        return paged;
    }

    @JsonProperty("paged")
    public void setPaged(Boolean paged) {
        this.paged = paged;
    }

    public Pageable withPaged(Boolean paged) {
        this.paged = paged;
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

    public Pageable withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
