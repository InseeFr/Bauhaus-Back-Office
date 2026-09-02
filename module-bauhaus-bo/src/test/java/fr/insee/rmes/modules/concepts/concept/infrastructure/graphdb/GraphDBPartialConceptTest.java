package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDBPartialConceptTest {

    @Test
    void toDomain_keeps_id_and_label_and_uses_default_language() {
        var row = new GraphDBPartialConcept("c00001", "Mon concept", null);

        var partial = row.toDomain();

        assertThat(partial.id().value()).isEqualTo("c00001");
        assertThat(partial.defaultLabel().value()).isEqualTo("Mon concept");
        assertThat(partial.defaultLabel().lang()).isEqualTo(Lang.defaultLanguage());
    }

    @Test
    void toDomain_keeps_the_alternative_label() {
        var row = new GraphDBPartialConcept("c00001", "Concept", "RNIPP");

        var partial = row.toDomain();

        assertThat(partial.alternativeLabel().value()).isEqualTo("RNIPP");
        assertThat(partial.alternativeLabel().lang()).isEqualTo(Lang.defaultLanguage());
    }

    @Test
    void toDomain_leaves_the_alternative_label_null_when_the_row_has_none() {
        var row = new GraphDBPartialConcept("c00001", "Concept", null);

        assertThat(row.toDomain().alternativeLabel()).isNull();
    }

    @Test
    void mergeAltLabelOf_concatenates_the_alternative_labels_of_two_rows_of_the_same_concept() {
        var first = new GraphDBPartialConcept("c00001", "Concept", "RNIPP");
        var second = new GraphDBPartialConcept("c00001", "Concept", "Sirene");

        assertThat(first.mergeAltLabelOf(second).altLabel()).isEqualTo("RNIPP || Sirene");
    }

    @Test
    void mergeAltLabelOf_ignores_a_row_without_alternative_label() {
        var first = new GraphDBPartialConcept("c00001", "Concept", "RNIPP");
        var second = new GraphDBPartialConcept("c00001", "Concept", null);

        assertThat(first.mergeAltLabelOf(second).altLabel()).isEqualTo("RNIPP");
        assertThat(second.mergeAltLabelOf(first).altLabel()).isEqualTo("RNIPP");
    }
}
