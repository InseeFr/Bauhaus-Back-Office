package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PAVTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://purl.org/pav/", PAV.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("pav", PAV.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = PAV.NS;
        assertNotNull(ns);
        assertEquals("pav", ns.getPrefix());
        assertEquals("http://purl.org/pav/", ns.getName());
    }

    @Test
    void shouldHaveValidVersionConstant() {
        assertNotNull(PAV.VERSION);
        assertEquals("http://purl.org/pav/version", PAV.VERSION.toString());
    }

    @Test
    void shouldHaveValidLastRefreshedOnConstant() {
        assertNotNull(PAV.LASTREFRESHEDON);
        assertEquals("http://purl.org/pav/lastRefreshedOn", PAV.LASTREFRESHEDON.toString());
    }


}