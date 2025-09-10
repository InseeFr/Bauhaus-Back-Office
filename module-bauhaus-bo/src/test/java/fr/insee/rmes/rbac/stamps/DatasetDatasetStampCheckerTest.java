package fr.insee.rmes.rbac.stamps;

import fr.insee.rmes.bauhaus_services.datasets.DatasetQueries;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatasetDatasetStampCheckerTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private Environment environment;

    private DatasetDatasetStampChecker checker;

    @BeforeEach
    void setUp() {
        repositoryGestion = mock(RepositoryGestion.class);
        environment = mock(Environment.class);

        when(environment.getProperty("fr.insee.rmes.bauhaus.sesame.gestion.baseURI")).thenReturn("http://base/");
        when(environment.getProperty("fr.insee.rmes.bauhaus.datasets.record.baseURI")).thenReturn("datasets");
        when(environment.getProperty("fr.insee.rmes.bauhaus.baseGraph")).thenReturn("http://graph/");
        when(environment.getProperty("fr.insee.rmes.bauhaus.datasets.graph")).thenReturn("datasetsGraph");

        checker = new DatasetDatasetStampChecker(environment, repositoryGestion);
    }

    @Test
    void testGetStamps_withContributors() throws Exception {
        JSONArray array = new JSONArray();
        JSONObject obj1 = new JSONObject().put("contributor", "STAMP_1");
        JSONObject obj2 = new JSONObject().put("contributor", "STAMP_2");
        array.put(obj1);
        array.put(obj2);

        IRI expectedIRI = fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils.createIRI("http://base/datasets/123");
        String expectedQuery = DatasetQueries.getDatasetContributors(expectedIRI, "http://graph/datasetsGraph");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("123");

        assertEquals(2, result.size());
        assertTrue(result.contains("STAMP_1"));
        assertTrue(result.contains("STAMP_2"));
    }

    @Test
    void testGetStamps_withEmptyContributors() throws Exception {
        JSONArray array = new JSONArray(); // empty array

        IRI expectedIRI = fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils.createIRI("http://base/datasets/456");
        String expectedQuery = DatasetQueries.getDatasetContributors(expectedIRI, "http://graph/datasetsGraph");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenReturn(array);

        List<String> result = checker.getStamps("456");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetStamps_withException() throws Exception {
        IRI expectedIRI = fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils.createIRI("http://base/datasets/789");
        String expectedQuery = DatasetQueries.getDatasetContributors(expectedIRI, "http://graph/datasetsGraph");
        when(repositoryGestion.getResponseAsArray(expectedQuery)).thenThrow(new RmesException("500", new Exception("Message")));

        List<String> result = checker.getStamps("789");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
