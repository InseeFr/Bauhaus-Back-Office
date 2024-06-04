package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class DocumentsImplTest {
    @MockBean
    private DocumentsUtils documentsUtils;

    @Autowired
    private DocumentsImpl documentService;

    @Test
    public void testGetDocuments() throws RmesException {
        JSONArray documents = new JSONArray("[\"document\"]");
        when(documentsUtils.getAllDocuments()).thenReturn(documents);
        assertEquals(documentService.getDocuments(), documents.toString());
    }

    @Test
    public void testGetDocument() throws RmesException {
        JSONObject document = new JSONObject().put("id", "1");
        when(documentsUtils.getDocument(eq("1"), eq(false))).thenReturn(document);
        assertEquals(documentService.getDocument("1"), document);
    }

    @Test
    public void testGetLink() throws RmesException {
        JSONObject document = new JSONObject().put("id", "1");
        when(documentsUtils.getDocument(eq("1"), eq(true))).thenReturn(document);
        assertEquals(documentService.getLink("1"), document);
    }

    @Test
    public void testCreateDocument() throws RmesException {
        String body = "Sample body";
        InputStream documentFile = new ByteArrayInputStream("Sample content".getBytes());
        String documentName = "valid_document.txt";
        String generatedId = "12345";

        doNothing().when(documentsUtils).checkFileNameValidity(documentName);
        when(documentsUtils.createDocumentID()).thenReturn(generatedId);
        doNothing().when(documentsUtils).createDocument(eq(generatedId), eq(body), eq(false), eq(documentFile), eq(documentName));

        String result = documentService.createDocument(body, documentFile, documentName);

        assertEquals(generatedId, result);
        verify(documentsUtils).checkFileNameValidity(documentName);
        verify(documentsUtils).createDocumentID();
        verify(documentsUtils).createDocument(eq(generatedId), eq(body), eq(false), eq(documentFile), eq(documentName));
    }

    @Test
    public void testChangeDocument() throws RmesException {
        String docId = "12345";
        InputStream documentFile = new ByteArrayInputStream("Sample content".getBytes());
        String documentName = "valid_document.txt";
        String expectedUrl = "http://example.com/document/12345";

        doNothing().when(documentsUtils).checkFileNameValidity(documentName);
        when(documentsUtils.changeFile(eq(docId), eq(documentFile), eq(documentName))).thenReturn(expectedUrl);

        String result = documentService.changeDocument(docId, documentFile, documentName);

        assertEquals(expectedUrl, result);
        verify(documentsUtils).checkFileNameValidity(documentName);
        verify(documentsUtils).changeFile(eq(docId), eq(documentFile), eq(documentName));
    }
}