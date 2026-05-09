package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class INSEETest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://rdf.insee.fr/def/base#", INSEE.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("insee", INSEE.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = INSEE.NS;
        assertNotNull(ns);
        assertEquals("insee", ns.getPrefix());
        assertEquals("http://rdf.insee.fr/def/base#", ns.getName());
    }

    @Test
    void shouldHaveValidIRIConstants() {
        assertNotNull(INSEE.LAST_CODE_URI_SEGMENT);
        assertEquals("http://rdf.insee.fr/def/base#lastCodeUriSegment", INSEE.LAST_CODE_URI_SEGMENT.toString());

        assertNotNull(INSEE.DISSEMINATIONSTATUS);
        assertEquals("http://rdf.insee.fr/def/base#disseminationStatus", INSEE.DISSEMINATIONSTATUS.toString());

        assertNotNull(INSEE.ADDITIONALMATERIAL);
        assertEquals("http://rdf.insee.fr/def/base#additionalMaterial", INSEE.ADDITIONALMATERIAL.toString());

        assertNotNull(INSEE.LEGALMATERIAL);
        assertEquals("http://rdf.insee.fr/def/base#legalMaterial", INSEE.LEGALMATERIAL.toString());

        assertNotNull(INSEE.VALIDFROM);
        assertEquals("http://rdf.insee.fr/def/base#validFrom", INSEE.VALIDFROM.toString());

        assertNotNull(INSEE.VALIDUNTIL);
        assertEquals("http://rdf.insee.fr/def/base#validUntil", INSEE.VALIDUNTIL.toString());
    }

    @Test
    void shouldHaveValidClassConstants() {
        assertNotNull(INSEE.FAMILY);
        assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationFamily", INSEE.FAMILY.toString());

        assertNotNull(INSEE.OPERATION);
        assertEquals("http://rdf.insee.fr/def/base#StatisticalOperation", INSEE.OPERATION.toString());

        assertNotNull(INSEE.SERIES);
        assertEquals("http://rdf.insee.fr/def/base#StatisticalOperationSeries", INSEE.SERIES.toString());

        assertNotNull(INSEE.INDICATOR);
        assertEquals("http://rdf.insee.fr/def/base#StatisticalIndicator", INSEE.INDICATOR.toString());
    }

    @Test
    void shouldHaveValidPropertyConstants() {
        assertNotNull(INSEE.DATA_COLLECTOR);
        assertEquals("http://rdf.insee.fr/def/base#dataCollector", INSEE.DATA_COLLECTOR.toString());

        assertNotNull(INSEE.CONCEPT_VERSION);
        assertEquals("http://rdf.insee.fr/def/base#conceptVersion", INSEE.CONCEPT_VERSION.toString());

        assertNotNull(INSEE.IS_VALIDATED);
        assertEquals("http://rdf.insee.fr/def/base#isValidated", INSEE.IS_VALIDATED.toString());

        assertNotNull(INSEE.VALIDATION_STATE);
        assertEquals("http://rdf.insee.fr/def/base#validationState", INSEE.VALIDATION_STATE.toString());

        assertNotNull(INSEE.CODELIST);
        assertEquals("http://rdf.insee.fr/def/base#codeList", INSEE.CODELIST.toString());
    }

    @Test
    void shouldHaveValidAdditionalConstants() {
        assertNotNull(INSEE.SUBTITLE);
        assertEquals("http://rdf.insee.fr/def/base#subtitle", INSEE.SUBTITLE.toString());

        assertNotNull(INSEE.CONFIDENTIALITY_STATUS);
        assertEquals("http://rdf.insee.fr/def/base#confidentialityStatus", INSEE.CONFIDENTIALITY_STATUS.toString());

        assertNotNull(INSEE.PROCESS_STEP);
        assertEquals("http://rdf.insee.fr/def/base#processStep", INSEE.PROCESS_STEP.toString());

        assertNotNull(INSEE.ARCHIVE_UNIT);
        assertEquals("http://rdf.insee.fr/def/base#archiveUnit", INSEE.ARCHIVE_UNIT.toString());

        assertNotNull(INSEE.STATISTICAL_UNIT);
        assertEquals("http://rdf.insee.fr/def/base#statisticalUnit", INSEE.STATISTICAL_UNIT.toString());

        assertNotNull(INSEE.STRUCTURE);
        assertEquals("http://rdf.insee.fr/def/base#structure", INSEE.STRUCTURE.toString());

        assertNotNull(INSEE.NUM_OBSERVATIONS);
        assertEquals("http://rdf.insee.fr/def/base#numObservations", INSEE.NUM_OBSERVATIONS.toString());

        assertNotNull(INSEE.SPATIAL_RESOLUTION);
        assertEquals("http://rdf.insee.fr/def/base#spatialResolution", INSEE.SPATIAL_RESOLUTION.toString());

        assertNotNull(INSEE.SPATIAL_TEMPORAL);
        assertEquals("http://rdf.insee.fr/def/base#spatialTemporal", INSEE.SPATIAL_TEMPORAL.toString());

        assertNotNull(INSEE.RUBRIQUE_SANS_OBJECT);
        assertEquals("http://rdf.insee.fr/def/base#rubriqueSansObjet", INSEE.RUBRIQUE_SANS_OBJECT.toString());
    }

}