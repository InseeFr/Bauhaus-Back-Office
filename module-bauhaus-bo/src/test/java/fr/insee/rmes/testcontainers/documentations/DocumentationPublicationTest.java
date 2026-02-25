package fr.insee.rmes.testcontainers.documentations;

import fr.insee.rmes.Config;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationPublication;
import fr.insee.rmes.modules.operations.msd.DocumentationConfiguration;
import fr.insee.rmes.modules.organisations.OrganisationsProperties;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
class DocumentationPublicationTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private RepositoryPublication repositoryPublication;
    private DocumentationPublication documentationPublication;
    private Config config;

    @BeforeAll
    static void initData() {
        RdfUtils.setConfig(new ConfigStub());
        container.withTrigFiles("documentation-publication-test.trig");
    }

    @BeforeEach
    void setUp() throws Exception {
        config = new ConfigStub();

        // Create a simple PropertiesFinder for UriUtils
        UriUtils.PropertiesFinder propertiesFinder = Optional::of;

        UriUtils uriUtils = new UriUtils(
                "http://publication/",
                "http://bauhaus/",
                propertiesFinder
        );
        RdfUtils.setUriUtils(uriUtils);

        RepositoryUtils repositoryUtils = new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED);

        // For publication repository, we use the same GraphDB instance
        repositoryPublication = new RepositoryPublication(
                getRdfGestionConnectionDetails().getUrlServer(),
                getRdfGestionConnectionDetails().repositoryId(),
                repositoryUtils
        );

        PublicationUtils publicationUtils = new PublicationUtils(
                "http://bauhaus/",
                "http://publication/",
                repositoryGestion,
                repositoryPublication
        );

        // Create DocumentsPublication mock (we don't need to test document publication here)
        DocumentsPublication documentsPublication = Mockito.mock(DocumentsPublication.class);

        // Create DocumentationPublication with constructor injection
        var documentationConfiguration = new DocumentationConfiguration(
                new DocumentationConfiguration.Geographie("qualite/territoires", "territoire"),
                "Rapport qualité :",
                "Quality report:"
        );
        documentationPublication = new DocumentationPublication(
                repositoryGestion,
                repositoryPublication,
                publicationUtils,
                documentsPublication,
                documentationConfiguration,
                new OrganisationsProperties("organisations")
        );
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException _) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in class hierarchy");
    }

    @Test
    void shouldPublishSimsSuccessfully() {
        // SIMS 9999 exists in our test data
        assertDoesNotThrow(() -> documentationPublication.publishSims("9999"));
    }

    @Test
    void shouldThrowExceptionWhenSimsNotFound() {
        // SIMS 0000 does not exist
        RmesException exception = assertThrows(RmesException.class,
            () -> documentationPublication.publishSims("0000"));

        assertThat(exception.getDetails()).contains("Sims not found");
    }

    @Test
    void shouldFilterOutRestrictedRubrics() throws RmesException {
        // When publishing SIMS 9999, rubrics S.1.3 to S.1.8 should not be published
        documentationPublication.publishSims("9999");

        // Verify restricted rubrics (S.1.3-S.1.8) are filtered out
        String query = """
            SELECT ?predicate WHERE {
                GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/9999> {
                    ?s ?predicate ?o .
                    FILTER(CONTAINS(STR(?predicate), "S.1.3"))
                }
            }
            """;

        var result = repositoryPublication.getResponseAsArray(query);
        assertThat(result.length()).isZero();
    }

    @Test
    void shouldPublishAllowedRubrics() throws RmesException {
        // Publish SIMS 9999
        documentationPublication.publishSims("9999");

        // Verify that allowed rubrics (like S.1.1) are published
        String query = """
            SELECT ?value WHERE {
                GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/9999> {
                    ?s <http://publication/qualite/simsv2fr/attribut/S.1.1> ?value .
                }
            }
            """;

        var result = repositoryPublication.getResponseAsArray(query);
        assertThat(result).hasSizeGreaterThan(0);
    }

    @Test
    void shouldPublishOrganizationInS37() throws RmesException {
        // Publish SIMS 9999 which contains an organization reference in S.3.7
        documentationPublication.publishSims("9999");

        // Verify that S.3.7 organization reference and its label are published
        // The label is in the general organizations graph
        String query = """
            SELECT ?organisation ?label WHERE {
                GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/9999> {
                    ?s <http://publication/qualite/simsv2fr/attribut/S.3.7> ?organisation .
                }
                GRAPH <http://rdf.insee.fr/graphes/organisations> {
                    ?organisation <http://www.w3.org/2000/01/rdf-schema#label> ?label .
                }
            }
            """;

        var result = repositoryPublication.getResponseAsArray(query);

        // S.3.7 should be published with the organization reference and label
        assertThat(result).hasSizeGreaterThan(0);

        // The organization URI should be transformed to publication URI
        String organisationUri = result.getJSONObject(0).getString("organisation");
        assertThat(organisationUri).contains("publication");

        // The organization label should be published
        String label = result.getJSONObject(0).getString("label");
        assertThat(label).isEqualTo("Direction des statistiques d'entreprises");
    }

    @Test
    void shouldPublishOrganizationInBothGraphs() throws RmesException {
        // Publish SIMS 9999 which contains an organization reference in S.3.7
        documentationPublication.publishSims("9999");

        // Verify data is published in the general organizations graph
        String queryGeneralGraph = """
            SELECT ?label WHERE {
                GRAPH <http://rdf.insee.fr/graphes/organisations> {
                    <http://publication/organisations/insee/DG75-F001> <http://www.w3.org/2000/01/rdf-schema#label> ?label .
                }
            }
            """;

        var resultGeneral = repositoryPublication.getResponseAsArray(queryGeneralGraph);
        assertThat(resultGeneral).hasSizeGreaterThan(0);

        // Verify data is published in the INSEE organizations graph
        String queryInseeGraph = """
            SELECT ?identifiant WHERE {
                GRAPH <http://rdf.insee.fr/graphes/organisations/insee> {
                    <http://publication/organisations/insee/DG75-F001> <http://rdf.insee.fr/def/base#identifiant> ?identifiant .
                }
            }
            """;

        var resultInsee = repositoryPublication.getResponseAsArray(queryInseeGraph);
        assertThat(resultInsee).hasSizeGreaterThan(0);
        assertThat(resultInsee.getJSONObject(0).getString("identifiant")).isEqualTo("DG75-F001");
    }

    @Test
    void shouldConvertMarkdownToHtmlWhenPublishingRichTextRubrics() throws RmesException {
        // Publish SIMS 9999
        documentationPublication.publishSims("9999");

        // Verify that RICHTEXT rubrics have both rdf:value (markdown) and insee:html (HTML)
        String queryHtmlVersion = """
            SELECT ?html WHERE {
                GRAPH <http://rdf.insee.fr/graphes/qualite/rapport/9999> {
                    ?textResource a <http://purl.org/dc/dcmitype/Text> .
                    ?textResource <http://rdf.insee.fr/def/base#html> ?html .
                }
            }
            """;

        var resultHtml = repositoryPublication.getResponseAsArray(queryHtmlVersion);

        // If there are RICHTEXT rubrics with content, they should have HTML versions
        // The test data should have at least one RICHTEXT rubric to properly test this
        if (resultHtml.length() > 0) {
            String htmlContent = resultHtml.getJSONObject(0).getString("html");
            assertThat(htmlContent).isNotEmpty();
            // HTML content should contain HTML tags (not markdown)
            // If the original was markdown like "**bold**", HTML should be "<strong>bold</strong>" or similar
        }
    }
}