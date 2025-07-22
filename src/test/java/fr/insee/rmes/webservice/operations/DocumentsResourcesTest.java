package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.config.swagger.model.operations.documentation.DocumentId;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Properties;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class DocumentsResourcesTest {

    @MockitoBean
    DocumentsService documentsService;

    @MockitoBean
    Properties properties;

    @Test
    void shouldReturnResponseWhenGetDocuments() throws RmesException {
        DocumentsResources documentsResources = new DocumentsResources(documentsService,properties);
        when(documentsService.getDocuments()).thenReturn("mocked result");
        String actual =documentsResources.getDocuments().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[Content-Type:\"application/json\"]>",actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "mocked id", ""})
    void shouldReturnResponseWhenSetDocument(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService,properties);
        doNothing().when(documentsService).setDocument(id, " mocked body");
        String actual =documentsResources.setDocument(documentId," mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @Test
    void shouldReturnResponseWhenDeleteDocument() throws RmesException {
        DocumentId documentId = new DocumentId("mocked id");
        DocumentsResources documentsResources = new DocumentsResources(documentsService,properties);
        when(documentsService.deleteDocument("mocked id")).thenReturn(HttpStatus.OK);
        String actual =documentsResources.deleteDocument(documentId).toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "mocked id"})
    void shouldReturnResponseWhenDeleteLink(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService,properties);
        when(documentsService.deleteLink(id)).thenReturn(HttpStatus.OK);
        String actual =documentsResources.deleteLink(documentId).toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }


    @ParameterizedTest
    @ValueSource(strings = { "mocked id", ""})
    void shouldReturnResponseWhenSetLink(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService,properties);
        when(documentsService.setLink(id, " mocked body")).thenReturn("mocked result");
        String actual =documentsResources.setLink(documentId," mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }
}