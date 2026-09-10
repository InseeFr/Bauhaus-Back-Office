package fr.insee.rmes.bauhaus_services.concepts.concepts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.bauhaus_services.notes.NotesRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptsFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class LegacyConceptsRepositoryTest {

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private RepositoryPublication repositoryPublication;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private PublicationUtils publicationUtils;

    @Mock
    private NotesRepository notesRepository;

    @Mock
    private ConceptsService conceptsService;

    @Mock
    private CollectionsService collectionsService;

    private LegacyConceptsRepository legacyConceptsRepository;
    private ConceptsPublication conceptsPublication;
    private NoteManager noteManager;
    private ConceptConceptsQueries conceptConceptsQueries;

    @BeforeEach
    void setUp() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        conceptConceptsQueries =
                new ConceptConceptsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

        conceptsPublication = new ConceptsPublication(
                repoGestion, idGenerator, repositoryPublication, publicationUtils, conceptConceptsQueries, null);

        noteManager = new NoteManager(notesRepository);

        legacyConceptsRepository = new LegacyConceptsRepository(
                repoGestion,
                idGenerator,
                repositoryPublication,
                new BauhausLanguagesProperties("fr", "en"),
                publicationUtils,
                conceptsPublication,
                noteManager,
                5,
                conceptConceptsQueries,
                conceptsService,
                collectionsService);
    }

    @Test
    void shouldReturnGetConceptExportFileName() {

        ConceptsPublication conceptsPublication = new ConceptsPublication(null, null, null, null, null, null);
        NoteManager noteManager = new NoteManager(null);
        LegacyConceptsRepository legacyConceptsRepositoryExample = new LegacyConceptsRepository(
                null, null, null, null, null, conceptsPublication, noteManager, 19, null, null, null);

        ConceptForExport conceptForExport = new ConceptForExport();
        conceptForExport.setId("id");
        conceptForExport.setPrefLabelLg1("prefLabel1");
        conceptForExport.setPrefLabelLg2("prefLabel2");

        String response = legacyConceptsRepositoryExample.getConceptExportFileName(conceptForExport);

        assertEquals("idPreflabel1", response);
    }

    @Test
    void shouldCreateID() throws RmesException {
        List<String> identifiers = List.of("0007", "0008", "0009");
        List<String> actual = new ArrayList<>();
        for (String element : identifiers) {
            JSONObject json = new JSONObject().put(Constants.NOTATION, element);
            when(repoGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID()))
                    .thenReturn(json);
            actual.add(legacyConceptsRepository.createID());
        }
        List<String> expected = List.of("c8", "c9", "c10");
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnFalseWhenCheckIfConceptExists() throws RmesException {
        String id = "2025";
        when(repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists(id)))
                .thenReturn(false);
        assertFalse(legacyConceptsRepository.checkIfConceptExists(id));
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenGetConceptById() throws RmesException {
        String id = "2025";
        when(repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists(id)))
                .thenReturn(false);
        RmesException exception =
                assertThrows(RmesNotFoundException.class, () -> legacyConceptsRepository.getConceptById(id));
        Assertions.assertTrue(exception.getDetails().contains("This concept cannot be found in database"));
    }

    @Test
    void shouldCheckIfConceptExists() throws RmesException {
        when(repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists("c1000")))
                .thenReturn(true);
        Assertions.assertTrue(legacyConceptsRepository.checkIfConceptExists("c1000"));
    }

    @Test
    void shouldDeleteConcept() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        when(repoGestion.executeUpdate(conceptConceptsQueries.deleteConcept(
                        RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT, "c1000")),
                        RdfUtils.conceptGraph().toString())))
                .thenReturn(HttpStatus.OK);
        when(repositoryPublication.executeUpdate(conceptConceptsQueries.deleteConcept(
                        RdfUtils.toString(RdfUtils.objectIRIPublication(ObjectType.CONCEPT, "c1000")),
                        RdfUtils.conceptGraph().toString())))
                .thenReturn(HttpStatus.BAD_REQUEST);
        HttpStatus actual = legacyConceptsRepository.deleteConcept("c1000");
        assertEquals(HttpStatus.BAD_REQUEST, actual);
    }

    @Test
    void shouldGetRelatedConcepts() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(
                        conceptConceptsQueries.getRelatedConceptsQuery("http://bauhaus/concepts/definition/c1000")))
                .thenReturn(jsonArray);
        JSONArray actual = legacyConceptsRepository.getRelatedConcepts("http://bauhaus/concepts/definition/c1000");
        assertEquals(jsonArray, actual);
    }

    @Test
    void shouldGetGraphsWithConcept() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(
                        conceptConceptsQueries.getGraphWithConceptQuery("http://bauhaus/concepts/definition/c1000")))
                .thenReturn(jsonArray);
        JSONArray actual = legacyConceptsRepository.getGraphsWithConcept("http://bauhaus/concepts/definition/c1000");
        assertEquals(jsonArray, actual);
    }

    @Test
    void shouldCreateConceptWithSetConcept() throws RmesException {
        // Given
        String body =
                "{\"prefLabelLg1\":\"Test Concept\",\"creator\":\"https://testCreator\",\"contributor\":\"https://testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";

        JSONObject json = new JSONObject().put(Constants.NOTATION, "c0010");
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID()))
                .thenReturn(json);

        // When
        String id = legacyConceptsRepository.setConcept(body);

        // Then
        assertNotNull(id);
        assertEquals("c11", id);
    }

    @Test
    void shouldUpdateConceptWithSetConcept() throws RmesException {
        // Given
        String id = "c1";
        String body =
                "{\"prefLabelLg1\":\"Updated Concept\",\"creator\":\"https://testCreator\",\"contributor\":\"https://testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> legacyConceptsRepository.setConcept(id, body));
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

        // When
        legacyConceptsRepository.conceptsValidation(body);

        // Then - the validation writes validationState=Validated on the management graph
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).objectsValidation(anyList(), modelCaptor.capture());
        assertEquals(ValidationStatus.VALIDATED.getValue(), validationStateOf(modelCaptor.getValue()));
    }

    @Test
    void shouldCreateConceptAsUnpublished() throws RmesException {
        // Given
        String body =
                "{\"prefLabelLg1\":\"Test Concept\",\"creator\":\"https://testCreator\",\"contributor\":\"https://testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID()))
                .thenReturn(new JSONObject().put(Constants.NOTATION, "c0010"));

        // When
        legacyConceptsRepository.setConcept(body);

        // Then
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadConcept(any(), modelCaptor.capture(), any());
        assertEquals(ValidationStatus.UNPUBLISHED.getValue(), validationStateOf(modelCaptor.getValue()));
    }

    @Test
    void shouldMarkConceptAsModifiedWhenUpdatingValidatedConcept() throws RmesException {
        // Given an existing concept currently Validated
        String id = "c1";
        String body =
                "{\"prefLabelLg1\":\"Updated Concept\",\"creator\":\"https://testCreator\",\"contributor\":\"https://testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptCreated(id)))
                .thenReturn(new JSONObject());
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptValidationStatus(id)))
                .thenReturn(new JSONObject().put("state", ValidationStatus.VALIDATED.getValue()));

        // When updating it
        legacyConceptsRepository.setConcept(id, body);

        // Then it transitions to Modified (provisoire déjà publiée)
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadConcept(any(), modelCaptor.capture(), any());
        assertEquals(ValidationStatus.MODIFIED.getValue(), validationStateOf(modelCaptor.getValue()));
    }

    @Test
    void shouldStayUnpublishedWhenUpdatingUnpublishedConcept() throws RmesException {
        // Given an existing concept currently Unpublished
        String id = "c1";
        String body =
                "{\"prefLabelLg1\":\"Updated Concept\",\"creator\":\"https://testCreator\",\"contributor\":\"https://testContributor\",\"disseminationStatus\":\"http://example.com/status\"}";
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptCreated(id)))
                .thenReturn(new JSONObject());
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.getConceptValidationStatus(id)))
                .thenReturn(new JSONObject().put("state", ValidationStatus.UNPUBLISHED.getValue()));

        // When updating it
        legacyConceptsRepository.setConcept(id, body);

        // Then it stays Unpublished
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadConcept(any(), modelCaptor.capture(), any());
        assertEquals(ValidationStatus.UNPUBLISHED.getValue(), validationStateOf(modelCaptor.getValue()));
    }

    private static String validationStateOf(Model model) {
        for (Statement st : model) {
            if (st.getPredicate().equals(INSEE.VALIDATION_STATE)) {
                return st.getObject().stringValue();
            }
        }
        return null;
    }

    @Test
    void shouldCreateIDWhenNoConceptExists() throws RmesException {
        // Given
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.lastConceptID()))
                .thenReturn(new JSONObject());

        // When
        String id = legacyConceptsRepository.createID();

        // Then
        assertEquals("c0001", id);
    }

    @Test
    void shouldGetConceptByIdWithAltLabels() throws RmesException, ConceptsFetchException {
        // Given
        String id = "c1";
        JSONObject conceptJson =
                new JSONObject().put("id", id).put("prefLabelLg1", "Concept FR").put("prefLabelLg2", "Concept EN");

        JSONArray altLabelLg1 = new JSONArray()
                .put(new JSONObject().put("altLabel", "Alt FR 1"))
                .put(new JSONObject().put("altLabel", "Alt FR 2"));

        JSONArray altLabelLg2 = new JSONArray().put(new JSONObject().put("altLabel", "Alt EN 1"));

        when(repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists(id)))
                .thenReturn(true);
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.conceptQuery(id)))
                .thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(anyString())).thenAnswer(invocation -> {
            String query = invocation.getArgument(0);
            if (query.contains("lg1")) {
                return altLabelLg1;
            } else {
                return altLabelLg2;
            }
        });
        when(conceptsService.getCollectionIdsByConceptId(id)).thenReturn(Collections.emptyList());

        // When
        JSONObject result = legacyConceptsRepository.getConceptById(id);

        // Then
        assertNotNull(result);
        assertTrue(result.has(Constants.ALT_LABEL_LG1));
        assertTrue(result.has(Constants.ALT_LABEL_LG2));
    }

    @Test
    void shouldGetConceptByIdWithoutAltLabels() throws RmesException, ConceptsFetchException {
        // Given
        String id = "c1";
        JSONObject conceptJson = new JSONObject().put("id", id).put("prefLabelLg1", "Concept FR");

        JSONArray emptyArray = new JSONArray();

        when(repoGestion.getResponseAsBoolean(conceptConceptsQueries.checkIfExists(id)))
                .thenReturn(true);
        when(repoGestion.getResponseAsObject(conceptConceptsQueries.conceptQuery(id)))
                .thenReturn(conceptJson);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(emptyArray);
        when(conceptsService.getCollectionIdsByConceptId(id)).thenReturn(Collections.emptyList());

        // When
        JSONObject result = legacyConceptsRepository.getConceptById(id);

        // Then
        assertNotNull(result);
        assertFalse(result.has(Constants.ALT_LABEL_LG1));
        assertFalse(result.has(Constants.ALT_LABEL_LG2));
    }
}
