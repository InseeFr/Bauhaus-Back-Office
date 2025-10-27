package fr.insee.rmes.testcontainers.e2e.operations;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.rmes.testcontainers.e2e.BaseE2ETest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;

class FamilyResourcesE2ETest extends BaseE2ETest {

    @Test
    void testGetFamilies() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        var response = restTemplate.exchange(
            "http://localhost:" + port + "/api/operations/families",
            HttpMethod.GET, 
            entity, 
            String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        try {
            JsonNode jsonArray = objectMapper.readTree(response.getBody());
            Assertions.assertTrue(jsonArray.isArray());
            
            int familyCount = jsonArray.size();
            Assertions.assertTrue(familyCount > 0);

            JsonNode firstFamily = jsonArray.get(0);

            Assertions.assertTrue(firstFamily.has("id"));
            Assertions.assertTrue(firstFamily.has("label"));

            String firstFamilyId = firstFamily.get("id").asText();
            String firstFamilyLabel = firstFamily.get("label").asText();

            var firstSeriesHref = firstFamily.get("_links").get("self").get("href").asText();
            Assertions.assertTrue(firstSeriesHref.contains("/api/operations/family/" + firstFamilyId));

            Assertions.assertFalse(firstFamilyId.isEmpty());
            Assertions.assertFalse(firstFamilyLabel.isEmpty());
        } catch (Exception e) {
            Assertions.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

    @Test
    void testGetFamilyById() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        var response = restTemplate.exchange(
            "http://localhost:" + port + "/api/operations/family/s88", 
            HttpMethod.GET, 
            entity, 
            String.class
        );

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        
        try {
            JsonNode familyJson = objectMapper.readTree(response.getBody());
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

            var firstSeriesHref = series.get(0).get("_links").get("self").get("href").asText();
            Assertions.assertTrue(firstSeriesHref.contains("/api/operations/series/s1033"));

        } catch (Exception e) {
            Assertions.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

}