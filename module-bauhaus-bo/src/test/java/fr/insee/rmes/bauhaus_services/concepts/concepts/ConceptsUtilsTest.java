package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.bauhaus_services.notes.NotesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ConceptsUtilsTest {

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private RepositoryPublication repositoryPublication;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private PublicationUtils publicationUtils;

    @Mock
    private NotesUtils notesUtils;

    private ConceptsUtils conceptsUtils;
    private ConceptsPublication conceptsPublication;
    private NoteManager noteManager;

    @BeforeEach
    void setUp() throws Exception {
        // Create ConceptsPublication and inject its dependencies
        conceptsPublication = new ConceptsPublication();
        injectField(conceptsPublication, "repoGestion", repoGestion);
        injectField(conceptsPublication, "repositoryPublication", repositoryPublication);
        injectField(conceptsPublication, "idGenerator", idGenerator);
        injectField(conceptsPublication, "publicationUtils", publicationUtils);
        injectField(conceptsPublication, "config", new ConfigStub());

        // Create NoteManager with NotesUtils
        noteManager = new NoteManager(notesUtils);

        // Create ConceptsUtils with necessary dependencies
        conceptsUtils = new ConceptsUtils(conceptsPublication, noteManager, 5);

        // Inject mocks using reflection for fields from RdfService
        injectField(conceptsUtils, "repoGestion", repoGestion);
        injectField(conceptsUtils, "repositoryPublication", repositoryPublication);
        injectField(conceptsUtils, "idGenerator", idGenerator);
        injectField(conceptsUtils, "publicationUtils", publicationUtils);
        injectField(conceptsUtils, "config", new ConfigStub());
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = getFieldFromClassHierarchy(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field getFieldFromClassHierarchy(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in class hierarchy");
    }

    @Test
    void shouldReturnGetConceptExportFileName() {

        ConceptsPublication conceptsPublication =  new ConceptsPublication();
        NoteManager noteManager = new NoteManager(null);
        ConceptsUtils conceptsUtilsExample = new ConceptsUtils(conceptsPublication,noteManager,19);

        ConceptForExport conceptForExport = new ConceptForExport();
        conceptForExport.setId("id");
        conceptForExport.setPrefLabelLg1("prefLabel1");
        conceptForExport.setPrefLabelLg2("prefLabel2");

        String response = conceptsUtilsExample.getConceptExportFileName(conceptForExport);

        assertEquals("idPreflabel1",response);
    }

    @Test
    void shouldCreateID() throws RmesException {
        List<String> identifiers = List.of("0007","0008","0009");
        List<String> actual = new ArrayList<>();
            for (String element : identifiers ){
                JSONObject json = new JSONObject().put(Constants.NOTATION,element);
                when(repoGestion.getResponseAsObject(ConceptConceptsQueries.lastConceptID())).thenReturn(json);
                actual.add(conceptsUtils.createID());
                }
        List<String> expected = List.of("c8","c9","c10");
        assertEquals(expected,actual);
    }

    @Test
    void shouldReturnFalseWhenCheckIfConceptExists() throws RmesException {
        String id= "2025";
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(false);
        assertFalse(conceptsUtils.checkIfConceptExists(id));
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenGetConceptById() throws RmesException {
        String id= "2025";
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(false);
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->conceptsUtils.getConceptById(id));
        Assertions.assertTrue(exception.getDetails().contains("This concept cannot be found in database"));
    }

    @Test
    void shouldCheckIfConceptExists() throws RmesException {
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists("mocked id"))).thenReturn(true);
        Assertions.assertTrue(conceptsUtils.checkIfConceptExists("mocked id"));
    }

    @Test
    void shouldDeleteConcept() throws RmesException {
        RdfUtils.setConfig(new ConfigStub());
        when(repoGestion.executeUpdate(ConceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT,"mocked id")),RdfUtils.conceptGraph().toString()))).thenReturn(HttpStatus.OK);
        when(repositoryPublication.executeUpdate(ConceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRIPublication(ObjectType.CONCEPT,"mocked id")),RdfUtils.conceptGraph().toString()))).thenReturn(HttpStatus.BAD_REQUEST);
        HttpStatus actual = conceptsUtils.deleteConcept("mocked id");
        assertEquals(HttpStatus.BAD_REQUEST,actual);
    }

    @Test
    void shouldGetRelatedConcepts() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(ConceptConceptsQueries.getRelatedConceptsQuery("mocked id"))).thenReturn(jsonArray);
        JSONArray actual = conceptsUtils.getRelatedConcepts("mocked id");
        assertEquals(jsonArray,actual);
    }

    @Test
    void shouldGetGraphsWithConcept() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(ConceptConceptsQueries.getGraphWithConceptQuery("mocked id"))).thenReturn(jsonArray);
        JSONArray actual = conceptsUtils.getGraphsWithConcept("mocked id");
        assertEquals(jsonArray,actual);
    }

    @Test
    void shouldCreateConceptWithSetConcept() throws RmesException {
        // Given
        String body = "{\"prefLabelLg1\":\"Test Concept\",\"creator\":\"testCreator\",\"contributor\":\"testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";

        JSONObject json = new JSONObject().put(Constants.NOTATION, "c0010");
        when(repoGestion.getResponseAsObject(ConceptConceptsQueries.lastConceptID())).thenReturn(json);

        // When
        String id = conceptsUtils.setConcept(body);

        // Then
        assertNotNull(id);
        assertEquals("c11", id);
    }

    @Test
    void shouldUpdateConceptWithSetConcept() throws RmesException {
        // Given
        String id = "c1";
        String body = "{\"prefLabelLg1\":\"Updated Concept\",\"creator\":\"testCreator\",\"contributor\":\"testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> conceptsUtils.setConcept(id, body));
    }

    @Test
    void shouldValidateConcepts() throws RmesException {
        // Given
        String body = "[\"c1\", \"c2\", \"c3\"]";
        SimpleValueFactory factory = SimpleValueFactory.getInstance();

        // Mock the getNarrowers query for each concept
        when(repositoryPublication.getResponseAsArray(anyString())).thenReturn(new JSONArray());

        // Mock repository connection and statements
        RepositoryConnection mockConnection = mock(RepositoryConnection.class);
        RepositoryResult mockStatements = mock(RepositoryResult.class);

        when(repoGestion.getConnection()).thenReturn(mockConnection);
        when(repoGestion.getStatements(any(), any())).thenReturn(mockStatements);
        when(mockStatements.hasNext()).thenReturn(false);

        // Mock getStatements on the connection itself (for publishMemberLinks)
        when(mockConnection.getStatements(any(), any(), any(), anyBoolean())).thenReturn(mockStatements);

        // Mock publicationUtils to return valid IRI/Resource objects
        Resource mockResource = factory.createIRI("http://example.com/concept");
        when(publicationUtils.tranformBaseURIToPublish(any())).thenReturn(mockResource);

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> conceptsUtils.conceptsValidation(body));
    }

    @Test
    void shouldCreateIDWhenNoConceptExists() throws RmesException {
        // Given
        when(repoGestion.getResponseAsObject(ConceptConceptsQueries.lastConceptID())).thenReturn(new JSONObject());

        // When
        String id = conceptsUtils.createID();

        // Then
        assertEquals("c0001", id);
    }

    @Test
    void shouldGetConceptByIdWithAltLabels() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Concept FR")
                .put("prefLabelLg2", "Concept EN");

        JSONArray altLabelLg1 = new JSONArray()
                .put(new JSONObject().put("altLabel", "Alt FR 1"))
                .put(new JSONObject().put("altLabel", "Alt FR 2"));

        JSONArray altLabelLg2 = new JSONArray()
                .put(new JSONObject().put("altLabel", "Alt EN 1"));

        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(true);
        when(repoGestion.getResponseAsObject(ConceptConceptsQueries.conceptQuery(id))).thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(anyString())).thenAnswer(invocation -> {
            String query = invocation.getArgument(0);
            if (query.contains("lg1")) {
                return altLabelLg1;
            } else {
                return altLabelLg2;
            }
        });

        // When
        JSONObject result = conceptsUtils.getConceptById(id);

        // Then
        assertNotNull(result);
        assertTrue(result.has(Constants.ALT_LABEL_LG1));
        assertTrue(result.has(Constants.ALT_LABEL_LG2));
    }

    @Test
    void shouldGetConceptByIdWithoutAltLabels() throws RmesException {
        // Given
        String id = "c1";
        JSONObject conceptJson = new JSONObject()
                .put("id", id)
                .put("prefLabelLg1", "Concept FR");

        JSONArray emptyArray = new JSONArray();

        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(true);
        when(repoGestion.getResponseAsObject(ConceptConceptsQueries.conceptQuery(id))).thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(emptyArray);

        // When
        JSONObject result = conceptsUtils.getConceptById(id);

        // Then
        assertNotNull(result);
        assertFalse(result.has(Constants.ALT_LABEL_LG1));
        assertFalse(result.has(Constants.ALT_LABEL_LG2));
    }

    }