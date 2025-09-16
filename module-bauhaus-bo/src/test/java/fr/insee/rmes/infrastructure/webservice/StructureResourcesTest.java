package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureService;
<<<<<<< HEAD
import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/StructureResourcesTest.java
=======
=======
import fr.insee.rmes.domain.exceptions.RmesException;
>>>>>>> 895fe5ae (refactor: migrate getFamily et getFamilies to the hexagonale architecture (#995))
import fr.insee.rmes.onion.infrastructure.webservice.structures.StructureResources;
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/infrastructure/webservice/StructureResourcesTest.java
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
