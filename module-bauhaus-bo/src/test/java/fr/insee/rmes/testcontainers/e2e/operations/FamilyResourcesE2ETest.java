package fr.insee.rmes.testcontainers.e2e.operations;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

class FamilyResourcesE2ETest extends BaseE2ETest {

    @Test
    void testGetFamilies() {
        var response = getJsonOk("/api/operations/families");

        try {
            JsonNode jsonArray = objectMapper.readTree(response.getResponseBody());
            Assertions.assertTrue(jsonArray.isArray());

            int familyCount = jsonArray.size();
            Assertions.assertTrue(familyCount > 0);

            JsonNode firstFamily = jsonArray.get(0);

            Assertions.assertTrue(firstFamily.has("id"));
            Assertions.assertTrue(firstFamily.has("label"));

            String firstFamilyId = firstFamily.get("id").asText();
            String firstFamilyLabel = firstFamily.get("label").asText();

            var firstSeriesHref =
                    firstFamily.get("_links").get("self").get("href").asText();
            Assertions.assertTrue(firstSeriesHref.contains("/api/operations/family/" + firstFamilyId));

            Assertions.assertFalse(firstFamilyId.isEmpty());
            Assertions.assertFalse(firstFamilyLabel.isEmpty());
        } catch (Exception e) {
            Assertions.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    @Test
    void testGetFamilyById() {
        var response = getJsonOk("/api/operations/family/s88");

        try {
            JsonNode familyJson = objectMapper.readTree(response.getResponseBody());
            Assertions.assertTrue(familyJson.isObject());

            Assertions.assertTrue(familyJson.has("id"));
            String familyId = familyJson.get("id").asText();
            Assertions.assertEquals("s88", familyId);

            if (familyJson.has("prefLabelLg1")) {
                String prefLabel = familyJson.get("prefLabelLg1").asText();
                Assertions.assertEquals("Voir également", prefLabel);
            }

            var series = familyJson.get("series");
            Assertions.assertEquals(2, series.size());

            var firstSeriesHref =
                    series.get(0).get("_links").get("self").get("href").asText();
            Assertions.assertTrue(firstSeriesHref.contains("/api/operations/series/s1033"));

        } catch (Exception e) {
            Assertions.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    /** GET en JSON, dont la réponse doit être un 200 avec un corps. */
    private EntityExchangeResult<String> getJsonOk(String path) {
        var response = restTestClient
                .get()
                .uri("http://localhost:" + port + path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult(String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getResponseBody());
        return response;
    }
}
