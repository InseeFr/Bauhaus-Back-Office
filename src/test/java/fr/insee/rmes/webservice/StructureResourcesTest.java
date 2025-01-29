package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.structures.StructureResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class StructureResourcesTest {

    @InjectMocks
    private StructureResources structureResources;

    @Mock
    StructureService structureService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

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
