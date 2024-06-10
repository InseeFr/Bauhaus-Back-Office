package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentsUtilsTest {

    @MockBean
    RepositoryGestion repositoryGestion;

    @Autowired
    DocumentsUtils documentsUtils;

    @Test
    void shouldReturnAListOfDocuments() throws RmesException {
        JSONObject document = new JSONObject().put("id", "1");
        JSONArray array = new JSONArray();
        array.put(document);

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DocumentsQueries> mockedFactory = Mockito.mockStatic(DocumentsQueries.class)) {
            mockedFactory.when(() -> DocumentsQueries.getDocumentsForSimsRubricQuery(eq("1"), eq("2"), eq("http://bauhaus/codes/langue/en"))).thenReturn("query");
            JSONArray response = documentsUtils.getListDocumentLink("1", "2", "en");
            Assertions.assertEquals(response.get(0), document);
        }
    }

    @Test
    public void testFileNameIsNull() {
        String fileName = null;
        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
        assertEquals(exception.getDetails(), "{\"code\":361,\"message\":\"Empty fileName\"}");
    }

    @Test
    public void testFileNameIsEmpty() {
        String fileName = "";
        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
        assertEquals(exception.getDetails(), "{\"code\":361,\"message\":\"Empty fileName\"}");
    }

    @Test
    public void testFileNameContainsForbiddenCharacters() {
        String fileName = "invalid@name!";
        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
        assertEquals(exception.getDetails(), "{\"code\":362,\"details\":\"invalid@name!\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}");
    }

    @Test
    public void testFileNameIsValid() {
        String fileName = "valid_file-name.txt";
        assertDoesNotThrow(() -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
    }
}