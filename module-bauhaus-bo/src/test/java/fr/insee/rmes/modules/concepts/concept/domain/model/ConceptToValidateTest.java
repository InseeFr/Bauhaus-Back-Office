package fr.insee.rmes.modules.concepts.concept.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptToValidateTest {

    @Test
    void exposes_id_label_and_creator() {
        var item = new ConceptToValidate(new ConceptId("c00001"), "Mon concept", "HIE000000");

        assertThat(item.id()).isEqualTo(new ConceptId("c00001"));
        assertThat(item.label()).isEqualTo("Mon concept");
        assertThat(item.creator()).isEqualTo("HIE000000");
    }
}
