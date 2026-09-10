package fr.insee.rmes.modules.concepts.concept.webservice.response;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;
import org.junit.jupiter.api.Test;

class ConceptToValidateResponseTest {

    @Test
    void fromDomain_maps_id_label_and_creator() {
        var domain = new ConceptToValidate(new ConceptId("c00001"), "Concept", "HIE000000");

        var response = ConceptToValidateResponse.fromDomain(domain);

        assertThat(response.id()).isEqualTo("c00001");
        assertThat(response.label()).isEqualTo("Concept");
        assertThat(response.creator()).isEqualTo("HIE000000");
    }
}
