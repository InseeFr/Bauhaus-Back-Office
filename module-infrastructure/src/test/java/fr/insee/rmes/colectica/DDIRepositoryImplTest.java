package fr.insee.rmes.colectica;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    private DDIRepositoryImpl ddiRepository;

    @BeforeEach
    void setUp() {
        ddiRepository = new DDIRepositoryImpl(restTemplate, colecticaConfiguration);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        String baseURI = "http://localhost:8082/colectica";
        String expectedUrl = baseURI + "/physical-instances";
        
        List<Map<String, String>> mockResponse = List.of(
                Map.of("id", "pi-1", "label", "Physical Instance 1"),
                Map.of("id", "pi-2", "label", "Physical Instance 2")
        );
        
        when(colecticaConfiguration.baseURI()).thenReturn(baseURI);
        when(restTemplate.exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Physical Instance 1", result.get(0).label());
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Physical Instance 2", result.get(1).label());
        
        verify(colecticaConfiguration).baseURI();
        verify(restTemplate).exchange(
                eq(expectedUrl),
                eq(HttpMethod.GET),
                eq(null),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        // Given
        String baseURI = "http://localhost:8082/colectica";
        String instanceId = "pi-123";
        String expectedUrl = baseURI + "/physical-instances/" + instanceId;
        
        Map<String, String> mockResponse = Map.of("id", instanceId, "label", "Physical Instance 123");
        
        when(colecticaConfiguration.baseURI()).thenReturn(baseURI);
        when(restTemplate.getForObject(expectedUrl, Map.class)).thenReturn(mockResponse);

        // When
        PhysicalInstance result = ddiRepository.getPhysicalInstance(instanceId);

        // Then
        assertNotNull(result);
        assertEquals(instanceId, result.id());
        assertEquals("Physical Instance 123", result.label());
        
        verify(colecticaConfiguration).baseURI();
        verify(restTemplate).getForObject(expectedUrl, Map.class);
    }
}