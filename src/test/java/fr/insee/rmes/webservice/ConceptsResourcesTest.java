package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class ConceptsResourcesTest {

    @InjectMocks
    private ConceptsResources conceptsResources;

    @Mock
    ConceptsCollectionService conceptsCollectionService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturn500IfRmesExceptionWhenFetchingCollectionById() throws RmesException {
        when(conceptsCollectionService.getCollectionByID(anyString())).thenThrow(new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "erreur", ""));
        ResponseEntity<?> response = conceptsResources.getCollectionByID("1");
        Assertions.assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void shouldReturn200WhenFetchingCollectionById() throws RmesException {
        when(conceptsCollectionService.getCollectionByID(anyString())).thenReturn("result");
        ResponseEntity<?> response = conceptsResources.getCollectionByID("1");
        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals("result", response.getBody());
    }
}
