package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


class DocumentsResourcesTest {

    @Mock
    private DocumentsService documentsService;

    @InjectMocks
    private DocumentsResources documentsResources;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void shouldReturnNotFoundException() throws RmesException, IOException {
    	when(documentsService.downloadDocument(anyString())).thenThrow(new NoSuchFileException("id"));
        ResponseEntity<Object> response = documentsResources.downloadDocument("id");
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        Assertions.assertEquals("id does not exist", response.getBody());
    }

    @Test
    void shouldReturnInternalException() throws RmesException, IOException {
        when(documentsService.downloadDocument(anyString())).thenThrow(new IOException(anyString()));
        ResponseEntity<Object> response = documentsResources.downloadDocument("id");
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }
}
