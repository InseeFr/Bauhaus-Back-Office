package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import fr.insee.rmes.domain.exceptions.RmesException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DocumentsImplTest {
    @Mock
    private DocumentsUtils documentsUtils;

    @InjectMocks
    private DocumentsImpl documentService;

    @Test
    void testGetDocuments() throws RmesException {
        JSONArray documents = new JSONArray("[\"document\"]");
        when(documentsUtils.getAllDocuments()).thenReturn(documents);
        assertEquals(documentService.getDocuments(), documents.toString());
    }

    @Test
    void testGetDocument() throws RmesException {
        JSONObject document = new JSONObject().put("id", "1");
        when(documentsUtils.getDocument("1", false)).thenReturn(document);
        assertEquals(documentService.getDocument("1"), document);
    }

    @Test
    void testGetLink() throws RmesException {
        JSONObject document = new JSONObject().put("id", "1");
        when(documentsUtils.getDocument("1", true)).thenReturn(document);
        assertEquals(documentService.getLink("1"), document);
    }

    @Test
    void testCreateDocument() throws RmesException {
        String body = "Sample body";
        InputStream documentFile = new ByteArrayInputStream("Sample content".getBytes());
        String documentName = "valid_document.txt";
        String generatedId = "12345";

        doCallRealMethod().when(documentsUtils).checkFileNameValidity(documentName);
        when(documentsUtils.createDocumentID()).thenReturn(generatedId);
        doNothing().when(documentsUtils).createDocument(generatedId, body, false, documentFile, documentName);

        String result;
        try {
            result = documentService.createDocument(body, documentFile, documentName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(generatedId, result);
        verify(documentsUtils).checkFileNameValidity(documentName);
        verify(documentsUtils).createDocumentID();
        verify(documentsUtils).createDocument(generatedId, body, false, documentFile, documentName);
    }

    @Test
    void testChangeDocument() throws RmesException {
        String docId = "12345";
        InputStream documentFile = new ByteArrayInputStream("Sample content".getBytes());
        String documentName = "valid_document.txt";
        String expectedUrl = "http://example.com/document/12345";

        doCallRealMethod().when(documentsUtils).checkFileNameValidity(documentName);
        when(documentsUtils.changeFile(docId, documentFile, documentName)).thenReturn(expectedUrl);

        String result = documentService.changeDocument(docId, documentFile, documentName);

        assertEquals(expectedUrl, result);
        verify(documentsUtils).checkFileNameValidity(documentName);
        verify(documentsUtils).changeFile(docId, documentFile, documentName);
    }

    @Test
    void shouldCreateDocument() throws RmesException, IOException {
        DocumentsImpl documentsImpl = new DocumentsImpl(documentsUtils);
        InputStream documentFile = new InputStream() {
            public int read() {
                return 2025;
            }
        };
        when(documentsUtils.createDocumentID()).thenReturn("idExample");
        String actual = documentsImpl.createDocument("body", documentFile, "documentName");
        assertEquals("idExample", actual);
    }

    @Test
    void shouldChangeDocument() throws RmesException {
        DocumentsImpl documentsImpl = new DocumentsImpl(documentsUtils);
        InputStream documentFile = new InputStream() {
            public int read() {
                return 2025;
            }
        };
        when(documentsUtils.changeFile("docId", documentFile, "documentName")).thenReturn("idExample");
        String actual = documentsImpl.changeDocument("docId", documentFile, "documentName");
        assertEquals("idExample", actual);
    }

    @Test
    void shouldSetLinkWhenSeveralArguments() throws RmesException {
        DocumentsImpl documentsImpl = new DocumentsImpl(documentsUtils);
        when(documentsUtils.createDocumentID()).thenReturn("id");
        doNothing().when(documentsUtils).createDocument("id", "body", true, null, null);
        String actual = documentsImpl.setLink("body");
        assertEquals("id", actual);
    }

    @Test
    void shouldSetLinkWhenUsingOnlyOneParameter() throws RmesException {
        DocumentsImpl documentsImpl = new DocumentsImpl(documentsUtils);
        doNothing().when(documentsUtils).setDocument("id", "body", true);
        String actual = documentsImpl.setLink("id", "body");
        assertEquals("id", actual);
    }

    @Test
    void shouldUpdateADocumentAsADocumentAndNotAsALink() throws RmesException {
        documentService.setDocument("1000", "body");

        verify(documentsUtils).setDocument("1000", "body", false);
    }

    @Test
    void shouldDeleteADocumentAsADocumentAndNotAsALink() throws RmesException {
        when(documentsUtils.deleteDocument("1000", false)).thenReturn(HttpStatus.OK);

        assertEquals(HttpStatus.OK, documentService.deleteDocument("1000"));
        verify(documentsUtils).deleteDocument("1000", false);
    }

    @Test
    void shouldDeleteALinkAsALinkAndNotAsADocument() throws RmesException {
        when(documentsUtils.deleteDocument("1000", true)).thenReturn(HttpStatus.OK);

        assertEquals(HttpStatus.OK, documentService.deleteLink("1000"));
        verify(documentsUtils).deleteDocument("1000", true);
    }

    @Test
    void shouldDelegateTheDownloadToTheStorage() throws RmesException {
        ResponseEntity<Resource> expected = ResponseEntity.ok(new ByteArrayResource("contenu".getBytes()));
        when(documentsUtils.downloadDocumentFile("1000")).thenReturn(expected);

        assertEquals(expected, documentService.downloadDocument("1000"));
    }
}
