package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeries;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySubject;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.shared_kernel.infrastructure.publication.ObjectPublished;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import java.util.List;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class GraphDBOperationFamilyRepositoryTest {

    private static final ValueFactory VALUES = SimpleValueFactory.getInstance();

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private OperationFamilyQueries operationFamilyQueries;

    @Mock
    private OperationQueries operationQueries;

    @Mock
    private RepositoryPublication repositoryPublication;

    @Mock
    private PublicationUtils publicationUtils;

    @Mock
    private ApplicationEventPublisher events;

    private GraphDBOperationFamilyRepository repository;

    @BeforeEach
    void setUp() {
        repository = new GraphDBOperationFamilyRepository(
                repositoryGestion,
                operationFamilyQueries,
                operationQueries,
                repositoryPublication,
                publicationUtils,
                new BauhausLanguagesProperties("fr", "en"),
                events);
    }

    @Test
    void get_families_returns_empty_list_when_no_families() throws RmesException {
        JSONArray emptyArray = new JSONArray();
        when(operationFamilyQueries.familiesQuery()).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        try (var _ = diacriticSorterReturning(List.of())) {

            List<PartialOperationFamily> result = repository.getFamilies();

            assertTrue(result.isEmpty());
            verify(repositoryGestion).getResponseAsArray("query");
        }
    }

    @Test
    void get_families_returns_sorted_list_when_families_exist() throws RmesException {
        JSONArray familiesArray = new JSONArray()
                .put(new JSONObject().put("id", "fam1").put("label", "Family 1"))
                .put(new JSONObject().put("id", "fam2").put("label", "Family 2"));

        List<PartialOperationFamily> expectedFamilies =
                List.of(new PartialOperationFamily("fam1", "Family 1"), new PartialOperationFamily("fam2", "Family 2"));

        when(operationFamilyQueries.familiesQuery()).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(familiesArray);

        try (var _ = diacriticSorterReturning(expectedFamilies)) {

            List<PartialOperationFamily> result = repository.getFamilies();

            assertEquals(expectedFamilies, result);
            verify(repositoryGestion).getResponseAsArray("query");
        }
    }

    @Test
    void get_family_returns_family_when_family_exists() throws RmesException {
        String familyId = "fam001";
        JSONObject familyJson = new JSONObject()
                .put("id", familyId)
                .put("prefLabelLg1", "Family Label")
                .put("validationState", "VALIDATED");

        when(operationFamilyQueries.familyQuery(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(familyJson);

        try (MockedStatic<XhtmlToMarkdownUtils> mockedUtils = mockStatic(XhtmlToMarkdownUtils.class)) {
            mockedUtils
                    .when(() -> XhtmlToMarkdownUtils.convertJSONObject(familyJson))
                    .then(invocation -> null);

            OperationFamily result = repository.getFamily(familyId);

            assertNotNull(result);
            assertEquals(familyId, result.id());
            assertEquals("Family Label", result.prefLabelLg1());
            assertEquals("VALIDATED", result.validationState());

            verify(repositoryGestion).getResponseAsObject("query");
            mockedUtils.verify(() -> XhtmlToMarkdownUtils.convertJSONObject(familyJson));
        }
    }

    @Test
    void get_family_throws_a_not_found_exception_when_family_not_found() throws RmesException {
        String familyId = "nonexistent";
        JSONObject emptyJson = new JSONObject();

        when(operationFamilyQueries.familyQuery(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(emptyJson);

        RmesException exception = assertThrows(RmesException.class, () -> repository.getFamily(familyId));

        assertEquals(HttpStatus.SC_NOT_FOUND, exception.getStatus());
        assertTrue(exception.getDetails().contains("Family " + familyId + " not found"));
    }

    @Test
    void get_family_series_returns_empty_list_when_no_series() throws RmesException {
        String familyId = "fam001";
        JSONArray emptyArray = new JSONArray();

        when(operationFamilyQueries.getSeries(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        List<OperationFamilySeries> result = repository.getFamilySeries(familyId);

        assertTrue(result.isEmpty());
        verify(repositoryGestion).getResponseAsArray("query");
    }

    @Test
    void get_family_series_returns_list_when_series_exist() throws RmesException {
        String familyId = "fam001";
        JSONArray seriesArray = new JSONArray()
                .put(new JSONObject().put("id", "s1").put("labelLg1", "Series 1"))
                .put(new JSONObject().put("id", "s2").put("labelLg1", "Series 2"));

        when(operationFamilyQueries.getSeries(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(seriesArray);

        List<OperationFamilySeries> result = repository.getFamilySeries(familyId);

        assertEquals(2, result.size());
        assertEquals("s1", result.get(0).id());
        assertEquals("Series 1", result.get(0).labelLg1());
        assertEquals("s2", result.get(1).id());
        assertEquals("Series 2", result.get(1).labelLg1());
    }

    @Test
    void get_family_subjects_returns_empty_list_when_no_subjects() throws RmesException {
        String familyId = "fam001";
        JSONArray emptyArray = new JSONArray();

        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        List<OperationFamilySubject> result = repository.getFamilySubjects(familyId);

        assertTrue(result.isEmpty());
        verify(repositoryGestion).getResponseAsArray("query");
    }

    @Test
    void get_family_subjects_returns_list_when_subjects_exist() throws RmesException {
        String familyId = "fam001";
        JSONArray subjectsArray = new JSONArray()
                .put(new JSONObject().put("id", "sub1").put("labelLg1", "Subject 1"))
                .put(new JSONObject().put("id", "sub2").put("labelLg1", "Subject 2"));

        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(subjectsArray);

        List<OperationFamilySubject> result = repository.getFamilySubjects(familyId);

        assertEquals(2, result.size());
        assertEquals("sub1", result.get(0).id());
        assertEquals("Subject 1", result.get(0).labelLg1());
        assertEquals("sub2", result.get(1).id());
        assertEquals("Subject 2", result.get(1).labelLg1());
    }

    @Test
    void get_full_family_returns_family_with_series_and_subjects() throws RmesException {
        String familyId = "fam001";

        // Mock base family
        JSONObject familyJson = new JSONObject().put("id", familyId).put("prefLabelLg1", "Family Label");

        // Mock series
        JSONArray seriesArray =
                new JSONArray().put(new JSONObject().put("id", "s1").put("labelLg1", "Series 1"));

        // Mock subjects
        JSONArray subjectsArray =
                new JSONArray().put(new JSONObject().put("id", "sub1").put("labelLg1", "Subject 1"));

        givenFullFamily(familyId, familyJson, seriesArray, subjectsArray);

        try (var _ = xhtmlConversionIgnored()) {
            OperationFamily result = repository.getFullFamily(familyId);

            assertNotNull(result);
            assertEquals(familyId, result.id());
            assertEquals("Family Label", result.prefLabelLg1());
            assertEquals(1, result.series().size());
            assertEquals("s1", result.series().getFirst().id());
            assertEquals(1, result.subjects().size());
            assertEquals("sub1", result.subjects().getFirst().id());
        }
    }

    @Test
    void get_full_family_returns_family_without_series_and_subjects_when_none_exist() throws RmesException {
        String familyId = "fam001";

        JSONObject familyJson = new JSONObject().put("id", familyId).put("prefLabelLg1", "Family Label");

        JSONArray emptyArray = new JSONArray();

        givenFullFamily(familyId, familyJson, emptyArray, emptyArray);

        try (var _ = xhtmlConversionIgnored()) {
            OperationFamily result = repository.getFullFamily(familyId);

            assertNotNull(result);
            assertEquals(familyId, result.id());
            assertTrue(result.series().isEmpty());
            assertTrue(result.subjects().isEmpty());
        }
    }

    @Test
    void get_series_with_report_maps_every_row_of_the_query() throws RmesException {
        when(operationFamilyQueries.seriesWithReportQuery("s1")).thenReturn("query");
        JSONArray rows = new JSONArray()
                .put(new JSONObject()
                        .put("id", "s1033")
                        .put("labelLg1", "Série")
                        .put("labelLg2", "Series")
                        .put("idSims", "1234"));
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(rows);

        var series = repository.getSeriesWithReport("s1");

        assertEquals(1, series.size());
        assertEquals("s1033", series.getFirst().id());
        assertEquals("Série", series.getFirst().labelLg1());
        assertEquals("Series", series.getFirst().labelLg2());
        assertEquals("1234", series.getFirst().idSims());
    }

    @Test
    void get_series_with_report_skips_the_empty_row_of_a_sparql_query_without_solution() throws RmesException {
        when(operationFamilyQueries.seriesWithReportQuery("s1")).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(new JSONArray().put(new JSONObject()));

        assertTrue(repository.getSeriesWithReport("s1").isEmpty());
    }

    @Test
    void publish_announces_the_publication_with_the_operations_graph() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> Optional.of("famille")));
        IRI familyIRI = VALUES.createIRI("http://bauhaus/famille/s1001");
        givenManagementTriples(VALUES.createStatement(familyIRI, SKOS.PREF_LABEL, VALUES.createLiteral("Famille")));

        repository.publish("s1001");

        ArgumentCaptor<ObjectPublished> event = ArgumentCaptor.forClass(ObjectPublished.class);
        verify(events).publishEvent(event.capture());
        assertEquals(
                new ObjectPublished(familyIRI, VALUES.createIRI("http://rdf.insee.fr/graphes/operations")),
                event.getValue());
    }

    private static MockedStatic<DiacriticSorter> diacriticSorterReturning(List<PartialOperationFamily> families) {
        MockedStatic<DiacriticSorter> mockedSorter = mockStatic(DiacriticSorter.class);
        mockedSorter
                .when(() -> DiacriticSorter.sort(any(JSONArray.class), eq(PartialOperationFamily[].class), any()))
                .thenReturn(families);
        return mockedSorter;
    }

    private static MockedStatic<XhtmlToMarkdownUtils> xhtmlConversionIgnored() {
        MockedStatic<XhtmlToMarkdownUtils> mockedUtils = mockStatic(XhtmlToMarkdownUtils.class);
        mockedUtils.when(() -> XhtmlToMarkdownUtils.convertJSONObject(any())).then(invocation -> null);
        return mockedUtils;
    }

    /** La famille, ses séries et ses sujets sont lus par trois requêtes distinctes. */
    private void givenFullFamily(String familyId, JSONObject familyJson, JSONArray series, JSONArray subjects)
            throws RmesException {
        when(operationFamilyQueries.familyQuery(familyId)).thenReturn("familyQuery");
        when(operationFamilyQueries.getSeries(familyId)).thenReturn("seriesQuery");
        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("subjectsQuery");

        when(repositoryGestion.getResponseAsObject("familyQuery")).thenReturn(familyJson);
        when(repositoryGestion.getResponseAsArray("seriesQuery")).thenReturn(series);
        when(repositoryGestion.getResponseAsArray("subjectsQuery")).thenReturn(subjects);
    }

    /** Les triplets de gestion que {@code publish} recopie vers le graphe de publication. */
    @SuppressWarnings("unchecked")
    private void givenManagementTriples(Statement statement) throws RmesException {
        RepositoryResult<Statement> statements = mock(RepositoryResult.class);
        when(statements.hasNext()).thenReturn(true, true, false);
        when(statements.next()).thenReturn(statement);
        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements);
        when(publicationUtils.tranformBaseURIToPublish(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
