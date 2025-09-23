package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DCMITYPETest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://purl.org/dc/dcmitype/", DCMITYPE.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("dcmitype", DCMITYPE.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = DCMITYPE.NS;
        assertNotNull(ns);
        assertEquals("dcmitype", ns.getPrefix());
        assertEquals("http://purl.org/dc/dcmitype/", ns.getName());
    }

    @Test
    void shouldHaveValidTextConstant() {
        assertNotNull(DCMITYPE.TEXT);
        assertEquals("http://purl.org/dc/dcmitype/Text", DCMITYPE.TEXT.toString());
    }

}