package fr.insee.rmes.webservice;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.port.clientside.DDIService;
import fr.insee.rmes.webservice.response.ddi.PartialPhysicalInstanceResponse;
import fr.insee.rmes.webservice.response.ddi.PhysicalInstanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DdiResourcesTest {

    @Mock
    private DDIService ddiService;

    private DdiResources ddiResources;

    @BeforeEach
    void setUp() {
        ddiResources = new DdiResources(ddiService);
    }

    @Test
    void shouldGetPhysicalInstances() {
        List<PartialPhysicalInstance> expectedInstances = List.of(
                new PartialPhysicalInstance("pi-1", "Physical Instance 1"),
                new PartialPhysicalInstance("pi-2", "Physical Instance 2")
        );
        when(ddiService.getPhysicalInstances()).thenReturn(expectedInstances);

        ResponseEntity<List<PartialPhysicalInstanceResponse>> response = ddiResources.getPhysicalInstances();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialPhysicalInstanceResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first instance data and links
        assertEquals("pi-1", result.getFirst().getId());
        assertEquals("Physical Instance 1", result.getFirst().getLabel());
        assertNotNull(result.getFirst().getLinks());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals("/ddi/physical-instance/pi-1", result.getFirst().getRequiredLink("self").getHref());
        
        // Verify second instance data and links
        assertEquals("pi-2", result.get(1).getId());
        assertEquals("Physical Instance 2", result.get(1).getLabel());
        assertNotNull(result.get(1).getLinks());
        assertEquals(1, result.get(1).getLinks().toList().size());
        assertEquals("/ddi/physical-instance/pi-2", result.get(1).getRequiredLink("self").getHref());
        
        verify(ddiService).getPhysicalInstances();
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        String instanceId = "pi-123";
        PhysicalInstance expectedInstance = new PhysicalInstance(instanceId, "Physical Instance 123");
        when(ddiService.getPhysicalInstance(instanceId)).thenReturn(expectedInstance);

        PhysicalInstanceResponse result = ddiResources.getPhysicalInstanceById(instanceId);

        assertNotNull(result);
        assertEquals(instanceId, result.getId());
        assertEquals("Physical Instance 123", result.getLabel());
        
        // Verify hypermédia links
        assertNotNull(result.getLinks());
        assertEquals(1, result.getLinks().toList().size());
        assertEquals("/ddi/physical-instance/" + instanceId, result.getRequiredLink("self").getHref());
        
        verify(ddiService).getPhysicalInstance(instanceId);
    }
}