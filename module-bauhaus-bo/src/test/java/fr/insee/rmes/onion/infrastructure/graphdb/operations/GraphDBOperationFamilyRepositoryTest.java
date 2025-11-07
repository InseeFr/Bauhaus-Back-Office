package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySeries;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySubject;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.OperationFamilyQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphDBOperationFamilyRepositoryTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private OperationFamilyQueries operationFamilyQueries;

    private GraphDBOperationFamilyRepository repository;

    private final boolean familiesRichTextNexStructure = true;

    @BeforeEach
    void setUp() {
        repository = new GraphDBOperationFamilyRepository(
                repositoryGestion,
                operationFamilyQueries,
                familiesRichTextNexStructure
        );
    }

    @Test
    void getFamilies_returnsEmptyList_whenNoFamilies() throws RmesException {
        JSONArray emptyArray = new JSONArray();
        when(operationFamilyQueries.familiesQuery()).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        try (MockedStatic<DiacriticSorter> mockedSorter = mockStatic(DiacriticSorter.class)) {
            mockedSorter.when(() -> DiacriticSorter.sort(
                    any(JSONArray.class),
                    eq(PartialOperationFamily[].class),
                    any()
            )).thenReturn(List.of());

            List<PartialOperationFamily> result = repository.getFamilies();

            assertTrue(result.isEmpty());
            verify(repositoryGestion).getResponseAsArray("query");
        }
    }

    @Test
    void getFamilies_returnsSortedList_whenFamiliesExist() throws RmesException {
        JSONArray familiesArray = new JSONArray()
                .put(new JSONObject().put("id", "fam1").put("value", "Family 1"))
                .put(new JSONObject().put("id", "fam2").put("value", "Family 2"));

        List<PartialOperationFamily> expectedFamilies = List.of(
                new PartialOperationFamily("fam1", "Family 1"),
                new PartialOperationFamily("fam2", "Family 2")
        );

        when(operationFamilyQueries.familiesQuery()).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(familiesArray);

        try (MockedStatic<DiacriticSorter> mockedSorter = mockStatic(DiacriticSorter.class)) {
            mockedSorter.when(() -> DiacriticSorter.sort(
                    any(JSONArray.class),
                    eq(PartialOperationFamily[].class),
                    any()
            )).thenReturn(expectedFamilies);

            List<PartialOperationFamily> result = repository.getFamilies();

            assertEquals(expectedFamilies, result);
            verify(repositoryGestion).getResponseAsArray("query");
        }
    }

    @Test
    void getFamily_returnsFamily_whenFamilyExists() throws RmesException {
        String familyId = "fam001";
        JSONObject familyJson = new JSONObject()
                .put("id", familyId)
                .put("prefLabelLg1", "Family Label")
                .put("validationState", "VALIDATED");

        when(operationFamilyQueries.familyQuery(familyId, familiesRichTextNexStructure)).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(familyJson);

        try (MockedStatic<XhtmlToMarkdownUtils> mockedUtils = mockStatic(XhtmlToMarkdownUtils.class)) {
            mockedUtils.when(() -> XhtmlToMarkdownUtils.convertJSONObject(familyJson)).then(invocation -> null);

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
    void getFamily_throwsException_whenFamilyNotFound() throws RmesException {
        String familyId = "nonexistent";
        JSONObject emptyJson = new JSONObject();

        when(operationFamilyQueries.familyQuery(familyId, familiesRichTextNexStructure)).thenReturn("query");
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(emptyJson);

        RmesException exception = assertThrows(RmesException.class, () -> repository.getFamily(familyId));

        assertEquals(HttpStatus.SC_BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getDetails().contains("Family " + familyId + " not found"));
    }

    @Test
    void getFamilySeries_returnsEmptyList_whenNoSeries() throws RmesException {
        String familyId = "fam001";
        JSONArray emptyArray = new JSONArray();

        when(operationFamilyQueries.getSeries(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        List<OperationFamilySeries> result = repository.getFamilySeries(familyId);

        assertTrue(result.isEmpty());
        verify(repositoryGestion).getResponseAsArray("query");
    }

    @Test
    void getFamilySeries_returnsList_whenSeriesExist() throws RmesException {
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
    void getFamilySubjects_returnsEmptyList_whenNoSubjects() throws RmesException {
        String familyId = "fam001";
        JSONArray emptyArray = new JSONArray();

        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(emptyArray);

        List<OperationFamilySubject> result = repository.getFamilySubjects(familyId);

        assertTrue(result.isEmpty());
        verify(repositoryGestion).getResponseAsArray("query");
    }

    @Test
    void getFamilySubjects_returnsList_whenSubjectsExist() throws RmesException {
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
    void getFullFamily_returnsFamilyWithSeriesAndSubjects() throws RmesException {
        String familyId = "fam001";
        
        // Mock base family
        JSONObject familyJson = new JSONObject()
                .put("id", familyId)
                .put("prefLabelLg1", "Family Label");
        
        // Mock series
        JSONArray seriesArray = new JSONArray()
                .put(new JSONObject().put("id", "s1").put("labelLg1", "Series 1"));
        
        // Mock subjects
        JSONArray subjectsArray = new JSONArray()
                .put(new JSONObject().put("id", "sub1").put("labelLg1", "Subject 1"));

        when(operationFamilyQueries.familyQuery(familyId, familiesRichTextNexStructure)).thenReturn("familyQuery");
        when(operationFamilyQueries.getSeries(familyId)).thenReturn("seriesQuery");
        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("subjectsQuery");
        
        when(repositoryGestion.getResponseAsObject("familyQuery")).thenReturn(familyJson);
        when(repositoryGestion.getResponseAsArray("seriesQuery")).thenReturn(seriesArray);
        when(repositoryGestion.getResponseAsArray("subjectsQuery")).thenReturn(subjectsArray);

        try (MockedStatic<XhtmlToMarkdownUtils> mockedUtils = mockStatic(XhtmlToMarkdownUtils.class)) {
            mockedUtils.when(() -> XhtmlToMarkdownUtils.convertJSONObject(any())).then(invocation -> null);

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
    void getFullFamily_returnsFamilyWithoutSeriesAndSubjects_whenNoneExist() throws RmesException {
        String familyId = "fam001";
        
        JSONObject familyJson = new JSONObject()
                .put("id", familyId)
                .put("prefLabelLg1", "Family Label");
        
        JSONArray emptyArray = new JSONArray();

        when(operationFamilyQueries.familyQuery(familyId, familiesRichTextNexStructure)).thenReturn("familyQuery");
        when(operationFamilyQueries.getSeries(familyId)).thenReturn("seriesQuery");
        when(operationFamilyQueries.getSubjects(familyId)).thenReturn("subjectsQuery");
        
        when(repositoryGestion.getResponseAsObject("familyQuery")).thenReturn(familyJson);
        when(repositoryGestion.getResponseAsArray("seriesQuery")).thenReturn(emptyArray);
        when(repositoryGestion.getResponseAsArray("subjectsQuery")).thenReturn(emptyArray);

        try (MockedStatic<XhtmlToMarkdownUtils> mockedUtils = mockStatic(XhtmlToMarkdownUtils.class)) {
            mockedUtils.when(() -> XhtmlToMarkdownUtils.convertJSONObject(any())).then(invocation -> null);

            OperationFamily result = repository.getFullFamily(familyId);

            assertNotNull(result);
            assertEquals(familyId, result.id());
            assertTrue(result.series().isEmpty());
            assertTrue(result.subjects().isEmpty());
        }
    }

    @Test
    void constructor_setsAllFields() {
        GraphDBOperationFamilyRepository repo = new GraphDBOperationFamilyRepository(
                repositoryGestion,
                operationFamilyQueries,
                false
        );

        assertNotNull(repo);
    }
}