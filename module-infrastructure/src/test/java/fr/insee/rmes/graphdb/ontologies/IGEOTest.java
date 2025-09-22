package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IGEOTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://rdf.insee.fr/def/geo#", IGEO.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("igeo", IGEO.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = IGEO.NS;
        assertNotNull(ns);
        assertEquals("igeo", ns.getPrefix());
        assertEquals("http://rdf.insee.fr/def/geo#", ns.getName());
    }

    @Test
    void shouldHaveValidNomConstant() {
        assertNotNull(IGEO.NOM);
        assertEquals("http://rdf.insee.fr/def/geo#nom", IGEO.NOM.toString());
    }

    @Test
    void shouldHaveValidCodeInseeConstant() {
        assertNotNull(IGEO.CODE_INSEE);
        assertEquals("http://rdf.insee.fr/def/geo#codeINSEE", IGEO.CODE_INSEE.toString());
    }

    @Test
    void shouldHaveValidTerritoireStatistiqueConstant() {
        assertNotNull(IGEO.TERRITOIRE_STATISTIQUE);
        assertEquals("http://rdf.insee.fr/def/geo#TerritoireStatistique", IGEO.TERRITOIRE_STATISTIQUE.toString());
    }

}