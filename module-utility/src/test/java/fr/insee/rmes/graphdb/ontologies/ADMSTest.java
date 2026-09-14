package fr.insee.rmes.graphdb.ontologies;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ADMSTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://www.w3.org/ns/adms#", ADMS.NAMESPACE);
    }

    @Test
    void shouldHaveValidIdentifierConstant() {
        assertNotNull(ADMS.IDENTIFIER);
        assertEquals("http://www.w3.org/ns/adms#Identifier", ADMS.IDENTIFIER.toString());
    }

    @Test
    void shouldHaveValidHasIdentifierConstant() {
        assertNotNull(ADMS.HAS_IDENTIFIER);
        assertEquals("http://www.w3.org/ns/adms#identifier", ADMS.HAS_IDENTIFIER.toString());
    }
}
