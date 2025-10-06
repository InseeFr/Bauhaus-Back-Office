package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DDIServiceImplTest {

    @Mock
    private DDIRepository ddiRepository;

    private DDIServiceImpl ddiService;

    @BeforeEach
    void setUp() {
        ddiService = new DDIServiceImpl(ddiRepository);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        List<PartialPhysicalInstance> expectedInstances = List.of(
                new PartialPhysicalInstance("pi-1", "Physical Instance 1", new Date()),
                new PartialPhysicalInstance("pi-2", "Physical Instance 2", new Date()),
                new PartialPhysicalInstance("pi-3", "Physical Instance 3", new Date())
        );
        when(ddiRepository.getPhysicalInstances()).thenReturn(expectedInstances);

        // When
        List<PartialPhysicalInstance> result = ddiService.getPhysicalInstances();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Physical Instance 1", result.get(0).label());
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Physical Instance 2", result.get(1).label());
        assertEquals("pi-3", result.get(2).id());
        assertEquals("Physical Instance 3", result.get(2).label());
        
        verify(ddiRepository).getPhysicalInstances();
    }

    @Test
    void shouldGetDdi4PhysicalInstance() {
        // Given
        String instanceId = "pi-test";
        Ddi4Response expectedResponse = new Ddi4Response(
            "test-schema",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        when(ddiRepository.getPhysicalInstance(instanceId)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.getDdi4PhysicalInstance(instanceId);

        // Then
        assertNotNull(result);
        assertEquals("test-schema", result.schema());
        
        verify(ddiRepository).getPhysicalInstance(instanceId);
    }

    @Test
    void shouldUpdatePhysicalInstance() {
        // Given
        String instanceId = "test-id";
        UpdatePhysicalInstanceRequest request = new UpdatePhysicalInstanceRequest(
            "Updated Physical Instance Label",
            "Updated DataRelationship Name"
        );
        Ddi4Response expectedResponse = new Ddi4Response(
            "updated-schema",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
        when(ddiRepository.getPhysicalInstance(instanceId)).thenReturn(expectedResponse);

        // When
        Ddi4Response result = ddiService.updatePhysicalInstance(instanceId, request);

        // Then
        assertNotNull(result);
        assertEquals("updated-schema", result.schema());
        verify(ddiRepository).updatePhysicalInstance(instanceId, request);
        verify(ddiRepository).getPhysicalInstance(instanceId);
    }







}