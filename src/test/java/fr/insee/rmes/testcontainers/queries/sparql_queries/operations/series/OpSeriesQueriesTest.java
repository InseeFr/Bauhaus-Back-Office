package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OpSeriesQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
    }

    @Test
    void should_return_all_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.seriesWithSimsQuery());
        assertEquals(174, result.length());

        for (var i = 0; i < result.length(); i++){
            assertNotNull(result.getJSONObject(i).getString("iri"));
        }
    }

    @Test
    void should_return_all_series_and_operators() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.checkIfSeriesExists(List.of("http://bauhaus/operations/serie/s1028", "http://bauhaus/operations/operation/s1489")));
        assertEquals(2, result.length());
    }

    @Test
    void should_return_filter_missing_objects() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.checkIfSeriesExists(List.of("http://bauhaus/operations/serie/unknown", "http://bauhaus/operations/operation/s1489")));
        assertEquals(1, result.length());
    }

    @Test
    void should_return_published_operations_for_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpSeriesQueries.getPublishedOperationsForSeries("http://bauhaus/operations/serie/s1227"));
        assertEquals(1, result.length());
    }
}
