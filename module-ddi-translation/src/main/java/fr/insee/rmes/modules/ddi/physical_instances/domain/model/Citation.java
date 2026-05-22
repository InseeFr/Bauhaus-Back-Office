package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;

import java.util.List;

public record Citation(
        @JsonProperty("Title") List<LangString> title
) {
}
