package fr.insee.rmes.webservice.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import fr.insee.rmes.webservice.DomainToResponseConverter;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic base class for response objects that provides HATEOAS support
 * and domain-to-response conversion contract.
 * 
 * @param <T> the concrete response type
 * @param <D> the domain object type
 */
public abstract class BaseResponse<T extends BaseResponse<T, D>, D> extends RepresentationModel<T> 
        implements DomainToResponseConverter<D, T> {

    @JsonUnwrapped
    private final D domainObject;

    protected BaseResponse(D domainObject) {
        this.domainObject = domainObject;
    }

    @JsonProperty("_links")
    public Object getLinksAsHal() {
        if (!hasLinks()) {
            return null;
        }
        
        Map<String, Object> halLinks = new HashMap<>();
        for (Link link : getLinks()) {
            Map<String, String> linkObject = new HashMap<>();
            linkObject.put("href", link.getHref());
            halLinks.put(link.getRel().value(), linkObject);
        }
        return halLinks;
    }
    
    @JsonProperty(value = "links", access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public @NotNull Links getLinks() {
        return super.getLinks();
    }

    protected D getDomainObject() {
        return domainObject;
    }
}