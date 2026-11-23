package fr.insee.rmes.testcontainers.e2e.operations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * End-to-end test for the organisation-aware validation on POST /api/operations/series.
 *
 * Covers plan §6 validator-side cases:
 * - POST with an IRI creator unknown to the organisations graph → 400, body lists the IRI,
 * - POST with a known IRI creator → 200,
 * - POST with a non-IRI legacy string that does not resolve → 400.
 */
@Tag("integration")
class SeriesValidatorIntegrationTest extends BaseE2ETest {

    private static final String KNOWN_IRI = "http://bauhaus/organisations/insee/HIE2000069";
    private static final String UNKNOWN_IRI = "http://bauhaus/organisations/insee/DOES_NOT_EXIST";

    @DynamicPropertySource
    static void alignBaseUriWithFixture(DynamicPropertyRegistry registry) {
        // The shared fixtures (all-operations-and-indicators.trig, organizations.trig) use the
        // http://bauhaus/ namespace, but AppSpringBootTest defaults baseURI to http://. The series
        // creation path checks the family via an exact-IRI ASK, so the URIs must agree.
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.baseURI", () -> "http://bauhaus/");
    }

    @BeforeAll
    static void seedFixtures() {
        container.withTrigFiles("all-operations-and-indicators.trig");
        container.withTrigFiles("organizations.trig");
    }

    @Test
    void postSeries_returnsSuccess_whenCreatorIsAKnownIri() {
        var response = postSeries(buildSeriesJson("Série E2E IRI connue", KNOWN_IRI));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postSeries_returnsBadRequest_whenCreatorIsAnUnknownIri() {
        var response = postSeries(buildSeriesJson("Série E2E IRI inconnue", UNKNOWN_IRI));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getResponseBody())
                .as("the failing IRI should appear in the response body for debug")
                .contains(UNKNOWN_IRI);
    }

    @Test
    void postSeries_returnsBadRequest_whenCreatorIsAnUnresolvableLegacyString() {
        String legacy = "JUNK_NOT_A_STAMP";

        var response = postSeries(buildSeriesJson("Série E2E littéral inconnu", legacy));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getResponseBody()).contains(legacy);
    }

    private EntityExchangeResult<String> postSeries(String body) {
        return restTestClient
                .post()
                .uri("http://localhost:" + port + "/api/operations/series")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange()
                .returnResult(String.class);
    }

    private static String buildSeriesJson(String label, String creatorIri) {
        // Minimal Series payload: family + bilingual prefLabel + a single creator.
        // The family s60 is provided by all-operations-and-indicators.trig.
        return """
                {
                  "family": {"id": "s60"},
                  "prefLabelLg1": "%s",
                  "prefLabelLg2": "%s EN",
                  "creators": ["%s"]
                }
                """.formatted(label, label, creatorIri);
    }
}
