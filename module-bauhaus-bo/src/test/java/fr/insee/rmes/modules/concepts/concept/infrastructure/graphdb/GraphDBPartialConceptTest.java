package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDBPartialConceptTest {

    @Test
    void toDomain_keeps_id_and_label_and_uses_default_language() {
        var row = new GraphDBPartialConcept("c00001", "Mon concept", null);

        var compact = row.toDomain();

        assertThat(compact.id().value()).isEqualTo("c00001");
        assertThat(compact.prefLabel().value()).isEqualTo("Mon concept");
        assertThat(compact.prefLabel().lang()).isEqualTo(Lang.defaultLanguage());
    }

    @Test
    void altLabel_column_is_kept_on_the_record_but_ignored_when_mapping_to_domain() {
        var row = new GraphDBPartialConcept("c00001", "Concept", "altLabel ignored here");

        assertThat(row.altLabel()).isEqualTo("altLabel ignored here");
        assertThat(row.toDomain().prefLabel().value()).isEqualTo("Concept");
    }
}
