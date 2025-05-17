package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatasetDistributionStampCheckerTest {
    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private Environment environment;

    @Mock
    private UriUtils uriUtils;

    private DatasetDistributionStampChecker checker;

    @BeforeEach
    void setUp() {
        repositoryGestion = mock(RepositoryGestion.class);
        environment = mock(Environment.class);
        uriUtils = mock(UriUtils.class);

        when(environment.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.baseURI")).thenReturn("http://base/");
        when(environment.getProperty("fr.insee.rmes.bauhaus.datasets.record.baseURI")).thenReturn("datasets");
        when(environment.getProperty("fr.insee.rmes.bauhaus.baseGraph")).thenReturn("http://graph/");
        when(environment.getProperty("fr.insee.rmes.bauhaus.datasets.graph")).thenReturn("datasetsGraph");
        when(uriUtils.getBaseUriGestion(ObjectType.DISTRIBUTION)).thenReturn("http://distribution");

        RdfUtils.setUriUtils(uriUtils);

        checker = new DatasetDistributionStampChecker(environment, repositoryGestion);
    }

    @Test
    void testGetStamps_withContributors() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject().put("contributors", "STAMP_1");
        JSONObject obj2 = new JSONObject().put("contributors", "STAMP_2");
        array.put(obj1);
        array.put(obj2);

        String expectedQuery = DistributionQueries.getContributorsByDistributionUri("http://distribution/123");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("STAMP_1"));
        assertTrue(result.contains("STAMP_2"));
    }

    @Test
    void testGetStamps_withEmptyContributors() throws Exception {
        JSONArray array = new JSONArray();

        String expectedQuery = DistributionQueries.getContributorsByDistributionUri("http://distribution/456");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("456");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStamps_withException() throws Exception {
        String expectedQuery = DistributionQueries.getContributorsByDistributionUri("http://distribution/789");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenThrow(new RmesException("500", new Exception("Message")));

        List<String> result = checker.getStamps("789");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}