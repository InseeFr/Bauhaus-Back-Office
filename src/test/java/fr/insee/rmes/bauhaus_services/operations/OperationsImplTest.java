package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.families.OpFamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
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

        try (MockedStatic<OpSeriesQueries> mockedFactory = Mockito.mockStatic(OpSeriesQueries.class)) {
            mockedFactory.when(OpSeriesQueries::seriesQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
            when(repoGestion.getResponseAsArray("query")).thenReturn(array);
            var series = operationsImpl.getSeries().stream().toList();

            assertEquals(4, series.size());

            assertEquals("3", series.get(0).id());
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

    }

    @Test
    void shouldGetOperationsList() throws RmesException {

        try (MockedStatic<OperationsQueries> mockedFactory = Mockito.mockStatic(OperationsQueries.class)) {
            mockedFactory.when(OperationsQueries::operationsQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
            when(repoGestion.getResponseAsArray("query")).thenReturn(array);
            var series = operationsImpl.getOperations().stream().toList();

            assertEquals(4, series.size());

            assertEquals("3", series.get(0).id());
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

    }

    @Test
    void shouldGetFamiliesList() throws RmesException {

        try (MockedStatic<OpFamiliesQueries> mockedFactory = Mockito.mockStatic(OpFamiliesQueries.class)) {
            mockedFactory.when(OpFamiliesQueries::familiesQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
            when(repoGestion.getResponseAsArray("query")).thenReturn(array);
            var series = operationsImpl.getFamilies().stream().toList();

            assertEquals(4, series.size());

            assertEquals("3", series.get(0).id());
            assertEquals("alabel 1", series.get(0).label());

            assertEquals("2", series.get(1).id());
            assertEquals("elabel 1", series.get(1).label());

            assertEquals("4", series.get(2).id());
            assertEquals("élabel 1", series.get(2).label());

            assertEquals("1", series.get(3).id());
            assertEquals("label 1", series.get(3).label());
        }

    }

    @Test
    void shouldGetIndicatorsList() throws RmesException {

        try (MockedStatic<IndicatorsQueries> mockedFactory = Mockito.mockStatic(IndicatorsQueries.class)) {
            mockedFactory.when(IndicatorsQueries::indicatorsQuery).thenReturn("query");

            JSONArray array = new JSONArray();
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
            array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
            array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
            array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
            array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
            when(repoGestion.getResponseAsArray("query")).thenReturn(array);
            var series = operationsImpl.getIndicators().stream().toList();

            assertEquals(4, series.size());

            assertEquals("3", series.get(0).id());
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
    }

    @Test
    void shouldGetSeriesWithSims() throws RmesException {
        when(repoGestion.getResponseAsArray(OpSeriesQueries.seriesWithSimsQuery())).thenReturn( new JSONArray().put("mockedExample"));
        String actual = operationsImpl.getSeriesWithSims();
        assertEquals("[\"mockedExample\"]",actual);
    }

    @Test
    void shouldGetOperationsWithoutReportWithoutCondition() throws RmesException {
        JSONObject firstJsonObject = new JSONObject().put("firstExample","mockedFirstExample");
        JSONObject secondJsonObject = new JSONObject().put("secondExample","mockedSecondExample");
        JSONArray jsonArrayTwoElements = new JSONArray().put(firstJsonObject).put(secondJsonObject);
        when(repoGestion.getResponseAsArray(OperationsQueries.operationsWithoutSimsQuery("2025"))).thenReturn(jsonArrayTwoElements);
        assertEquals("[{\"firstExample\":\"mockedFirstExample\"},{\"secondExample\":\"mockedSecondExample\"}]",operationsImpl.getOperationsWithoutReport("2025"));
    };
}