package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.bauhaus_services.FilesOperations;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfServicesForRdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentsUtilsTest {

    public static final String DOCUMENTS_GRAPH = "http://documents/graph";
    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    Config config;

    @Mock
    StampsRestrictionsService stampsRestrictionsService;

    @Mock
    FilesOperations filesOperations;

    String documentIRIString = "http://document/1";



    @BeforeAll
    static void initGenericQueries() {
        GenericQueries.setConfig(new ConfigStub());
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

        Stubber.forRdfService(documentsUtils).injectStampsRestrictionsService(stampsRestrictionsService);
        Stubber.forRdfService(documentsUtils).injectRepoGestion(repositoryGestion);
        Stubber.forRdfService(documentsUtils).injectConfig(config);

        when(config.getDocumentsStorageGestion()).thenReturn("/path/");
        when(stampsRestrictionsService.canManageDocumentsAndLinks()).thenReturn(true);
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


        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI documentIRI = valueFactory.createIRI(documentIRIString);
        IRI graph = valueFactory.createIRI(DOCUMENTS_GRAPH);

        try (MockedStatic<RdfUtils> rdfUtilsMockedStatic = Mockito.mockStatic(RdfUtils.class);
             MockedStatic<DocumentsQueries> documentQueriesMockedStatic = Mockito.mockStatic(DocumentsQueries.class)
        ) {
            
            initRdfUtils();
            
//            rdfUtilsMockedStatic.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
//            rdfUtilsMockedStatic.when(() -> RdfUtils.addTripleString(eq(documentIRI), any(IRI.class), any(), any(Model.class), eq(graph))).thenCallRealMethod();
//            rdfUtilsMockedStatic.when(() -> RdfUtils.setLiteralDate(any(String.class))).thenCallRealMethod();
//            rdfUtilsMockedStatic.when(() -> RdfUtils.addTripleDate(eq(documentIRI), any(IRI.class), any(), any(Model.class), eq(graph))).thenCallRealMethod();
//            rdfUtilsMockedStatic.when(RdfUtils::documentsGraph).thenReturn(graph);
// ?            rdfUtilsMockedStatic.when(() -> RdfUtils.toURI(any())).thenReturn(documentIRI);
            documentQueriesMockedStatic.when(() -> DocumentsQueries.checkLabelUnicity(eq("1"), anyString(), any())).thenReturn(documentIRIString);


            documentsUtils.createDocument(id, body, isLink, document, name);
            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

            verify(repositoryGestion, times(1)).loadSimpleObject(any(), model.capture());
            Assertions.assertEquals("[(http://document/1, http://purl.org/pav/lastRefreshedOn, \"2024-11-20\"^^<http://www.w3.org/2001/XMLSchema#date>, http://documents/graph) [http://documents/graph]]", model.getValue().toString());
        }
    }

    private void initRdfUtils() {

        // ?            rdfUtilsMockedStatic.when(() -> ArgumentMatchers.<IRI>any().toString()).thenReturn(documentIRIString);

        RdfServicesForRdfUtils rdfServicesForRdfUtils = new RdfServicesForRdfUtils(new Config(){
            @Override
            public String getDocumentsGraph() {
                return DOCUMENTS_GRAPH;
            }
        }, new UriUtils("","http://bauhaus/", null));
        rdfServicesForRdfUtils.initRdfUtils();
    }
}