package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DCTERMSTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://purl.org/dc/terms/", DCTERMS.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("dcterms", DCTERMS.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = DCTERMS.NS;
        assertNotNull(ns);
        assertEquals("dcterms", ns.getPrefix());
        assertEquals("http://purl.org/dc/terms/", ns.getName());
    }

    @Test
    void shouldHaveValidHasPartConstant() {
        assertNotNull(DCTERMS.HAS_PART);
        assertEquals("http://purl.org/dc/terms/hasPart", DCTERMS.HAS_PART.toString());
    }

}