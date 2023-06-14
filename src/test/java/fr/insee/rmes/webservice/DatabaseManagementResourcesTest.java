package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.DatabaseManagementService;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;


class DatabaseManagementResourcesTest {

    @InjectMocks
    private DatabaseManagementResources databaseManagementResources;

    @Mock
    DatabaseManagementService databaseManagementService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturn500() throws RmesException {
        doThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", "")).when(databaseManagementService).clearGraph();
        ResponseEntity<?> response = databaseManagementResources.reset();
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200() throws RmesException {
        doNothing().when(databaseManagementService).clearGraph();
        ResponseEntity<?> response = databaseManagementResources.reset();
        Assertions.assertEquals(200, response.getStatusCode().value());
    }
}
