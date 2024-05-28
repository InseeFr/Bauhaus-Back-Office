package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class DocumentsImplTest {
    @MockBean
    private DocumentsUtils documentsUtils;

    @Autowired
    private DocumentsImpl documentService;

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