package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StructureStructureStampCheckerTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private UriUtils uriUtils;

   @ Mock
    Config config;

    private StructureStructureStampChecker checker;

    @BeforeEach
    void setUp() {
        repositoryGestion = mock(RepositoryGestion.class);
        uriUtils = mock(UriUtils.class);
        config = mock(Config.class);

        when(uriUtils.getBaseUriGestion(ObjectType.STRUCTURE)).thenReturn("http://structure");

        when(config.getStructuresComponentsGraph()).thenReturn("http://graph");
        when(config.getStructuresGraph()).thenReturn("http://graph");

        RdfUtils.setUriUtils(uriUtils);
        StructureQueries.setConfig(config);
        checker = new StructureStructureStampChecker(repositoryGestion);
    }

    @Test
    void testGetStamps_withContributors() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject().put("contributors", "STAMP_1");
        JSONObject obj2 = new JSONObject().put("contributors", "STAMP_2");
        array.put(obj1);
        array.put(obj2);

        String expectedQuery = StructureQueries.getContributorsByStructureUri("http://structure/123");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("STAMP_1"));
        assertTrue(result.contains("STAMP_2"));
    }

    @Test
    void testGetStamps_withEmptyContributors() throws Exception {
        JSONArray array = new JSONArray();

        String expectedQuery = StructureQueries.getContributorsByStructureUri("http://structure/456");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("456");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStamps_withException() throws Exception {
        String expectedQuery = StructureQueries.getContributorsByStructureUri("http://structure/789");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenThrow(new RmesException("500", new Exception("Message")));

        List<String> result = checker.getStamps("789");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}