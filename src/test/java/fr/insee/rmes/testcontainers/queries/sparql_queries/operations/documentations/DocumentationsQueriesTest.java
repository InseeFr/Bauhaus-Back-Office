package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.documentations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.families.OpFamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class DocumentationsQueriesTest extends WithGraphDBContainer {


    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("sims-metadata.trig");
    }

    @Test
    void should_return_rubrics_sans_object() throws Exception {
        DocumentationsQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(DocumentationsQueries.getAttributesQuery());
        assertEquals(96, result.length());


        for (int i = 0; i < result.length(); i++) {
            JSONObject obj = result.getJSONObject(i);
            if ("S.4".equalsIgnoreCase(obj.getString("id"))) {

                assertTrue(obj.getBoolean("sansObject"));

            }

            if ("S.6".equalsIgnoreCase(obj.getString("id"))) {

                assertFalse(obj.getBoolean("sansObject"));

            }
        }

    }
}
