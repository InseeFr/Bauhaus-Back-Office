package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BasedOnObject(
        @JsonProperty("$type") String type,
        @JsonProperty("BasedOnReference") List<Reference> basedOnReferences) {

    public static final String TYPE = "BasedOnObjectType";

    public static BasedOnObject of(List<Reference> basedOnReferences) {
        return new BasedOnObject(TYPE, basedOnReferences);
    }
}
