package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptVersion;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDBConceptTest {

    private static final String DISSEMINATION_STATUS = "http://id.insee.fr/codes/base/statutDiffusion/Prive";

    @Test
    void toDomain_maps_all_fields() {
        var row = new GraphDBConcept(
                "c00001",
                "Concept FR",
                "Concept EN",
                "HIE000000",
                "HIE000001",
                DISSEMINATION_STATUS,
                "additional material",
                "2026-01-01T10:00:00",
                "2026-01-02T11:00:00",
                "2026-12-31",
                3,
                "true",
                List.of("alt-fr-1", "alt-fr-2"),
                List.of("alt-en-1"),
                List.of("Collection-001")
        );

        Concept concept = row.toDomain();

        assertThat(concept.id().value()).isEqualTo("c00001");
        assertThat(concept.prefLabel().value()).isEqualTo("Concept FR");
        assertThat(concept.prefLabel().lang()).isEqualTo(Lang.FR);
        assertThat(concept.alternativeLabels())
                .hasSize(1)
                .extracting(label -> label.value() + "@" + label.lang())
                .containsExactly("Concept EN@EN");
        assertThat(concept.creator()).isEqualTo("HIE000000");
        assertThat(concept.contributor()).contains("HIE000001");
        assertThat(concept.disseminationStatus()).isEqualTo(DISSEMINATION_STATUS);
        assertThat(concept.created()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        assertThat(concept.modified()).contains(LocalDateTime.of(2026, 1, 2, 11, 0));
        assertThat(concept.isValidated()).isTrue();
        assertThat(concept.version()).isEqualTo(new ConceptVersion(3));
        assertThat(concept.collectionIds()).containsExactly("Collection-001");
    }

    @Test
    void toDomain_handles_null_optional_fields() {
        var row = new GraphDBConcept(
                "c00002",
                "Concept FR",
                null,
                null,
                null,
                null,
                null,
                "2026-01-01T10:00:00",
                null,
                null,
                1,
                "false",
                null,
                null,
                null
        );

        Concept concept = row.toDomain();

        assertThat(concept.alternativeLabels()).isEmpty();
        assertThat(concept.contributor()).isEmpty();
        assertThat(concept.modified()).isEmpty();
        assertThat(concept.isValidated()).isFalse();
        assertThat(concept.collectionIds()).isEmpty();
    }

    @Test
    void toDomain_parses_datetime_with_timezone_offset() {
        var row = new GraphDBConcept(
                "c00003",
                "Concept",
                null, null, null, null, null,
                "2026-01-01T10:00:00.000+01:00",
                "2026-06-01T15:30:00.000+02:00",
                null, 1, "false",
                null, null, null
        );

        Concept concept = row.toDomain();

        assertThat(concept.created()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        assertThat(concept.modified()).contains(LocalDateTime.of(2026, 6, 1, 15, 30));
    }

    @Test
    void toDomain_clamps_conceptVersion_to_one_when_projection_returns_zero() {
        // Defensive: the legacy data has no INSEE.CONCEPT_VERSION literal when no notes were
        // attached at creation time. Jackson reads ?conceptVersion as 0 in that case.
        var row = new GraphDBConcept(
                "c00004",
                "Concept",
                null, null, null, null, null,
                "2026-01-01T10:00:00",
                null, null, 0, "false",
                null, null, null
        );

        Concept concept = row.toDomain();

        assertThat(concept.version().value()).isEqualTo(1);
    }
}
