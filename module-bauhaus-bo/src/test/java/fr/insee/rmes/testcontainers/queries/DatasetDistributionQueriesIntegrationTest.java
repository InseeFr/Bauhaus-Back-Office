package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
class DatasetDistributionQueriesIntegrationTest extends WithGraphDBContainer {

    private static final String DISTRIBUTION_GRAPH = "http://rdf.insee.fr/graphes/catalogue-distributions-test";
    private static final String ADMS_GRAPH = "http://rdf.insee.fr/graphes/adms-distributions-test";

    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    DatasetDistributionQueries datasetDistributionQueries = new DatasetDistributionQueries(new BauhausLanguagesProperties("fr", "en"));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("distributions-pour-tests.trig");
    }

    private JSONObject rowOfDistribution(String distributionId) throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(
                datasetDistributionQueries.getDistributionsForSearch(DISTRIBUTION_GRAPH, ADMS_GRAPH));
        for (int i = 0; i < result.length(); i++) {
            JSONObject row = result.getJSONObject(i);
            if (distributionId.equals(row.optString("distributionId"))) {
                return row;
            }
        }
        throw new AssertionError("No row found for distribution " + distributionId + " in " + result);
    }

    @Test
    void should_expose_dataset_modification_date_under_the_projected_updated_variable() throws Exception {
        JSONObject row = rowOfDistribution("distributionAvecDates");

        // La colonne s'appelle `updated` : c'est le nom projeté par le SELECT, celui du record
        // DistributionsForSearch et celui sur lequel le front filtre (filterKeyDate("updated")).
        assertEquals("2021-03-01T00:00:00.000", row.optString("updated"));
    }

    @Test
    void should_expose_dataset_creation_date_and_distribution_dates() throws Exception {
        JSONObject row = rowOfDistribution("distributionAvecDates");

        assertEquals("2020-03-01T00:00:00.000", row.optString("created"));
        assertEquals("2022-03-01T00:00:00.000", row.optString("distributionCreated"));
        assertEquals("2023-03-01T00:00:00.000", row.optString("distributionUpdated"));
    }
}
