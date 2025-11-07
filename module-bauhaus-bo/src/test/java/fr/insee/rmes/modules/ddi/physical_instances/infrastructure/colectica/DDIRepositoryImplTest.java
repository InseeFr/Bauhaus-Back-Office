package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaItem;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.ColecticaResponse;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.QueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private ObjectMapper objectMapper;

    private DDIRepositoryImpl ddiRepository;

    @BeforeEach
    void setUp() {
        ddiRepository = new DDIRepositoryImpl(restTemplate, colecticaConfiguration, objectMapper);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        String baseURI = "http://localhost:8082/api/colectica";
        String expectedUrl = baseURI + "/_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");
        
        ColecticaItem item1 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Instance Physique 1", "en", "Physical Instance 1"), // itemName
            Map.of("fr-FR", "Label 1", "en", "Label 1"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "PhysicalInstance", // itemType
            "agency1", // agencyId
            1, // version
            "pi-1", // identifier
            null, // item
            null, // notes
            "2025-01-01T00:00:00", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            1L, // transactionId
            0 // versionCreationType
        );
        
        ColecticaItem item2 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Instance Physique 2", "en", "Physical Instance 2"), // itemName
            Map.of("fr-FR", "Label 2", "en", "Label 2"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "PhysicalInstance", // itemType
            "agency2", // agencyId
            1, // version
            "pi-2", // identifier
            null, // item
            null, // notes
            null, // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            2L, // transactionId
            0 // versionCreationType
        );
        
        ColecticaResponse mockResponse = new ColecticaResponse(List.of(item1, item2));
        
        when(colecticaConfiguration.baseURI()).thenReturn(baseURI);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);
        when(restTemplate.postForObject(eq(expectedUrl), any(QueryRequest.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Instance Physique 1", result.get(0).label());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2025-01-01 00:00:00", sdf.format(result.get(0).versionDate()));
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Instance Physique 2", result.get(1).label());
        assertNull(result.get(1).versionDate());

        verify(colecticaConfiguration).baseURI();
        verify(colecticaConfiguration).itemTypes();
        verify(restTemplate).postForObject(eq(expectedUrl), any(QueryRequest.class), eq(ColecticaResponse.class));
    }

    @Test
    void shouldGetPhysicalInstanceById() throws Exception {
        // Given
        String instanceId = "pi-123";

        // Mock the ObjectMapper to return a simple Ddi4Response without loading the actual file
        Ddi4Response mockResponse = new Ddi4Response(
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );

        when(objectMapper.readValue(any(String.class), eq(Ddi4Response.class)))
                .thenReturn(mockResponse);

        // When
        Ddi4Response result = ddiRepository.getPhysicalInstance(instanceId);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertNotNull(result.dataRelationship());

        // Verify ObjectMapper was called
        verify(objectMapper).readValue(any(String.class), eq(Ddi4Response.class));
    }
}