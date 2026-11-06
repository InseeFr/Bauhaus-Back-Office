package fr.insee.rmes.bauhaus_services.distribution;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.QueryCall;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatasetDistributionQueriesTest {

    private static final String DISTRIBUTION_GRAPH = "http://rdf.insee.fr/graphes/catalogue/distribution";

    DatasetDistributionQueries datasetDistributionQueries =
            new DatasetDistributionQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void shouldCallGetDistributionQuery() throws RmesException {
        assertDistributionRequest(
                "getDistributions.ftlh",
                graphAndLanguages(),
                () -> datasetDistributionQueries.getDistributions(DISTRIBUTION_GRAPH));
    }

    @Test
    void shouldCallGetDistributionByIdQuery() throws RmesException {
        assertDistributionRequest(
                "getDistribution.ftlh",
                graphAndLanguagesWith("ID", "\"1\""),
                () -> datasetDistributionQueries.getDistribution("1", DISTRIBUTION_GRAPH));
    }

    @Test
    void shouldCallGetDistributionByDistributionIdQuery() throws RmesException {
        assertDistributionRequest(
                "getDistributions.ftlh",
                graphAndLanguagesWith("DATASET_ID", "\"1\""),
                () -> datasetDistributionQueries.getDatasetDistributions("1", DISTRIBUTION_GRAPH));
    }

    @Test
    void shouldCallGetLastIdQuery() throws RmesException {
        assertDistributionRequest(
                "getLastDistributionId.ftlh",
                Map.of("DATASET_GRAPH", "<" + DISTRIBUTION_GRAPH + ">"),
                () -> datasetDistributionQueries.lastDistributionId(DISTRIBUTION_GRAPH));
    }

    @Test
    void shouldCallGetContributorsByDistributionUri() throws RmesException {
        assertDistributionRequest(
                "getDistributionContributorsByUriQuery.ftlh",
                Map.of("DISTRIBUTION_URI", "<http://bauhaus/catalogue/distribution/d1000>"),
                () -> datasetDistributionQueries.getContributorsByDistributionUri(
                        "http://bauhaus/catalogue/distribution/d1000"));
    }

    private static Map<String, Object> graphAndLanguages() {
        return Map.of("DATASET_GRAPH", "<" + DISTRIBUTION_GRAPH + ">", "LG1", "\"fr\"", "LG2", "\"en\"");
    }

    private static Map<String, Object> graphAndLanguagesWith(String key, String value) {
        return Map.of("DATASET_GRAPH", "<" + DISTRIBUTION_GRAPH + ">", "LG1", "\"fr\"", "LG2", "\"en\"", key, value);
    }

    /**
     * La requête rendue est celle du template {@code distribution/<template>}, construit avec exactement ces
     * paramètres.
     */
    private static void assertDistributionRequest(String template, Map<String, Object> expectedParams, QueryCall call)
            throws RmesException {
        assertQueryBuiltFromTemplate("distribution/", template, "request", call, expectedParams::equals);
    }
}
