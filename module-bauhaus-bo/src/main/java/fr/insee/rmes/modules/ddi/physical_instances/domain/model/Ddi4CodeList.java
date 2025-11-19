package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Ddi4CodeList(
        @JsonProperty("@isUniversallyUnique") String isUniversallyUnique,
        @JsonProperty("@versionDate") String versionDate,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version,
        @JsonProperty("Label") Label label,
        @JsonProperty("Code") List<Code> code
) {
}