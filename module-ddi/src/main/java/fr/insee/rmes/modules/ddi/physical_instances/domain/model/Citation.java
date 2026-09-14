package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DDI 3.3 {@code r:Citation}: the title of an item, possibly in several languages, plus its
 * alternate titles (the short labels — {@code skos:altLabel} on the RDF side).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Citation(
        @JsonProperty("Title") List<LangString> title,
        @JsonProperty("AlternateTitle") List<LangString> alternateTitle) {

    /** A citation carrying a title only, without any alternate title. */
    public Citation(List<LangString> title) {
        this(title, null);
    }
}
