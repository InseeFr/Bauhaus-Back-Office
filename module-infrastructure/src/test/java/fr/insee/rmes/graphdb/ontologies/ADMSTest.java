package fr.insee.rmes.graphdb.ontologies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ADMSTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://www.w3.org/ns/adms#", ADMS.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("adms", ADMS.PREFIX);
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