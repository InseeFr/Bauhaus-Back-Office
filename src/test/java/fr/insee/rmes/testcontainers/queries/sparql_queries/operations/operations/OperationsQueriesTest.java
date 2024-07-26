package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OperationsQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
    }

    @Test
    void should_return_all_operations() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OperationsQueries.operationsQuery());
        assertEquals(390, result.length());

        for (var i = 0; i < result.length(); i++){
            assertNotNull(result.getJSONObject(i).getString("iri"));
        }
    }
}
