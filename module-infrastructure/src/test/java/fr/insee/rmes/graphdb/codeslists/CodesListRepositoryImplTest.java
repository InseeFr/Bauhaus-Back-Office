package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodesListRepositoryImplTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    private CodesListRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new CodesListRepositoryImpl(repositoryGestion);
    }

    @Test
    void shouldFindAllCodesListsWithPartialFalse() throws Exception {
        JSONArray mockResponse = new JSONArray();
        mockResponse.put(createMockCodesList("TEST001", "http://example.com/test1", "Label 1 FR", "Label 1 EN", "Range 1"));
        mockResponse.put(createMockCodesList("TEST002", "http://example.com/test2", "Label 2 FR", "Label 2 EN", "Range 2"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        List<CodesListDomain> result = repository.findAllCodesLists(false, "");

        assertNotNull(result);
        assertEquals(2, result.size());
        
        CodesListDomain first = result.get(0);
        assertEquals("TEST001", first.getId());
        assertEquals("http://example.com/test1", first.getUri());
        assertEquals("Label 1 FR", first.getLabelLg1());
        assertEquals("Label 1 EN", first.getLabelLg2());
        assertEquals("Range 1", first.getRange());

        verify(repositoryGestion).getResponseAsArray(anyString());
        verifyNoMoreInteractions(repositoryGestion);
    }

    @Test
    void shouldFindAllCodesListsWithPartialTrue() throws Exception {
        JSONArray mockResponse = new JSONArray();
        mockResponse.put(createMockCodesList("PARTIAL001", "http://example.com/partial1", "Partial Label 1 FR", "Partial Label 1 EN", "Partial Range 1"));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        List<CodesListDomain> result = repository.findAllCodesLists(true, "");

        assertNotNull(result);
        assertEquals(1, result.size());
        
        CodesListDomain first = result.get(0);
        assertEquals("PARTIAL001", first.getId());
        assertEquals("http://example.com/partial1", first.getUri());
        assertEquals("Partial Label 1 FR", first.getLabelLg1());
        assertEquals("Partial Label 1 EN", first.getLabelLg2());
        assertEquals("Partial Range 1", first.getRange());

        verify(repositoryGestion).getResponseAsArray(anyString());
        verifyNoMoreInteractions(repositoryGestion);
    }

    @Test
    void shouldReturnEmptyListWhenNoCodesListsFound() throws Exception {
        JSONArray emptyResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(emptyResponse);

        List<CodesListDomain> result = repository.findAllCodesLists(false, "");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repositoryGestion).getResponseAsArray(anyString());
    }

    @Test
    void shouldHandleCodesListsWithNullFields() throws Exception {
        JSONArray mockResponse = new JSONArray();
        mockResponse.put(createMockCodesListWithNulls("TEST003", "http://example.com/test3", "Label 3 FR", null, null));

        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        List<CodesListDomain> result = repository.findAllCodesLists(false, "");

        assertNotNull(result);
        assertEquals(1, result.size());
        
        CodesListDomain first = result.get(0);
        assertEquals("TEST003", first.getId());
        assertEquals("http://example.com/test3", first.getUri());
        assertEquals("Label 3 FR", first.getLabelLg1());
        assertNull(first.getLabelLg2());
        assertNull(first.getRange());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryThrowsException() throws Exception {
        when(repositoryGestion.getResponseAsArray(anyString()))
            .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> repository.findAllCodesLists(false, ""));

        assertEquals("Failed to retrieve codes lists", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Database connection failed", exception.getCause().getMessage());
    }

    @Test
    void shouldUseCorrectQueryBuilderForPartialCodesList() throws Exception {
        JSONArray mockResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        repository.findAllCodesLists(true, "");

        verify(repositoryGestion).getResponseAsArray(argThat(query -> 
            query.contains("skos:Collection")));
    }

    @Test
    void shouldUseCorrectQueryBuilderForCompleteCodesList() throws Exception {
        JSONArray mockResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        repository.findAllCodesLists(false, "");

        verify(repositoryGestion).getResponseAsArray(argThat(query -> 
            query.contains("skos:ConceptScheme")));
    }

    @Test
    void shouldSelectCorrectFields() throws Exception {
        JSONArray mockResponse = new JSONArray();
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(mockResponse);

        repository.findAllCodesLists(false, "");

        verify(repositoryGestion).getResponseAsArray(argThat(query -> 
            query.contains("id") && 
            query.contains("uri") && 
            query.contains("labelLg1") && 
            query.contains("labelLg2") && 
            query.contains("range")));
    }

    private org.json.JSONObject createMockCodesList(String id, String uri, String labelLg1, String labelLg2, String range) {
        org.json.JSONObject codesList = new org.json.JSONObject();
        codesList.put("id", id);
        codesList.put("uri", uri);
        codesList.put("labelLg1", labelLg1);
        codesList.put("labelLg2", labelLg2);
        codesList.put("range", range);
        return codesList;
    }

    private org.json.JSONObject createMockCodesListWithNulls(String id, String uri, String labelLg1, String labelLg2, String range) {
        org.json.JSONObject codesList = new org.json.JSONObject();
        codesList.put("id", id);
        codesList.put("uri", uri);
        codesList.put("labelLg1", labelLg1);
        if (labelLg2 != null) {
            codesList.put("labelLg2", labelLg2);
        }
        if (range != null) {
            codesList.put("range", range);
        }
        return codesList;
    }
}