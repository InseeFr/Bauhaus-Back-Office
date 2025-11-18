package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.QueryRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.service.MockDataService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.mock.webservice.ColecticaMockResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColecticaMockControllerIntegrationTest {

    @Mock
    private DDIRepository secondaryDDIRepository;

    private MockDataService mockDataService;

    private ColecticaMockResources controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Initialize MockDataService with mocked secondaryDDIRepository
        mockDataService = new MockDataService(secondaryDDIRepository);

        // Initialize ColecticaMockResources with mockDataService and objectMapper
        controller = new ColecticaMockResources(mockDataService, objectMapper);
    }

    @Test
    void shouldLoadControllerWhenEnabled() {
        assertNotNull(controller);
        assertNotNull(mockDataService);
    }

    @Test
    void shouldReturnMockServerInfo() {
        // When
        String info = controller.getColectica();

        // Then
        assertNotNull(info);
        assertEquals("Mock Colectica Server - Using Secondary Instance", info);
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        // Given
        AuthenticationRequest authRequest = new AuthenticationRequest("test-user", "test-password");

        // When
        ResponseEntity<AuthenticationResponse> response = controller.createToken(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().accessToken());
        assertTrue(response.getBody().accessToken().startsWith("mock-token-secondary-"));
    }

    @Test
    void shouldRejectEmptyCredentials() {
        // Test with empty username
        AuthenticationRequest emptyUsername = new AuthenticationRequest("", "password");
        ResponseEntity<AuthenticationResponse> response1 = controller.createToken(emptyUsername);
        assertEquals(HttpStatus.UNAUTHORIZED, response1.getStatusCode());

        // Test with empty password
        AuthenticationRequest emptyPassword = new AuthenticationRequest("username", "");
        ResponseEntity<AuthenticationResponse> response2 = controller.createToken(emptyPassword);
        assertEquals(HttpStatus.UNAUTHORIZED, response2.getStatusCode());
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        PartialPhysicalInstance instance1 = new PartialPhysicalInstance("id1", "Label 1", null, "fr.insee");
        PartialPhysicalInstance instance2 = new PartialPhysicalInstance("id2", "Label 2", null, "fr.insee");
        List<PartialPhysicalInstance> mockInstances = List.of(instance1, instance2);

        when(secondaryDDIRepository.getPhysicalInstances()).thenReturn(mockInstances);

        // When
        ColecticaResponse response = controller.getPhysicalInstances(new QueryRequest(List.of()));

        // Then
        assertNotNull(response);
        assertNotNull(response.results());
        assertEquals(2, response.results().size());
        assertEquals("id1", response.results().get(0).identifier());
        assertEquals("id2", response.results().get(1).identifier());
    }
}