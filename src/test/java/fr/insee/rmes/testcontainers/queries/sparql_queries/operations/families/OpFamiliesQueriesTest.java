package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.families.OpFamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@Tag("integration")
class OpFamiliesQueriesTest  extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
    }

    @Test
    void should_return_all_families() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpFamiliesQueries.familiesQuery());
        assertEquals(56, result.length());

        for (var i = 0; i < result.length(); i++){
            assertNotNull(result.getJSONObject(i).getString("id"));
            assertNotNull(result.getJSONObject(i).getString("label"));
        }
    }

    @Test
    void should_return_series() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OpFamiliesQueries.getSeries("s79"));
        assertEquals(3, result.length());

        assertEquals("s1178", result.getJSONObject(0).getString("id"));
        assertEquals("s1266", result.getJSONObject(1).getString("id"));
        assertEquals("s1279", result.getJSONObject(2).getString("id"));
    }
}
