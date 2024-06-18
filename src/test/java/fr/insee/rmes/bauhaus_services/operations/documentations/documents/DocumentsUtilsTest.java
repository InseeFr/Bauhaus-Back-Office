package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentsUtilsTest {

    @Mock
    RepositoryGestion repositoryGestion = new RepositoryGestion();

    @InjectMocks
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

    @CsvSource({
            ",'{\"code\":361,\"message\":\"Empty fileName\"}'",
            "'','{\"code\":361,\"message\":\"Empty fileName\"}'",
            "'invalid@name!', '{\"code\":362,\"details\":\"invalid@name!\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
            "'.', '{\"code\":362,\"details\":\".\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
            "'..', '{\"code\":362,\"details\":\"..\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
            "'...', '{\"code\":362,\"details\":\"...\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
            "'-', '{\"code\":362,\"details\":\"-\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
            "'-.', '{\"code\":362,\"details\":\"-.\",\"message\":\"FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens\"}'",
   })
    @ParameterizedTest
    void test_checkFileNameValidity_throwsWhenNameInvalid(String fileName, String exceptionDetail) {
        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
        assertEquals(exception.getDetails(), exceptionDetail);
    }


    @Test
    void testFileNameIsValid() {
        String fileName = "valid_file-name.txt";
        assertDoesNotThrow(() -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
    }
}