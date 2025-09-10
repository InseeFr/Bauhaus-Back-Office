package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StructureComponentStampCheckerTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private UriUtils uriUtils;

    @ Mock
    Config config;

    private StructureComponentStampChecker checker;

    @BeforeEach
    void setUp() {
        repositoryGestion = mock(RepositoryGestion.class);
        uriUtils = mock(UriUtils.class);
        config = mock(Config.class);

        when(uriUtils.getBaseUriGestion(ObjectType.ATTRIBUTE_PROPERTY)).thenReturn("http://attribute");

        when(config.getStructuresComponentsGraph()).thenReturn("http://graph");
        when(config.getStructuresGraph()).thenReturn("http://graph");

        RdfUtils.setUriUtils(uriUtils);
        StructureQueries.setConfig(config);
        checker = new StructureComponentStampChecker(repositoryGestion);
    }

    @Test
    void testGetStamps_withContributors() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject().put("contributors", "STAMP_1");
        JSONObject obj2 = new JSONObject().put("contributors", "STAMP_2");
        array.put(obj1);
        array.put(obj2);

        String expectedQuery = StructureQueries.getContributorsByComponentUri("http://attribute/123");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put("type", QB.ATTRIBUTE_PROPERTY.toString()));

        List<String> result = checker.getStamps("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("STAMP_1"));
        assertTrue(result.contains("STAMP_2"));
    }

    @Test
    void testGetStamps_withEmptyContributors() throws Exception {
        JSONArray array = new JSONArray();

        String expectedQuery = StructureQueries.getContributorsByComponentUri("http://attribute/456");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put("type", QB.ATTRIBUTE_PROPERTY.toString()));

        List<String> result = checker.getStamps("456");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStamps_withException() throws Exception {
        String expectedQuery = StructureQueries.getContributorsByComponentUri("http://attribute/789");
        when(repositoryGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put("type", QB.ATTRIBUTE_PROPERTY.toString()));
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenThrow(new RmesException("500", new Exception("Message")));

        List<String> result = checker.getStamps("789");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}