package fr.insee.rmes.testcontainers.e2e.operations;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Contrat de non-régression (Phase 0 de docs/migration-indicateur-hexagonal.md) :
 * ces tests décrivent le comportement actuel du code legacy (IndicatorsUtils/OperationsService)
 * et doivent continuer à passer une fois la migration hexagonale terminée.
 */
class IndicatorsResourcesE2ETest extends BaseE2ETest {

    @DynamicPropertySource
    static void configurePublicationProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.sesameServer", () -> getRdfGestionConnectionDetails().getUrlServer());
        registry.add("fr.insee.rmes.bauhaus.sesame.publication.repository", () -> getRdfGestionConnectionDetails().repositoryId());
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(body, headers);
    }

    @Test
    void testGetIndicators_listSortedWithSelfLinks() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicators",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode indicators = objectMapper.readTree(response.getBody());
        Assertions.assertTrue(indicators.isArray());
        Assertions.assertTrue(indicators.size() > 0);

        List<String> ids = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (JsonNode indicator : indicators) {
            Assertions.assertTrue(indicator.has("id"));
            Assertions.assertTrue(indicator.has("label"));
            String id = indicator.get("id").asText();
            ids.add(id);
            labels.add(indicator.get("label").asText());

            String selfHref = indicator.get("_links").get("self").get("href").asText();
            Assertions.assertTrue(selfHref.contains("/api/operations/indicator/" + id));
        }

        Assertions.assertTrue(ids.containsAll(List.of("p1651", "p1623", "p1636")));

        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        List<String> sortedLabels = new ArrayList<>(labels);
        sortedLabels.sort(collator);
        Assertions.assertEquals(sortedLabels, labels, "La liste des indicateurs doit être triée par label (Collator FR, primary strength)");
    }

    @Test
    void testGetIndicatorById_json_withLinks() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/p1651",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode indicator = objectMapper.readTree(response.getBody());

        Assertions.assertEquals("p1651", indicator.get("id").asText());
        Assertions.assertEquals("Index de réévaluation des actifs de la construction (base 2010)", indicator.get("prefLabelLg1").asText());
        Assertions.assertEquals("Index for updating material assets in construction (base 2010)", indicator.get("prefLabelLg2").asText());
        Assertions.assertEquals("Validated", indicator.get("validationState").asText());

        JsonNode wasGeneratedBy = indicator.get("wasGeneratedBy");
        Assertions.assertNotNull(wasGeneratedBy);
        Assertions.assertEquals(1, wasGeneratedBy.size());
        Assertions.assertEquals("s1032", wasGeneratedBy.get(0).get("id").asText());
        Assertions.assertEquals("series", wasGeneratedBy.get(0).get("type").asText());

        JsonNode seeAlso = indicator.get("seeAlso");
        Assertions.assertNotNull(seeAlso);
        List<String> seeAlsoIds = new ArrayList<>();
        for (JsonNode link : seeAlso) {
            seeAlsoIds.add(link.get("id").asText());
            Assertions.assertEquals("indicator", link.get("type").asText());
        }
        Assertions.assertTrue(seeAlsoIds.containsAll(List.of("p1661", "p1623", "p1650")));
    }

    @Test
    void testGetIndicatorById_json_replacesLink() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/p1623",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode indicator = objectMapper.readTree(response.getBody());

        Assertions.assertEquals("p1623", indicator.get("id").asText());
        JsonNode replaces = indicator.get("replaces");
        Assertions.assertNotNull(replaces);
        Assertions.assertEquals(1, replaces.size());
        Assertions.assertEquals("p1624", replaces.get(0).get("id").asText());
    }

    @Test
    void testGetIndicatorById_json_isReplacedByLink() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/p1636",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode indicator = objectMapper.readTree(response.getBody());

        Assertions.assertEquals("p1636", indicator.get("id").asText());
        JsonNode isReplacedBy = indicator.get("isReplacedBy");
        Assertions.assertNotNull(isReplacedBy);
        Assertions.assertEquals(1, isReplacedBy.size());
        Assertions.assertEquals("p1669", isReplacedBy.get(0).get("id").asText());
    }

    @Test
    void testGetIndicatorById_notFound() {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/p9999999",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetIndicatorById_xml() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", MediaType.APPLICATION_XML_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/p1651",
                HttpMethod.GET, entity, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.contains("<Indicator>"));
        Assertions.assertTrue(body.contains("<id>p1651</id>"));
        Assertions.assertTrue(body.contains("<validationState>Validated</validationState>"));
    }

    @Test
    void testGetIndicatorsWithSims_emptyOnTestData() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicators/withSims",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode result = objectMapper.readTree(response.getBody());
        Assertions.assertTrue(result.isArray());
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void testGetIndicatorsForSearch_richerShape() throws Exception {
        var response = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicators/advanced-search",
                HttpMethod.GET, jsonEntity(null), String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode result = objectMapper.readTree(response.getBody());
        Assertions.assertTrue(result.isArray());
        Assertions.assertTrue(result.size() > 0);

        JsonNode p1651 = null;
        for (JsonNode indicator : result) {
            if ("p1651".equals(indicator.get("id").asText())) {
                p1651 = indicator;
                break;
            }
        }
        Assertions.assertNotNull(p1651, "p1651 doit apparaître dans le résultat de la recherche avancée");
        Assertions.assertTrue(p1651.has("prefLabelLg1"));
        Assertions.assertTrue(p1651.has("creators"));
        Assertions.assertTrue(p1651.has("dataCollector"));
        Assertions.assertTrue(p1651.has("publishers"));
    }

    @Test
    void testCreateGetUpdateValidateCycle() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString();
        String prefLabelLg1 = "E2E indicateur FR " + uniqueSuffix;
        String prefLabelLg2 = "E2E indicator EN " + uniqueSuffix;

        String createBody = """
                {
                    "prefLabelLg1": "%s",
                    "prefLabelLg2": "%s",
                    "wasGeneratedBy": [{"id": "s1032", "type": "series"}]
                }
                """.formatted(prefLabelLg1, prefLabelLg2);

        var createResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator",
                HttpMethod.POST, jsonEntity(createBody), String.class);

        Assertions.assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        String newId = createResponse.getBody().replace("\"", "").trim();
        Assertions.assertFalse(newId.isBlank());

        var afterCreate = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/" + newId,
                HttpMethod.GET, jsonEntity(null), String.class);
        Assertions.assertEquals(HttpStatus.OK, afterCreate.getStatusCode());
        JsonNode createdIndicator = objectMapper.readTree(afterCreate.getBody());
        Assertions.assertEquals(prefLabelLg1, createdIndicator.get("prefLabelLg1").asText());
        Assertions.assertEquals("Unpublished", createdIndicator.get("validationState").asText());

        String updatedPrefLabelLg1 = prefLabelLg1 + " (updated)";
        String updateBody = """
                {
                    "prefLabelLg1": "%s",
                    "prefLabelLg2": "%s",
                    "wasGeneratedBy": [{"id": "s1032", "type": "series"}]
                }
                """.formatted(updatedPrefLabelLg1, prefLabelLg2);

        var updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/" + newId,
                HttpMethod.PUT, jsonEntity(updateBody), Object.class);
        Assertions.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        var afterUpdate = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/" + newId,
                HttpMethod.GET, jsonEntity(null), String.class);
        JsonNode updatedIndicator = objectMapper.readTree(afterUpdate.getBody());
        Assertions.assertEquals(updatedPrefLabelLg1, updatedIndicator.get("prefLabelLg1").asText());

        var validateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/" + newId + "/validate",
                HttpMethod.PUT, jsonEntity(""), String.class);
        Assertions.assertEquals(HttpStatus.OK, validateResponse.getStatusCode());

        var afterValidate = restTemplate.exchange(
                "http://localhost:" + port + "/api/operations/indicator/" + newId,
                HttpMethod.GET, jsonEntity(null), String.class);
        JsonNode validatedIndicator = objectMapper.readTree(afterValidate.getBody());
        Assertions.assertEquals("Validated", validatedIndicator.get("validationState").asText());
    }
}
