package fr.insee.rmes.graphdb.ontologies;

import org.eclipse.rdf4j.model.Namespace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SDMX_MMTest {

    @Test
    void shouldHaveCorrectNamespace() {
        assertEquals("http://www.w3.org/ns/sdmx-mm#", SDMX_MM.NAMESPACE);
    }

    @Test
    void shouldHaveCorrectPrefix() {
        assertEquals("sdmx-mm", SDMX_MM.PREFIX);
    }

    @Test
    void shouldHaveValidNamespaceObject() {
        Namespace ns = SDMX_MM.NS;
        assertNotNull(ns);
        assertEquals("sdmx-mm", ns.getPrefix());
        assertEquals("http://www.w3.org/ns/sdmx-mm#", ns.getName());
    }

    @Test
    void shouldHaveValidReportedAttributeConstant() {
        assertNotNull(SDMX_MM.REPORTED_ATTRIBUTE);
        assertEquals("http://www.w3.org/ns/sdmx-mm#ReportedAttribute", SDMX_MM.REPORTED_ATTRIBUTE.toString());
    }

    @Test
    void shouldHaveValidMetadataReportConstant() {
        assertNotNull(SDMX_MM.METADATA_REPORT);
        assertEquals("http://www.w3.org/ns/sdmx-mm#MetadataReport", SDMX_MM.METADATA_REPORT.toString());
    }

    @Test
    void shouldHaveValidMetadataReportPredicateConstant() {
        assertNotNull(SDMX_MM.METADATA_REPORT_PREDICATE);
        assertEquals("http://www.w3.org/ns/sdmx-mm#metadataReport", SDMX_MM.METADATA_REPORT_PREDICATE.toString());
    }

    @Test
    void shouldHaveValidTargetConstant() {
        assertNotNull(SDMX_MM.TARGET);
        assertEquals("http://www.w3.org/ns/sdmx-mm#target", SDMX_MM.TARGET.toString());
    }

}