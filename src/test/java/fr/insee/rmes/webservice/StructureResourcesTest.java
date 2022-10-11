package fr.insee.rmes.webservice;

import static org.mockito.Mockito.*;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesException;

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
    void shouldReturn500IfRmesExceptionWhenFetchingStructuresForSearch() throws RmesException {
        when(structureService.getStructuresForSearch()).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = structureResources.getStructuresForSearch();
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingStructureById() throws RmesException {
        when(structureService.getStructureById(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = structureResources.getStructureById("1");
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200WhenFetchingStructureById() throws RmesException {
        when(structureService.getStructureById(anyString())).thenReturn("result");
        ResponseEntity<?> response = structureResources.getStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenPublishingAStructure() throws RmesException {
        when(structureService.publishStructureById(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200WhenPublishingAStructure() throws RmesException {
        when(structureService.publishStructureById(anyString())).thenReturn("result publishing");
        ResponseEntity<?> response = structureResources.publishStructureById("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result publishing", response.getBody());
    }
}
