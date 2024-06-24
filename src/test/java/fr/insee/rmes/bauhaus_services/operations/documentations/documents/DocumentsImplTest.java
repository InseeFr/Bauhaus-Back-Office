package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

        String result = null;
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
}