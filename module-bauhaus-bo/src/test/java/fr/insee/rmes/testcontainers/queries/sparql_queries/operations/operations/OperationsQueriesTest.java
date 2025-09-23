package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.operations;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
class OperationsQueriesTest extends WithGraphDBContainer {
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

    @Test
    void should_return_operation() throws Exception {
        OpSeriesQueries.setConfig(new ConfigStub());

        JSONObject result = repositoryGestion.getResponseAsObject(OperationsQueries.operationQuery("s1447"));
        assertThat(result.getString("id")).hasToString("s1447");
        assertThat(result.getString("prefLabelLg1")).hasToString("Dispositif d'enquêtes permanentes des conditions de vie 2008");
        assertThat(result.getString("prefLabelLg2")).hasToString("Permanent living conditions survey 2008");
        assertThat(result.getString("altLabelLg2")).hasToString("EPCV scheme 2008");
        assertThat(result.getString("altLabelLg1")).hasToString("EPCV 2008");
        assertThat(result.getString("validationState")).hasToString("Validated");
        assertEquals("2024", result.getString("year"));

    }
}
