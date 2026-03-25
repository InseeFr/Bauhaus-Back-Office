package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionToValidateResponseTest {

    @Test
    void should_convert_from_domain() {
        var domain = new CollectionToValidate(new CollectionId("c1000"), "Label FR", "creator1");

        var response = CollectionToValidateResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c1000", response.id());
        assertEquals("Label FR", response.label());
        assertEquals("creator1", response.creator());
    }
}