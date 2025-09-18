package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.domain.port.serverside.DDIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                new PartialPhysicalInstance("pi-1", "Physical Instance 1"),
                new PartialPhysicalInstance("pi-2", "Physical Instance 2"),
                new PartialPhysicalInstance("pi-3", "Physical Instance 3")
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
    void shouldGetPhysicalInstance() {
        // Given
        String instanceId = "pi-test";
        PhysicalInstance expectedInstance = new PhysicalInstance(instanceId, "Test Physical Instance");
        when(ddiRepository.getPhysicalInstance(instanceId)).thenReturn(expectedInstance);

        // When
        PhysicalInstance result = ddiService.getPhysicalInstance(instanceId);

        // Then
        assertNotNull(result);
        assertEquals(instanceId, result.id());
        assertEquals("Test Physical Instance", result.label());
        
        verify(ddiRepository).getPhysicalInstance(instanceId);
    }
}