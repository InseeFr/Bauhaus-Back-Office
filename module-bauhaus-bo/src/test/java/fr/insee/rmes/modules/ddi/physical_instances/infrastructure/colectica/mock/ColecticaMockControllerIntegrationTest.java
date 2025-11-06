package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.QueryRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.webservice.ColecticaMockResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ColecticaMockControllerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = {
    "fr.insee.rmes.bauhaus.colectica.mock-server-enabled=true",
    "fr.insee.rmes.bauhaus.colectica.baseUrl=http://localhost:8082",
    "fr.insee.rmes.bauhaus.colectica.apiPath=/api/colectica/",
    "fr.insee.rmes.bauhaus.colectica.itemTypes=a51e85bb-6259-4488-8df2-f08cb43485f8"
})
class ColecticaMockControllerIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock")
    static class TestConfiguration {
    }

    @Autowired
    private ColecticaMockResources controller;

    @Test
    void shouldLoadControllerWhenEnabled() {
        assertNotNull(controller);
        String response = controller.getColectica();
        assertEquals("Mock Colectica Server Response", response);
    }

    @Test
    void shouldGetPhysicalInstances() {
        assertNotNull(controller);
        
        // Create request body with itemTypes
        QueryRequest queryRequest = new QueryRequest(List.of("a51e85bb-6259-4488-8df2-f08cb43485f8"));
        
        var response = controller.getPhysicalInstances(queryRequest);
        assertNotNull(response);
        assertNotNull(response.results());
        assertFalse(response.results().isEmpty());
        
        // Verify first result has identifier and other expected fields
        var firstItem = response.results().getFirst();
        assertNotNull(firstItem.identifier());
        assertNotNull(firstItem.itemName());
    }



    @Test
    void shouldCreateAuthenticationToken() {
        assertNotNull(controller);

        // Create authentication request
        AuthenticationRequest authRequest = new AuthenticationRequest("test-user", "test-password");

        ResponseEntity<AuthenticationResponse> response = controller.createToken(authRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().accessToken());
        assertTrue(response.getBody().accessToken().startsWith("mock-token-"));
    }

    @Test
    void shouldRejectEmptyCredentials() {
        assertNotNull(controller);

        // Test with empty username
        AuthenticationRequest emptyUsername = new AuthenticationRequest("", "password");
        ResponseEntity<AuthenticationResponse> response1 = controller.createToken(emptyUsername);
        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode());

        // Test with empty password
        AuthenticationRequest emptyPassword = new AuthenticationRequest("username", "");
        ResponseEntity<AuthenticationResponse> response2 = controller.createToken(emptyPassword);
        assertEquals(HttpStatus.UNAUTHORIZED, response2.getStatusCode());

        // Test with both empty
        AuthenticationRequest bothEmpty = new AuthenticationRequest("", "");
        ResponseEntity<AuthenticationResponse> response3 = controller.createToken(bothEmpty);
        assertEquals(HttpStatus.UNAUTHORIZED, response3.getStatusCode());
    }

    @Test
    void shouldRejectNullCredentials() {
        assertNotNull(controller);

        // Test with null request
        ResponseEntity<AuthenticationResponse> response1 = controller.createToken(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode());

        // Test with null username
        AuthenticationRequest nullUsername = new AuthenticationRequest(null, "password");
        ResponseEntity<AuthenticationResponse> response2 = controller.createToken(nullUsername);
        assertEquals(HttpStatus.UNAUTHORIZED, response2.getStatusCode());

        // Test with null password
        AuthenticationRequest nullPassword = new AuthenticationRequest("username", null);
        ResponseEntity<AuthenticationResponse> response3 = controller.createToken(nullPassword);
        assertEquals(HttpStatus.UNAUTHORIZED, response3.getStatusCode());
    }
}