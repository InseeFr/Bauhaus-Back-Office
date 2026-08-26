package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsImplTest {

    @InjectMocks
    OperationsImpl operationsImpl;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    OperationIndicatorsQueries operationIndicatorsQueries;

    @Mock
    OperationsOperationQueries operationsOperationQueries;

    @Mock
    OperationSeriesQueries operationSeriesQueries;

    @Mock
    UserDecoder userDecoder;

    @Test
    void shouldSortSeriesWithStampIgnoringCaseAndDiacritics() throws Throwable {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("principal", "credentials"));
        when(userDecoder.fromPrincipal(any()))
                .thenReturn(Optional.of(new User("id", List.of(), Set.of("stamp"))));
        when(operationSeriesQueries.seriesWithStampQuery(any(), anyBoolean())).thenReturn("query");

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "Ez"));
        array.put(new JSONObject().put("id", "2").put("label", "ea"));
        array.put(new JSONObject().put("id", "3").put("label", "éb"));
        when(repoGestion.getResponseAsArray("query")).thenReturn(array);

        List<String> labels = new JSONArray(operationsImpl.getSeriesWithStamp()).toList().stream()
                .map(o -> (String) ((Map<?, ?>) o).get("label"))
                .toList();

        assertThat(labels).containsExactly("ea", "éb", "Ez");
    }

    @Test
    void shouldGetSeriesList() throws RmesException {
        when(operationSeriesQueries.seriesQuery()).thenReturn("query");

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        when(repoGestion.getResponseAsArray("query")).thenReturn(array);
        var series = operationsImpl.getSeries().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("alatLabel1", series.get(0).altLabel());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("elatLabel1", series.get(1).altLabel());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("élatLabel1", series.get(2).altLabel());

        assertEquals("1", series.get(3).id());
        assertEquals("label 1", series.get(3).label());
        assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
    }

    @Test
    void shouldGetOperationsList() throws RmesException {
        when(operationsOperationQueries.operationsQuery()).thenReturn("query");

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        when(repoGestion.getResponseAsArray("query")).thenReturn(array);
        var series = operationsImpl.getOperations().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("alatLabel1", series.get(0).altLabel());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("elatLabel1", series.get(1).altLabel());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("élatLabel1", series.get(2).altLabel());

        assertEquals("1", series.get(3).id());
        assertEquals("label 1", series.get(3).label());
        assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
    }



    @Test
    void shouldGetIndicatorsList() throws RmesException {
        when(operationIndicatorsQueries.indicatorsQuery()).thenReturn("query");

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        when(repoGestion.getResponseAsArray("query")).thenReturn(array);
        var series = operationsImpl.getIndicators().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("alatLabel1", series.get(0).altLabel());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("elatLabel1", series.get(1).altLabel());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("élatLabel1", series.get(2).altLabel());

        assertEquals("1", series.get(3).id());
        assertEquals("label 1", series.get(3).label());
        assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
    }

    @Test
    void shouldGetSeriesWithSims() throws RmesException {
        when(operationSeriesQueries.seriesWithSimsQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(new JSONArray().put("mockedExample"));
        String actual = operationsImpl.getSeriesWithSims();
        assertEquals("[\"mockedExample\"]",actual);
    }

    @Test
    void shouldGetOperationsWithoutReportWithoutCondition() throws RmesException {
        JSONObject firstJsonObject = new JSONObject().put("firstExample","mockedFirstExample");
        JSONObject secondJsonObject = new JSONObject().put("secondExample","mockedSecondExample");
        JSONArray jsonArrayTwoElements = new JSONArray().put(firstJsonObject).put(secondJsonObject);
        when(operationsOperationQueries.operationsWithoutSimsQuery("2025")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(jsonArrayTwoElements);
        assertEquals("[{\"firstExample\":\"mockedFirstExample\"},{\"secondExample\":\"mockedSecondExample\"}]",operationsImpl.getOperationsWithoutReport("2025"));
    }
}