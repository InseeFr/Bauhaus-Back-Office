package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsImplTest {

    @InjectMocks
    OperationsImpl operationsImpl;

    @Mock
    RepositoryGestion repoGestion;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldGetSeriesList() throws RmesException {

        try (MockedStatic<OperationSeriesQueries> mockedFactory = Mockito.mockStatic(OperationSeriesQueries.class)) {
            mockedFactory.when(OperationSeriesQueries::seriesQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("value", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("value", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("value", "élabel 1").put("altLabel", "élatLabel1"));
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
            assertEquals("value 1", series.get(3).label());
            assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
        }

    }

    @Test
    void shouldGetOperationsList() throws RmesException {

        try (MockedStatic<OperationsOperationQueries> mockedFactory = Mockito.mockStatic(OperationsOperationQueries.class)) {
            mockedFactory.when(OperationsOperationQueries::operationsQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("value", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("value", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("value", "élabel 1").put("altLabel", "élatLabel1"));
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
            assertEquals("value 1", series.get(3).label());
            assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
        }

    }



    @Test
    void shouldGetIndicatorsList() throws RmesException {

        try (MockedStatic<OperationIndicatorsQueries> mockedFactory = Mockito.mockStatic(OperationIndicatorsQueries.class)) {
            mockedFactory.when(OperationIndicatorsQueries::indicatorsQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("value", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("value", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("value", "élabel 1").put("altLabel", "élatLabel1"));
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
            assertEquals("value 1", series.get(3).label());
            assertEquals("latLabel1 || latLabel2", series.get(3).altLabel());
        }
    }

    @Test
    void shouldGetSeriesWithSims() throws RmesException {
        when(repoGestion.getResponseAsArray(OperationSeriesQueries.seriesWithSimsQuery())).thenReturn( new JSONArray().put("mockedExample"));
        String actual = operationsImpl.getSeriesWithSims();
        assertEquals("[\"mockedExample\"]",actual);
    }

    @Test
    void shouldGetOperationsWithoutReportWithoutCondition() throws RmesException {
        JSONObject firstJsonObject = new JSONObject().put("firstExample","mockedFirstExample");
        JSONObject secondJsonObject = new JSONObject().put("secondExample","mockedSecondExample");
        JSONArray jsonArrayTwoElements = new JSONArray().put(firstJsonObject).put(secondJsonObject);
        when(repoGestion.getResponseAsArray(OperationsOperationQueries.operationsWithoutSimsQuery("2025"))).thenReturn(jsonArrayTwoElements);
        assertEquals("[{\"firstExample\":\"mockedFirstExample\"},{\"secondExample\":\"mockedSecondExample\"}]",operationsImpl.getOperationsWithoutReport("2025"));
    }
}