
package fr.insee.rmes.external_services.authentication.stamps.apiRHModel;

import java.util.HashMap;
import java.util.List;
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
    "content",
    "pageable",
    "last",
    "totalElements",
    "totalPages",
    "first",
    "sort",
    "number",
    "numberOfElements",
    "size",
    "empty"
})
@Generated("jsonschema2pojo")
public class Stamp {

    @JsonProperty("content")
    @Valid
    private List<Content> content = null;
    @JsonProperty("pageable")
    @Valid
    private Pageable pageable;
    @JsonProperty("last")
    private Boolean last;
    @JsonProperty("totalElements")
    private Integer totalElements;
    @JsonProperty("totalPages")
    private Integer totalPages;
    @JsonProperty("first")
    private Boolean first;
    @JsonProperty("sort")
    @Valid
    private Sort__1 sort;
    @JsonProperty("number")
    private Integer number;
    @JsonProperty("numberOfElements")
    private Integer numberOfElements;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("empty")
    private Boolean empty;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stamp() {
    }

    /**
     * 
     * @param number
     * @param last
     * @param numberOfElements
     * @param size
     * @param totalPages
     * @param pageable
     * @param sort
     * @param content
     * @param first
     * @param totalElements
     * @param empty
     */
    public Stamp(List<Content> content, Pageable pageable, Boolean last, Integer totalElements, Integer totalPages, Boolean first, Sort__1 sort, Integer number, Integer numberOfElements, Integer size, Boolean empty) {
        super();
        this.content = content;
        this.pageable = pageable;
        this.last = last;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.sort = sort;
        this.number = number;
        this.numberOfElements = numberOfElements;
        this.size = size;
        this.empty = empty;
    }

    @JsonProperty("content")
    public List<Content> getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(List<Content> content) {
        this.content = content;
    }

    public Stamp withContent(List<Content> content) {
        this.content = content;
        return this;
    }

    @JsonProperty("pageable")
    public Pageable getPageable() {
        return pageable;
    }

    @JsonProperty("pageable")
    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public Stamp withPageable(Pageable pageable) {
        this.pageable = pageable;
        return this;
    }

    @JsonProperty("last")
    public Boolean getLast() {
        return last;
    }

    @JsonProperty("last")
    public void setLast(Boolean last) {
        this.last = last;
    }

    public Stamp withLast(Boolean last) {
        this.last = last;
        return this;
    }

    @JsonProperty("totalElements")
    public Integer getTotalElements() {
        return totalElements;
    }

    @JsonProperty("totalElements")
    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Stamp withTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
        return this;
    }

    @JsonProperty("totalPages")
    public Integer getTotalPages() {
        return totalPages;
    }

    @JsonProperty("totalPages")
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Stamp withTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    @JsonProperty("first")
    public Boolean getFirst() {
        return first;
    }

    @JsonProperty("first")
    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Stamp withFirst(Boolean first) {
        this.first = first;
        return this;
    }

    @JsonProperty("sort")
    public Sort__1 getSort() {
        return sort;
    }

    @JsonProperty("sort")
    public void setSort(Sort__1 sort) {
        this.sort = sort;
    }

    public Stamp withSort(Sort__1 sort) {
        this.sort = sort;
        return this;
    }

    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(Integer number) {
        this.number = number;
    }

    public Stamp withNumber(Integer number) {
        this.number = number;
        return this;
    }

    @JsonProperty("numberOfElements")
    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    @JsonProperty("numberOfElements")
    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Stamp withNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
        return this;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    public Stamp withSize(Integer size) {
        this.size = size;
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

    public Stamp withEmpty(Boolean empty) {
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

    public Stamp withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
