package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.modules.commons.configuration.swagger.model.operations.documentation.DocumentId;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class DocumentsResourcesTest {

    @MockitoBean
    DocumentsService documentsService;

    @Test
    void shouldReturnResponseWhenGetDocuments() throws RmesException {
        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        when(documentsService.getDocuments()).thenReturn("mocked result");
        String actual =documentsResources.getDocuments().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[Content-Type:\"application/json\"]>",actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "mocked id", ""})
    void shouldReturnResponseWhenSetDocument(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        doNothing().when(documentsService).setDocument(id, " mocked body");
        String actual =documentsResources.setDocument(documentId," mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @Test
    void shouldReturnResponseWhenDeleteDocument() throws RmesException {
        DocumentId documentId = new DocumentId("mocked id");
        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        when(documentsService.deleteDocument("mocked id")).thenReturn(HttpStatus.OK);
        String actual =documentsResources.deleteDocument(documentId).toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "mocked id"})
    void shouldReturnResponseWhenDeleteLink(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        when(documentsService.deleteLink(id)).thenReturn(HttpStatus.OK);
        String actual =documentsResources.deleteLink(documentId).toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "mocked id", ""})
    void shouldReturnResponseWhenSetLink(String id) throws RmesException {
        DocumentId documentId = new DocumentId(id);
        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        when(documentsService.setLink(id, " mocked body")).thenReturn("mocked result");
        String actual =documentsResources.setLink(documentId," mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK,"));
    }

    @Test
    void shouldReturn201WithLocationHeaderWhenCreateDocument() throws RmesException, IOException {
        // Given
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/documents/document");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        DocumentsResources documentsResources = new DocumentsResources(documentsService);
        String expectedId = "document123";
        String documentBody = "{\"labelLg1\": \"Test Document\"}";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(documentsService.createDocument(eq(documentBody), any(), eq("test-document.pdf")))
                .thenReturn(expectedId);

        // When
        ResponseEntity<String> response = documentsResources.setDocument(documentBody, file);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        assertEquals(
                "/documents/document/" + expectedId,
                Objects.requireNonNull(response.getHeaders().getLocation()).getPath()
        );
    }

}
