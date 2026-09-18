package fr.insee.rmes.bauhaus_services.operations;

import static fr.insee.rmes.bauhaus_services.SortedLabelRows.assertSortedByLabelWithMergedAltLabels;
import static fr.insee.rmes.bauhaus_services.SortedLabelRows.rowsWithDuplicatesAndDiacritics;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
        when(userDecoder.fromPrincipal(any())).thenReturn(Optional.of(new User("id", List.of(), Set.of("stamp"))));
        when(operationSeriesQueries.seriesWithStampQuery(any(), anyBoolean())).thenReturn("query");

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "Ez"));
        array.put(new JSONObject().put("id", "2").put("label", "ea"));
        array.put(new JSONObject().put("id", "3").put("label", "éb"));
        when(repoGestion.getResponseAsArray("query")).thenReturn(array);

        List<String> labels = new JSONArray(operationsImpl.getSeriesWithStamp())
                .toList().stream()
                        .map(o -> (String) ((Map<?, ?>) o).get("label"))
                        .toList();

        assertThat(labels).containsExactly("ea", "éb", "Ez");
    }

    @Test
    void shouldGetSeriesList() throws RmesException {
        when(operationSeriesQueries.seriesQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(rowsWithDuplicatesAndDiacritics());
        var series = operationsImpl.getSeries().stream().toList();

        assertSortedByLabelWithMergedAltLabels(series, s -> s.id(), s -> s.label(), s -> s.altLabel());
    }

    @Test
    void shouldGetOperationsList() throws RmesException {
        when(operationsOperationQueries.operationsQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(rowsWithDuplicatesAndDiacritics());
        var series = operationsImpl.getOperations().stream().toList();

        assertSortedByLabelWithMergedAltLabels(series, s -> s.id(), s -> s.label(), s -> s.altLabel());
    }

    @Test
    void shouldGetIndicatorsList() throws RmesException {
        when(operationIndicatorsQueries.indicatorsQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(rowsWithDuplicatesAndDiacritics());
        var series = operationsImpl.getIndicators().stream().toList();

        assertSortedByLabelWithMergedAltLabels(series, s -> s.id(), s -> s.label(), s -> s.altLabel());
    }

    @Test
    void shouldGetSeriesWithSims() throws RmesException {
        when(operationSeriesQueries.seriesWithSimsQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(new JSONArray().put("mockedExample"));
        String actual = operationsImpl.getSeriesWithSims();
        assertEquals("[\"mockedExample\"]", actual);
    }

    @Test
    void shouldGetOperationsWithoutReportWithoutCondition() throws RmesException {
        JSONObject firstJsonObject = new JSONObject().put("firstExample", "mockedFirstExample");
        JSONObject secondJsonObject = new JSONObject().put("secondExample", "mockedSecondExample");
        JSONArray jsonArrayTwoElements = new JSONArray().put(firstJsonObject).put(secondJsonObject);
        when(operationsOperationQueries.operationsWithoutSimsQuery("2025")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(jsonArrayTwoElements);
        assertEquals(
                "[{\"firstExample\":\"mockedFirstExample\"},{\"secondExample\":\"mockedSecondExample\"}]",
                operationsImpl.getOperationsWithoutReport("2025"));
    }

    /**
     * Une requête SPARQL qui ne trouve rien renvoie tout de même une ligne, vide : la liste
     * d'opérations doit alors ressortir vide, et non porter un objet fantôme.
     */
    @Test
    void shouldDropTheEmptyRowWhenNoOperationIsWithoutReport() throws RmesException {
        when(operationsOperationQueries.operationsWithoutSimsQuery("s1000")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(new JSONArray().put(new JSONObject()));

        assertEquals("[]", operationsImpl.getOperationsWithoutReport("s1000"));
    }

    @Test
    void shouldListTheOperationsWithoutReport() throws RmesException {
        when(operationsOperationQueries.operationsWithoutSimsQuery("s1000")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query"))
                .thenReturn(new JSONArray().put(new JSONObject().put("id", "o1000")));

        assertThat(operationsImpl.getOperationsWithoutReport("s1000")).contains("o1000");
    }

    @Test
    void shouldDropTheEmptyRowWhenNoOperationHasAReport() throws RmesException {
        when(operationsOperationQueries.operationsWithSimsQuery("s1000")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query")).thenReturn(new JSONArray().put(new JSONObject()));

        assertEquals("[]", operationsImpl.getOperationsWithReport("s1000"));
    }

    @Test
    void shouldListTheOperationsWithAReport() throws RmesException {
        when(operationsOperationQueries.operationsWithSimsQuery("s1000")).thenReturn("query");
        when(repoGestion.getResponseAsArray("query"))
                .thenReturn(new JSONArray().put(new JSONObject().put("id", "o1000")));

        assertThat(operationsImpl.getOperationsWithReport("s1000")).contains("o1000");
    }

    @Test
    void shouldListTheIndicatorsThatCarryAReport() throws RmesException {
        when(operationIndicatorsQueries.indicatorsWithSimsQuery()).thenReturn("query");
        when(repoGestion.getResponseAsArray("query"))
                .thenReturn(new JSONArray().put(new JSONObject().put("id", "p1000")));

        assertThat(operationsImpl.getIndicatorsWithSims()).contains("p1000");
    }
}
