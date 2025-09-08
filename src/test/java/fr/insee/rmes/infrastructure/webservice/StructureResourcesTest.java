package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.infrastructure.webservice.structures.StructureResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StructureResourcesTest {

    @InjectMocks
    private StructureResources structureResources;

    @Mock
    StructureService structureService;

    @Test
    void shouldReturn200WhenFetchingStructureById() throws RmesException {
        when(structureService.getStructureById(anyString())).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn200WhenPublishingAStructure() throws RmesException {
        when(structureService.publishStructureById(anyString())).thenReturn("result publishing");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result publishing", response.getBody());
    }
}
