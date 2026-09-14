package fr.insee.rmes.modules.concepts.concept.webservice.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptVersion;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class ConceptResponseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void fromDomain_serialises_all_fields_in_the_legacy_flat_shape() throws Exception {
        Concept concept = new Concept(
                new ConceptId("c00001"),
                List.of(
                        LocalisedLabel.ofDefaultLanguage("Concept FR"),
                        LocalisedLabel.ofAlternativeLanguage("Concept EN")),
                "http://bauhaus/HIE000000",
                "http://bauhaus/HIE000001",
                "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 2, 2, 11, 0),
                ValidationStatus.UNPUBLISHED,
                new ConceptVersion(2),
                List.of("Collection-001"));

        ConceptResponse response = ConceptResponse.fromDomain(concept);
        JSONObject json = new JSONObject(MAPPER.writeValueAsString(response));

        // A.0 asserts these fields by name — keep the flat legacy layout.
        assertThat(json.getString("id")).isEqualTo("c00001");
        assertThat(json.getString("prefLabelLg1")).isEqualTo("Concept FR");
        assertThat(json.getString("prefLabelLg2")).isEqualTo("Concept EN");
        assertThat(json.getString("creator")).isEqualTo("http://bauhaus/HIE000000");
        assertThat(json.getString("contributor")).isEqualTo("http://bauhaus/HIE000001");
        assertThat(json.getString("disseminationStatus"))
                .isEqualTo("http://id.insee.fr/codes/base/statutDiffusion/Prive");
        // validationState is the 3-state string, aligned with the other objects.
        assertThat(json.getString("validationState")).isEqualTo("Unpublished");
        assertThat(json.has("created")).isTrue();
        assertThat(json.getJSONArray("collections").length()).isEqualTo(1);
        assertThat(json.getInt("conceptVersion")).isEqualTo(2);
    }

    @Test
    void null_optional_fields_are_omitted_from_the_response() throws Exception {
        Concept concept = new Concept(
                new ConceptId("c00002"),
                List.of(LocalisedLabel.ofDefaultLanguage("Concept FR")),
                "http://bauhaus/HIE000000",
                null, // contributor
                "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null, // modified
                ValidationStatus.UNPUBLISHED,
                ConceptVersion.initial(),
                Collections.emptyList());

        ConceptResponse response = ConceptResponse.fromDomain(concept);
        JSONObject json = new JSONObject(MAPPER.writeValueAsString(response));

        // NON_NULL Jackson include strips optional empty fields, matching the legacy SPARQL projection.
        assertThat(json.has("prefLabelLg2")).isFalse();
        assertThat(json.has("contributor")).isFalse();
        assertThat(json.has("modified")).isFalse();
        assertThat(json.has("additionalMaterial")).isFalse();
        assertThat(json.has("valid")).isFalse();
        // collections is always present even when empty (matches the legacy enrichment in
        // LegacyConceptsRepository.getConceptById).
        assertThat(json.has("collections")).isTrue();
        assertThat(json.getJSONArray("collections").length()).isZero();
    }
}
