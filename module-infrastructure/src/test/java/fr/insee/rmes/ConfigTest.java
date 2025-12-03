package fr.insee.rmes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {Config.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.lg1=fr",
        "fr.insee.rmes.bauhaus.lg2=en",
        "fr.insee.rmes.bauhaus.baseGraph=http://test.graph/",
        "fr.insee.rmes.bauhaus.per_page=20",
        "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://test.gestion/",
        "fr.insee.rmes.bauhaus.concepts.graph=/concepts",
        "fr.insee.rmes.bauhaus.concepts.scheme=http://test.scheme/",
        "fr.insee.rmes.bauhaus.classifications.families.graph=/classifications",
        "fr.insee.rmes.bauhaus.operations.graph=/operations",
        "fr.insee.rmes.bauhaus.documentations.graph=/docs",
        "fr.insee.rmes.bauhaus.documentations.msd.graph=/msd",
        "fr.insee.rmes.bauhaus.documentations.concepts.graph=/msd-concepts",
        "fr.insee.rmes.bauhaus.documentation.geographie.graph=/geo-docs",
        "fr.insee.rmes.bauhaus.documentation.titlePrefixLg1=Rapport de qualité - ",
        "fr.insee.rmes.bauhaus.documentation.titlePrefixLg2=Quality report - ",
        "fr.insee.rmes.bauhaus.links.baseURI=http://test.links/",
        "fr.insee.rmes.bauhaus.documents.graph=/documents",
        "fr.insee.rmes.bauhaus.storage.document.gestion=/storage/gestion",
        "fr.insee.web4g.baseURL=http://web4g.test/",
        "fr.insee.rmes.bauhaus.products.graph=/products",
        "fr.insee.rmes.bauhaus.products.baseURI=http://test.products/",
        "fr.insee.rmes.bauhaus.structures.graph=/structures",
        "fr.insee.rmes.bauhaus.structures.components.graph=/components",
        "fr.insee.rmes.bauhaus.codelists.graph=/codelists",
        "fr.insee.rmes.bauhaus.organisations.graph=/orgs",
        "fr.insee.rmes.bauhaus.insee.graph=/insee",
        "fr.insee.rmes.bauhaus.geographie.graph=/geo",
        "fr.insee.rmes.bauhaus.codeList.baseURI=http://test.codelist/",
        "fr.insee.rmes.bauhaus.documents.baseURI=http://test.documents/"
})
class ConfigTest {

    @Autowired
    private Config config;

    @Test
    void shouldReturnCorrectLanguages() {
        assertEquals("fr", config.getLg1());
        assertEquals("en", config.getLg2());
    }

    @Test
    void shouldReturnCorrectBaseGraph() {
        assertEquals("http://test.graph/", config.getBaseGraph());
    }

    @Test
    void shouldReturnCorrectPerPage() {
        assertEquals(20, config.getPerPage());
    }

    @Test
    void shouldReturnCorrectBaseUriGestion() {
        assertEquals("http://test.gestion/", config.getBaseUriGestion());
    }

    @Test
    void shouldReturnCorrectConceptsConfiguration() {
        assertEquals("http://test.graph//concepts", config.getConceptsGraph());
        assertEquals("http://test.scheme/", config.getConceptsScheme());
    }

    @Test
    void shouldReturnCorrectClassificationGraph() {
        assertEquals("http://test.graph//classifications", config.getClassifFamiliesGraph());
    }

    @Test
    void shouldReturnCorrectOperationsGraph() {
        assertEquals("http://test.graph//operations", config.getOperationsGraph());
    }

    @Test
    void shouldReturnCorrectDocumentationGraphs() {
        assertEquals("http://test.graph//docs", config.getDocumentationsGraph());
        assertEquals("http://test.graph//msd", config.getMsdGraph());
        assertEquals("http://test.graph//msd-concepts", config.getMsdConceptsGraph());
        assertEquals("http://test.graph//geo-docs", config.getDocumentationsGeoGraph());
    }

    @Test
    void shouldReturnCorrectDocumentationTitlePrefixes() {
        assertEquals("Rapport de qualité - ", config.getDocumentationsTitlePrefixLg1());
        assertEquals("Quality report - ", config.getDocumentationsTitlePrefixLg2());
    }

    @Test
    void shouldReturnCorrectProductsConfiguration() {
        assertEquals("http://test.graph//products", config.getProductsGraph());
        assertEquals("http://test.products/", config.getProductsBaseUri());
    }

    @Test
    void shouldReturnCorrectStructuresConfiguration() {
        assertEquals("http://test.graph//structures", config.getStructuresGraph());
        assertEquals("http://test.graph//components", config.getStructuresComponentsGraph());
    }

    @Test
    void shouldReturnCorrectCodeListConfiguration() {
        assertEquals("http://test.graph//codelists", config.getCodeListGraph());
        assertEquals("http://test.codelist/", config.getCodeListBaseUri());
    }

    @Test
    void shouldReturnCorrectOrganizationsConfiguration() {
        assertEquals("http://test.graph//orgs", config.getOrganizationsGraph());
        assertEquals("http://test.graph//insee", config.getOrgInseeGraph());
    }

    @Test
    void shouldReturnCorrectGeographyGraph() {
        assertEquals("http://test.graph//geo", config.getGeographyGraph());
    }

    @Test
    void shouldReturnCorrectBaseUris() {
        assertEquals("http://test.documents/", config.getDocumentsBaseUri());
    }

    @Test
    void shouldTrimDocumentsBaseUrl() {
        assertEquals("http://web4g.test/", config.getDocumentsBaseurl().trim());
    }
}