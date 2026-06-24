package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4CodeListResponse(
        @JsonProperty("$schema") String schema,
        @JsonProperty("TopLevelReference") List<Reference> topLevelReference,
        @JsonProperty("CodeList") List<Ddi4CodeList> codeList
) {
}
