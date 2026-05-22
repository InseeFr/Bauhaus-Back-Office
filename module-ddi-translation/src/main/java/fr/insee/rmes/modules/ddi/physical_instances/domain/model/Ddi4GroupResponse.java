package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import java.util.List;

public record Ddi4GroupResponse(
        @JsonProperty("$schema") String schema,
        List<TopLevelReference> topLevelReference,
        @JsonProperty("Group") List<Group> group,
        @JsonProperty("StudyUnit") List<StudyUnit> studyUnit
) {
}
