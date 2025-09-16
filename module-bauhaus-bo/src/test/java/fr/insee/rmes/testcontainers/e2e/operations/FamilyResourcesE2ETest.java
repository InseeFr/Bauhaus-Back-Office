package fr.insee.rmes.testcontainers.e2e.operations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

@Tag("integration")
@AppSpringBootTest
class FamilyResourcesE2ETest extends WithGraphDBContainer {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> getRdfGestionConnectionDetails().getUrlServer());
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> getRdfGestionConnectionDetails().repositoryId());
    }

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
    }

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

            Assertions.assertEquals("s82", firstFamilyId);
            Assertions.assertEquals("Activité, production et chiffre d'affaires", firstFamilyLabel);
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
            
        } catch (Exception e) {
            Assertions.fail("Failed to parse JSON response: " + e.getMessage());
        }
    }

}