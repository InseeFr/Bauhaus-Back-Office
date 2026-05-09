package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GEOTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://www.opengis.net/ont/geosparql#", GEO.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("geo", GEO.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = GEO.NS;
        assertNotNull(ns);
        assertEquals("geo", ns.getPrefix());
        assertEquals("http://www.opengis.net/ont/geosparql#", ns.getName());
    }

    @Test
    void shouldHaveValidFeatureConstant() {
        assertNotNull(GEO.FEATURE);
        assertEquals("http://www.opengis.net/ont/geosparql#Feature", GEO.FEATURE.toString());
    }

    @Test
    void shouldHaveValidDifferenceConstant() {
        assertNotNull(GEO.DIFFERENCE);
        assertEquals("http://www.opengis.net/ont/geosparql#difference", GEO.DIFFERENCE.toString());
    }

    @Test
    void shouldHaveValidUnionConstant() {
        assertNotNull(GEO.UNION);
        assertEquals("http://www.opengis.net/ont/geosparql#union", GEO.UNION.toString());
    }

}