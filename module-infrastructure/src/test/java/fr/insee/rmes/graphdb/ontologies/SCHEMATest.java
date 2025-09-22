package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SCHEMATest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://schema.org/", SCHEMA.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("schema", SCHEMA.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = SCHEMA.NS;
        assertNotNull(ns);
        assertEquals("schema", ns.getPrefix());
        assertEquals("http://schema.org/", ns.getName());
    }

    @Test
    void shouldHaveValidUrlConstant() {
        assertNotNull(SCHEMA.URL);
        assertTrue(SCHEMA.URL.toString().startsWith("http://schema.org/"));
        assertTrue(SCHEMA.URL.toString().contains("url"));
    }


}