package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BasedOnObject(
        @JsonProperty("$type") String type,
        @JsonProperty("BasedOnReference") List<Reference> basedOnReferences
) {

    public static final String TYPE = "BasedOnObjectType";

    public static BasedOnObject of(List<Reference> basedOnReferences) {
        return new BasedOnObject(TYPE, basedOnReferences);
    }
}
