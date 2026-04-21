package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Ddi4GroupResponse(
        @JsonProperty("$schema") String schema,
        List<TopLevelReference> topLevelReference,
        @JsonProperty("Group") List<Ddi4Group> group,
        @JsonProperty("StudyUnit") List<Ddi4StudyUnit> studyUnit
) {
}
