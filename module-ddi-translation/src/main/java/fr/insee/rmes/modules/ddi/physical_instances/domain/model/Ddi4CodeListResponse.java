package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;

import java.util.List;

public record Ddi4CodeListResponse(
        @JsonProperty("$schema") String schema,
        List<TopLevelReference> topLevelReference,
        @JsonProperty("CodeList") List<CodeList> codeList
) {
}
