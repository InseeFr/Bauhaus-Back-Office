package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.when;

class StructureResourcesTest {

    @InjectMocks
    private StructureResources structureResources;

    @Mock
    StructureService structureService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldReturn500IfRmesException() throws RmesException {
        when(structureService.getStructuresForSearch()).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        Response response = structureResources.getStructuresForSearch();
        Assertions.assertEquals(500, response.getStatus());
    }
}
