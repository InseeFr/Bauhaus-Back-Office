package fr.insee.rmes.testcontainers.e2e.operations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * End-to-end tests of the three publication endpoints of the operations module:
 * {@code PUT /api/operations/series/{id}/validate},
 * {@code PUT /api/operations/operation/{id}/validate} and
 * {@code PUT /api/operations/metadataReport/{id}/validate}.
 *
 * <p>The container hosts two repositories: {@code bauhaus-test} (gestion, seeded with the
 * {@code .trig} fixtures) and {@code bauhaus-test-publication}. Keeping them apart is what makes
 * the assertions meaningful — a triple found in the publication repository has really been
 * published, and the gestion side can be checked for the {@code Unpublished → Validated}
 * transition without being polluted by the published copy. The base URIs are aligned with the
 * fixtures ({@code http://bauhaus/} for gestion, {@code http://publication/} for publication) so
 * that a published IRI is immediately recognizable.</p>
 *
 * <p>The three endpoints live in the same class, and not in three classes sharing a base class,
 * because nested classes share the fixtures and the publication repository seeded once by the
 * enclosing {@code @BeforeAll}. Splitting them would mean reloading and republishing everything
 * three times.</p>
 */
@Tag("integration")
class OperationsPublicationE2ETest extends BaseE2ETest {

    private static final String GESTION_SERIES = "http://bauhaus/operations/serie/";
    private static final String GESTION_OPERATION = "http://bauhaus/operations/operation/";
    private static final String GESTION_REPORT = "http://bauhaus/qualite/rapport/";

    private static final String PUBLISHED_FAMILY = "http://publication/operations/famille/";
    private static final String PUBLISHED_SERIES = "http://publication/operations/serie/";
    private static final String PUBLISHED_OPERATION = "http://publication/operations/operation/";
    private static final String PUBLISHED_REPORT = "http://publication/qualite/rapport/";
    private static final String PUBLISHED_ATTRIBUTE = "http://publication/qualite/attribut/";
    private static final String PUBLISHED_SIMS_ATTRIBUTE = "http://publication/qualite/simsv2fr/attribut/";

    private static final String IS_PART_OF = "http://purl.org/dc/terms/isPartOf";
    private static final String HAS_PART = "http://purl.org/dc/terms/hasPart";
    private static final String PUBLISHER = "http://purl.org/dc/terms/publisher";
    private static final String VALIDATION_STATE = "http://rdf.insee.fr/def/base#validationState";
    private static final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    private static final String ALT_LABEL = "http://www.w3.org/2004/02/skos/core#altLabel";
    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String SIMS_TARGET = "http://www.w3.org/ns/sdmx-mm#target";

    /** Paths already published by {@link #publishOnce(String)}, shared by every test of the class. */
    private static final Set<String> alreadyPublished = new HashSet<>();

    @Autowired
    RepositoryPublication repositoryPublication;

    @Autowired
    RepositoryGestion repositoryGestion;

    @DynamicPropertySource
    static void configurePublicationRepository(DynamicPropertyRegistry registry) {
        String graphdbUrl = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        // The fixtures live in the http://bauhaus/ namespace, whereas AppSpringBootTest defaults
        // the gestion base URI to http://.
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.baseURI", () -> "http://bauhaus/");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.baseURI", () -> "http://publication/");
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> graphdbUrl);
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> "bauhaus-test-publication");
    }

    @BeforeAll
    static void initPublicationData() {
        container.withRepository("config-publication.ttl");
        container.withTrigFiles("operations-publication-it.trig");
    }

    /**
     * Business rules of {@code SeriesPublication#publishSeries}.
     */
    @Nested
    class SeriesValidation {

        @Test
        void publishesTheSeriesAndSwitchesItsGestionStateToValidated() throws RmesException {
            assertThat(gestionValidationState(GESTION_SERIES + "s9106"))
                    .as("s9106 is published by this test only, its initial state is observable")
                    .isEqualTo("Unpublished");

            ResponseEntity<String> response = validate("/operations/series/s9106/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("s9106");
            assertThat(gestionValidationState(GESTION_SERIES + "s9106")).isEqualTo("Validated");
            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9106", PREF_LABEL))
                    .contains("Série pour la transition d'état");
        }

        @Test
        void copiesTheLiteralsOfTheSeries() throws RmesException {
            publishOnce("/operations/series/s9101/validate");

            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", PREF_LABEL))
                    .containsExactlyInAnyOrder("Série à publier", "Series to publish");
            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", ALT_LABEL))
                    .containsExactlyInAnyOrder("SAP", "STP");
        }

        @Test
        void doesNotCopyTheValidationStateToThePublicationBase() throws RmesException {
            publishOnce("/operations/series/s9101/validate");

            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", VALIDATION_STATE))
                    .as("the validation state is a gestion-only property")
                    .isEmpty();
        }

        @Test
        void rewritesTheLinksOfTheSeriesToThePublicationNamespace() throws RmesException {
            publishOnce("/operations/series/s9101/validate");

            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", IS_PART_OF))
                    .containsExactly(PUBLISHED_FAMILY + "s9100");
            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", PUBLISHER))
                    .containsExactly("http://publication/organisations/insee/DG75-E001");
            assertThat(publishedObjectsOf(PUBLISHED_FAMILY + "s9100", HAS_PART))
                    .as("the family points at the published series in the publication base")
                    .contains(PUBLISHED_SERIES + "s9101");
        }

        @Test
        void publishesHasPartLinksOnlyTowardsOperationsThatAreThemselvesPublished() throws RmesException {
            publishOnce("/operations/series/s9101/validate");

            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9101", HAS_PART))
                    .as("s9105 is unpublished, only s9102 must appear")
                    .containsExactly(PUBLISHED_OPERATION + "s9102");
        }

        @Test
        void refusesToPublishASeriesWhoseFamilyIsNotPublished() throws RmesException {
            ResponseEntity<String> response = validate("/operations/series/s9111/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code"))
                    .isEqualTo(ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY);
            assertThat(gestionValidationState(GESTION_SERIES + "s9111"))
                    .as("a refused publication must leave the gestion base untouched")
                    .isEqualTo("Unpublished");
            assertThat(publishedTriples(PUBLISHED_SERIES + "s9111")).isEmpty();
        }

        @Test
        void refusesToPublishASeriesThatIsAlreadyPublished() throws RmesException {
            publishOnce("/operations/series/s9101/validate");

            ResponseEntity<String> response = validate("/operations/series/s9101/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code")).isEqualTo(ErrorCodes.ALREADY_PUBLISHED);
            assertThat(gestionValidationState(GESTION_SERIES + "s9101")).isEqualTo("Validated");
        }

        @Test
        void returnsNotFoundForAnUnknownSeries() {
            ResponseEntity<String> response = validate("/operations/series/s9199/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(new JSONObject(response.getBody()).getInt("code")).isEqualTo(ErrorCodes.SERIES_UNKNOWN_ID);
            assertThat(new JSONObject(response.getBody()).getString("message")).isEqualTo("Series not found");
        }
    }

    /**
     * Business rules of {@code OperationPublication#publishOperation}.
     */
    @Nested
    class OperationValidation {

        @Test
        void publishesTheOperationAndSwitchesItsGestionStateToValidated() throws RmesException {
            assertThat(gestionValidationState(GESTION_OPERATION + "s9107"))
                    .as("s9107 is published by this test only, its initial state is observable")
                    .isEqualTo("Unpublished");

            ResponseEntity<String> response = validate("/operations/operation/s9107/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("s9107");
            assertThat(gestionValidationState(GESTION_OPERATION + "s9107")).isEqualTo("Validated");
            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9107", PREF_LABEL))
                    .contains("Opération pour la transition d'état");
        }

        @Test
        void copiesTheLiteralsOfTheOperation() throws RmesException {
            publishOnce("/operations/operation/s9104/validate");

            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9104", PREF_LABEL))
                    .containsExactlyInAnyOrder("Opération à publier", "Operation to publish");
            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9104", ALT_LABEL))
                    .containsExactlyInAnyOrder("OAP", "OTP");
        }

        @Test
        void doesNotCopyTheGestionOnlyProperties() throws RmesException {
            publishOnce("/operations/operation/s9104/validate");

            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9104", VALIDATION_STATE))
                    .isEmpty();
            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9104", PUBLISHER))
                    .as("publisher is carried by the series, it is not copied on the operation")
                    .isEmpty();
        }

        @Test
        void rewritesTheSeriesLinksToThePublicationNamespace() throws RmesException {
            publishOnce("/operations/operation/s9104/validate");

            assertThat(publishedObjectsOf(PUBLISHED_OPERATION + "s9104", IS_PART_OF))
                    .containsExactly(PUBLISHED_SERIES + "s9103");
            assertThat(publishedObjectsOf(PUBLISHED_SERIES + "s9103", HAS_PART))
                    .contains(PUBLISHED_OPERATION + "s9104");
        }

        @Test
        void refusesToPublishAnOperationThatIsAlreadyPublished() throws RmesException {
            publishOnce("/operations/operation/s9104/validate");

            ResponseEntity<String> response = validate("/operations/operation/s9104/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code")).isEqualTo(ErrorCodes.ALREADY_PUBLISHED);
            assertThat(gestionValidationState(GESTION_OPERATION + "s9104")).isEqualTo("Validated");
        }

        @Test
        void refusesToPublishAnOperationWhoseSeriesIsNotPublished() throws RmesException {
            ResponseEntity<String> response = validate("/operations/operation/s9112/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code"))
                    .isEqualTo(ErrorCodes.OPERATION_VALIDATION_UNPUBLISHED_SERIES);
            assertThat(gestionValidationState(GESTION_OPERATION + "s9112"))
                    .as("a refused publication must leave the gestion base untouched")
                    .isEqualTo("Unpublished");
            assertThat(publishedTriples(PUBLISHED_OPERATION + "s9112")).isEmpty();
        }
    }

    /**
     * Business rules of {@code DocumentationsUtils#publishMetadataReport}.
     */
    @Nested
    class MetadataReportValidation {

        @Test
        void publishesTheReportAndSwitchesItsGestionStateToValidated() throws RmesException {
            assertThat(gestionValidationState(GESTION_REPORT + "9204"))
                    .as("9204 is published by this test only, its initial state is observable")
                    .isEqualTo("Unpublished");

            ResponseEntity<String> response = validate("/operations/metadataReport/9204/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("9204");
            assertThat(gestionValidationState(GESTION_REPORT + "9204")).isEqualTo("Validated");
            assertThat(publishedObjectsOf(PUBLISHED_REPORT + "9204", RDFS_LABEL))
                    .contains("Rapport qualité pour la transition d'état");
        }

        @Test
        void rewritesTheTargetOfTheReportToThePublicationNamespace() throws RmesException {
            publishOnce("/operations/metadataReport/9202/validate");

            assertThat(publishedObjectsOf(PUBLISHED_REPORT + "9202", SIMS_TARGET))
                    .containsExactly(PUBLISHED_OPERATION + "s9102");
        }

        @Test
        void publishesThePublishableRubrics() throws RmesException {
            publishOnce("/operations/metadataReport/9202/validate");

            assertThat(publishedObjectsOf(PUBLISHED_ATTRIBUTE + "9202/S.1.1", PUBLISHED_SIMS_ATTRIBUTE + "S.1.1"))
                    .containsExactly("Rubrique publiable");
        }

        @Test
        void filtersOutTheInternalRubricsAndTheValidationState() throws RmesException {
            publishOnce("/operations/metadataReport/9202/validate");

            assertThat(publishedObjectsOf(PUBLISHED_ATTRIBUTE + "9202/S.1.5", PUBLISHED_SIMS_ATTRIBUTE + "S.1.5"))
                    .as("S.1.3 to S.1.8 are internal rubrics, they never reach the publication base")
                    .isEmpty();
            assertThat(publishedObjectsOf(PUBLISHED_REPORT + "9202", VALIDATION_STATE))
                    .isEmpty();
        }

        @Test
        void refusesToPublishAReportThatIsAlreadyPublished() throws RmesException {
            publishOnce("/operations/metadataReport/9202/validate");

            ResponseEntity<String> response = validate("/operations/metadataReport/9202/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code")).isEqualTo(ErrorCodes.ALREADY_PUBLISHED);
            assertThat(gestionValidationState(GESTION_REPORT + "9202")).isEqualTo("Validated");
        }

        @Test
        void refusesToPublishAReportWhoseTargetIsNotPublished() throws RmesException {
            ResponseEntity<String> response = validate("/operations/metadataReport/9203/validate");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(new JSONObject(response.getBody()).getInt("code"))
                    .isEqualTo(ErrorCodes.OPERATION_VALIDATION_UNPUBLISHED_PARENT);
            assertThat(gestionValidationState(GESTION_REPORT + "9203"))
                    .as("a refused publication must leave the gestion base untouched")
                    .isEqualTo("Unpublished");
            assertThat(publishedTriples(PUBLISHED_REPORT + "9203")).isEmpty();
        }
    }

    /**
     * Publishes the resource once for the whole class. Several tests assert different facets of the
     * very same publication ; since republishing an already published resource is now a 400, the
     * publication has to happen exactly once and the tests that follow only read its outcome.
     */
    private void publishOnce(String path) {
        if (alreadyPublished.add(path)) {
            ResponseEntity<String> response = validate(path);
            assertThat(response.getStatusCode())
                    .as("the shared publication of %s must succeed", path)
                    .isEqualTo(HttpStatus.OK);
        }
    }

    private ResponseEntity<String> validate(String path) {
        HttpHeaders headers = new HttpHeaders();
        // Every /validate endpoint declares consumes=application/json.
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(
                "http://localhost:" + port + "/api" + path, HttpMethod.PUT, new HttpEntity<>(headers), String.class);
    }

    /**
     * @return the validation state held by the gestion base for the given IRI, or {@code null}
     * when the resource carries none.
     */
    private String gestionValidationState(String iri) throws RmesException {
        JSONArray rows = repositoryGestion.getResponseAsArray("""
                SELECT ?state WHERE { GRAPH ?g { <%s> <%s> ?state } }
                """.formatted(iri, VALIDATION_STATE));
        return rows.isEmpty() ? null : rows.getJSONObject(0).getString("state");
    }

    private JSONArray publishedTriples(String subjectIri) throws RmesException {
        return repositoryPublication.getResponseAsArray("""
                SELECT ?p ?o WHERE { GRAPH ?g { <%s> ?p ?o } }
                """.formatted(subjectIri));
    }

    /**
     * @return the objects of {@code <subjectIri> <predicateIri> ?o} as found in the publication base.
     */
    private List<String> publishedObjectsOf(String subjectIri, String predicateIri) throws RmesException {
        JSONArray rows = repositoryPublication.getResponseAsArray("""
                SELECT ?o WHERE { GRAPH ?g { <%s> <%s> ?o } }
                """.formatted(subjectIri, predicateIri));
        return JSONUtils.stream(rows).map(row -> row.getString("o")).toList();
    }
}
