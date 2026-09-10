package fr.insee.rmes.modules.concepts.concept.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

class CompactConceptTest {

    @Test
    void exposes_id_and_pref_label() {
        var id = new ConceptId("c00001");
        var label = new LocalisedLabel("Mon concept", Lang.FR);

        var compact = new CompactConcept(id, label);

        assertThat(compact.id()).isEqualTo(id);
        assertThat(compact.prefLabel()).isEqualTo(label);
    }
}
