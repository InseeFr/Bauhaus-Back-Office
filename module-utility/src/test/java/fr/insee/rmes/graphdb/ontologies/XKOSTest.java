package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XKOSTest {

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = XKOS.NS;
        assertNotNull(ns);
        assertEquals("xkos", ns.getPrefix());
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#", ns.getName());
    }

    @Test
    void shouldHaveValidClassConstants() {
        assertNotNull(XKOS.CLASSIFICATION_LEVEL);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#ClassificationLevel", XKOS.CLASSIFICATION_LEVEL.toString());

        assertNotNull(XKOS.CONCEPT_ASSOCIATION);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#ConceptAssociation", XKOS.CONCEPT_ASSOCIATION.toString());

        assertNotNull(XKOS.CORRESPONDENCE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#Correspondence", XKOS.CORRESPONDENCE.toString());

        assertNotNull(XKOS.EXPLANATORY_NOTE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#ExplanatoryNote", XKOS.EXPLANATORY_NOTE.toString());
    }

    @Test
    void shouldHaveValidPropertyConstants() {
        assertNotNull(XKOS.CASE_LAW);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#caseLaw", XKOS.CASE_LAW.toString());

        assertNotNull(XKOS.BELONGS_TO);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#belongsTo", XKOS.BELONGS_TO.toString());

        assertNotNull(XKOS.MAX_LENGTH);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#maxLength", XKOS.MAX_LENGTH.toString());
    }

    @Test
    void shouldHaveValidNoteConstants() {
        assertNotNull(XKOS.CORE_CONTENT_NOTE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#coreContentNote", XKOS.CORE_CONTENT_NOTE.toString());

        assertNotNull(XKOS.ADDITIONAL_CONTENT_NOTE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#additionalContentNote", XKOS.ADDITIONAL_CONTENT_NOTE.toString());

        assertNotNull(XKOS.EXCLUSION_NOTE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#exclusionNote", XKOS.EXCLUSION_NOTE.toString());

        assertNotNull(XKOS.PLAIN_TEXT);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#plainText", XKOS.PLAIN_TEXT.toString());
    }

    @Test
    void shouldHaveValidOrganizationConstants() {
        assertNotNull(XKOS.ORGANISED_BY);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#organisedBy", XKOS.ORGANISED_BY.toString());

        assertNotNull(XKOS.MADE_OF);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#madeOf", XKOS.MADE_OF.toString());
    }

    @Test
    void shouldHaveValidConceptConstants() {
        assertNotNull(XKOS.SOURCE_CONCEPT);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#sourceConcept", XKOS.SOURCE_CONCEPT.toString());

        assertNotNull(XKOS.TARGET_CONCEPT);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#targetConcept", XKOS.TARGET_CONCEPT.toString());

        assertNotNull(XKOS.COMPARES);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#compares", XKOS.COMPARES.toString());
    }

    @Test
    void shouldHaveValidTemporalConstants() {
        assertNotNull(XKOS.VARIANT);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#variant", XKOS.VARIANT.toString());

        assertNotNull(XKOS.BEFORE);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#before", XKOS.BEFORE.toString());

        assertNotNull(XKOS.AFTER);
        assertEquals("http://rdf-vocabulary.ddialliance.org/xkos#after", XKOS.AFTER.toString());
    }

}