package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationSeriesStampCheckerTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private UriUtils uriUtils;

    @Mock
    Config config;

    private OperationSeriesStampChecker checker;

    @BeforeEach
    void setUp() {
        repositoryGestion = mock(RepositoryGestion.class);
        uriUtils = mock(UriUtils.class);
        config = mock(Config.class);

        OpSeriesQueries.setConfig(config);
        when(config.getOperationsGraph()).thenReturn("graph");

        when(uriUtils.getBaseUriGestion(ObjectType.SERIES)).thenReturn("http://series");

        RdfUtils.setUriUtils(uriUtils);
        checker = new OperationSeriesStampChecker(repositoryGestion);
    }

    @Test
    void testGetStamps_withContributors() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject().put("creators", "STAMP_1");
        JSONObject obj2 = new JSONObject().put("creators", "STAMP_2");
        array.put(obj1);
        array.put(obj2);

        String expectedQuery = OpSeriesQueries.getCreatorsBySeriesUri("http://series/123");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("STAMP_1"));
        assertTrue(result.contains("STAMP_2"));
    }

    @Test
    void testGetStamps_withEmptyContributors() throws Exception {
        JSONArray array = new JSONArray();

        String expectedQuery = OpSeriesQueries.getCreatorsBySeriesUri("http://series/456");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("456");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStamps_withException() throws Exception {
        String expectedQuery = OpSeriesQueries.getCreatorsBySeriesUri("http://series/789");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenThrow(new RmesException("500", new Exception("Message")));

        List<String> result = checker.getStamps("789");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}