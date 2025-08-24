package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentsUtilsTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    Config config;


    @Mock
    FilesOperations filesOperations;

    @BeforeAll
    static void initGenericQueries() {
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldGetIdFromJson(){
        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        JSONObject jsonFirst = new JSONObject();
        jsonFirst.put("id","32").put("creator","creatorExample");

        JSONObject jsonSecond = new JSONObject();
        jsonSecond.put("id","undefined").put("publisher","publisher");

        JSONObject jsonThird = new JSONObject();
        jsonThird.put("id","");

        boolean getIdFromJsonFirst = documentsUtils.getIdFromJson(jsonFirst).toString().equals("32");
        boolean getIdFromJsonSecond = documentsUtils.getIdFromJson(jsonSecond)==null;
        boolean getIdFromJsonThird= documentsUtils.getIdFromJson(jsonThird)==null;

        List<Boolean> expect = List.of(true,true,true);
        List<Boolean> actual = List.of(getIdFromJsonFirst,getIdFromJsonSecond,getIdFromJsonThird);

        assertEquals(expect,actual);

    }

    @Test
    void shouldReturnAListOfDocuments() throws RmesException {
        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        JSONObject document = new JSONObject().put("id", "1");
        JSONArray array = new JSONArray();
        array.put(document);
        Stubber.forRdfService(documentsUtils).injectRepoGestion(repositoryGestion);


        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(array);

        JSONArray response = documentsUtils.getListDocumentLink("1", "2", "en");
        Assertions.assertEquals(response.get(0), document);
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
        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
        assertEquals(exception.getDetails(), exceptionDetail);
    }


    @Test
    void testFileNameIsValid() {
        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        String fileName = "valid_file-name.txt";
        assertDoesNotThrow(() -> {
            documentsUtils.checkFileNameValidity(fileName);
        });
    }

    @Test
    void testIfDateMiseAJourSavedAsString() throws RmesException {
        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        Stubber.forRdfService(documentsUtils).injectRepoGestion(repositoryGestion);
        Stubber.forRdfService(documentsUtils).injectConfig(config);

        when(config.getDocumentsStorageGestion()).thenReturn("/path/");
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject());
        when(filesOperations.dirExists(any(Path.class))).thenReturn(true);

        var id = "1";
        var body = new JSONObject()
                .put("id", "1")
                .put("updatedDate", "2024-11-20").toString();
        var isLink = false;
        var document = IOUtils.toInputStream("stream");
        var name = "documentName";

        String documentIRIString = "http://document/1";
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI documentIRI = valueFactory.createIRI(documentIRIString);
        IRI graph = valueFactory.createIRI("http://documents/graph");

        try (MockedStatic<RdfUtils> rdfUtilsMockedStatic = Mockito.mockStatic(RdfUtils.class);
             MockedStatic<DocumentsQueries> documentQueriesMockedStatic = Mockito.mockStatic(DocumentsQueries.class)
        ) {
            rdfUtilsMockedStatic.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMockedStatic.when(() -> RdfUtils.addTripleString(eq(documentIRI), any(IRI.class), any(), any(Model.class), eq(graph))).thenCallRealMethod();
            rdfUtilsMockedStatic.when(() -> RdfUtils.setLiteralDate(any(String.class))).thenCallRealMethod();
            rdfUtilsMockedStatic.when(() -> RdfUtils.addTripleDate(eq(documentIRI), any(IRI.class), any(), any(Model.class), eq(graph))).thenCallRealMethod();
            rdfUtilsMockedStatic.when(RdfUtils::documentsGraph).thenReturn(graph);
            rdfUtilsMockedStatic.when(() -> RdfUtils.toString(any())).thenReturn(documentIRIString);
            rdfUtilsMockedStatic.when(() -> RdfUtils.toURI(any())).thenReturn(documentIRI);
            documentQueriesMockedStatic.when(() -> DocumentsQueries.checkLabelUnicity(eq("1"), anyString(), any())).thenReturn(documentIRIString);


            documentsUtils.createDocument(id, body, isLink, document, name);
            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

            verify(repositoryGestion, times(1)).loadSimpleObject(any(), model.capture());
            Assertions.assertEquals("[(http://document/1, http://purl.org/pav/lastRefreshedOn, \"2024-11-20\"^^<http://www.w3.org/2001/XMLSchema#date>) [http://documents/graph]]", model.getValue().toString());
        }
    }

    @Test
    void shouldBuildDocumentFromJson() {

        DocumentsUtils documentsUtils = new DocumentsUtils(null, filesOperations);

        JSONObject jsonObject = new JSONObject().
                put(Constants.LABEL_LG1,"mocked Label LG1").
                put(Constants.LABEL_LG2,"mocked Label LG2").
                put(Constants.DESCRIPTION_LG1,"mocked Description LG1").
                put(Constants.DESCRIPTION_LG2,"mocked Description LG2").
                put(Constants.UPDATED_DATE,"mocked Updated Date").
                put(Constants.LANG,"mocked Lang").
                put(Constants.URL,"mocked URL").
                put(Constants.URI,"mocked URI");

        List<Boolean> actual = List.of(
                documentsUtils.buildDocumentFromJson(jsonObject).getLabelLg1()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getLabelLg2()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getDescriptionLg1()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getDescriptionLg2()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getDateMiseAJour()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getLangue()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getUrl()!=null,
                documentsUtils.buildDocumentFromJson(jsonObject).getUri()!=null
                );

        assertEquals(List.of(true,true,true,true,true,true,true,true),actual);

    }




}