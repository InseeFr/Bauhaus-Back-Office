package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDBConceptToValidateTest {

    @Test
    void toDomain_carries_id_label_and_creator() {
        var row = new GraphDBConceptToValidate("c00001", "Concept", "HIE000000", null);

        var domain = row.toDomain();

        assertThat(domain.id().value()).isEqualTo("c00001");
        assertThat(domain.label()).isEqualTo("Concept");
        assertThat(domain.creator()).isEqualTo("HIE000000");
    }

    @Test
    void valid_column_is_preserved_on_the_record_even_though_domain_ignores_it() {
        var row = new GraphDBConceptToValidate("c00001", "Concept", "HIE000000", "2026-12-31");
        assertThat(row.valid()).isEqualTo("2026-12-31");
    }
}
