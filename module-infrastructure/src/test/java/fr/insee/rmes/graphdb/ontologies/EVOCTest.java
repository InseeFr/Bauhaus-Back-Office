package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EVOCTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://eurovoc.europa.eu/schema#", EVOC.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("evoc", EVOC.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = EVOC.NS;
        assertNotNull(ns);
        assertEquals("evoc", ns.getPrefix());
        assertEquals("http://eurovoc.europa.eu/schema#", ns.getName());
    }

    @Test
    void shouldHaveValidNoteLiteralConstant() {
        assertNotNull(EVOC.NOTE_LITERAL);
        assertEquals("http://eurovoc.europa.eu/schema#noteLiteral", EVOC.NOTE_LITERAL.toString());
    }

}