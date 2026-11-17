package fr.insee.rmes.bauhaus_services.datasets;

import static fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.assertQueryBuiltFromTemplate;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.QueryCall;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DatasetQueriesTest {

    private static final String DATASET_GRAPH = "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees";

    DatasetQueries datasetQueries = new DatasetQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void shouldCallGetDatasetsQueryWithoutStamp() throws RmesException {
        assertDatasetRequest(
                "getDatasets.ftlh",
                Map.of("LG1", "\"fr\"", "DATASET_GRAPH", "<" + DATASET_GRAPH + ">"),
                () -> datasetQueries.getDatasets(DATASET_GRAPH, Set.of()));
    }

    @Test
    void shouldCallGetDatasetsQueryWithStamp() throws RmesException {
        assertDatasetRequest(
                "getDatasets.ftlh",
                Map.of("LG1", "\"fr\"", "DATASET_GRAPH", "<" + DATASET_GRAPH + ">", "STAMP", List.of("\"stamp\"")),
                () -> datasetQueries.getDatasets(DATASET_GRAPH, Set.of("stamp")));
    }

    @Test
    void shouldCallGetDatasetQuery() throws RmesException {
        assertDatasetRequest(
                "getDataset.ftlh",
                Map.of(
                        "LG1", "\"fr\"",
                        "LG2", "\"en\"",
                        "DATASET_GRAPH", "<" + DATASET_GRAPH + ">",
                        "ADMS_GRAPH", "<http://rdf.insee.fr/graphes/adms>",
                        "ID", "\"1\""),
                () -> datasetQueries.getDataset("1", DATASET_GRAPH, "http://rdf.insee.fr/graphes/adms"));
    }

    @Test
    void getDatasetsForSearchShouldExposeAltIdentifierProjection() throws RmesException {
        String query = datasetQueries.getDatasetsForSearch(DATASET_GRAPH, "http://rdf.insee.fr/graphes/adms");
        Assertions.assertTrue(
                query.contains("?altIdentifier"),
                "Rendered template must select ?altIdentifier so the search results expose the alternative identifier");
        Assertions.assertTrue(
                query.contains("adms:identifier"),
                "Rendered template must declare the adms:identifier OPTIONAL clause");
        Assertions.assertTrue(
                query.contains("skos:notation"),
                "Rendered template must use skos:notation to fetch the alternative identifier");
        Assertions.assertTrue(
                query.contains("FROM <http://rdf.insee.fr/graphes/adms>"),
                "Rendered template must include the adms graph to resolve altIdentifier triples");
    }

    @Test
    void shouldCallGetDatasetCreatorsQuery() throws RmesException {
        assertDatasetRequest(
                "getDatasetCreators.ftlh",
                datasetGraphAndId1(),
                () -> datasetQueries.getDatasetCreators("1", DATASET_GRAPH));
    }

    @Test
    void shouldCallGetDatasetSpacialResolutionsQuery() throws RmesException {
        assertDatasetRequest(
                "getDatasetSpacialResolutions.ftlh",
                datasetGraphAndId1(),
                () -> datasetQueries.getDatasetSpacialResolutions("1", DATASET_GRAPH));
    }

    @Test
    void shouldCallGetDatasetStatisticalUnitsQuery() throws RmesException {
        assertDatasetRequest(
                "getDatasetStatisticalUnits.ftlh",
                datasetGraphAndId1(),
                () -> datasetQueries.getDatasetStatisticalUnits("1", DATASET_GRAPH));
    }

    @Test
    void shouldCallGetLastDatasetIdQuery() throws RmesException {
        assertDatasetRequest(
                "getLastDatasetId.ftlh",
                Map.of("DATASET_GRAPH", "<" + DATASET_GRAPH + ">"),
                () -> datasetQueries.lastDatasetId(DATASET_GRAPH));
    }

    @Test
    void shouldCallGetContributorsByDatasetUri() throws RmesException {
        assertDatasetRequest(
                "getDatasetsContributorsByUriQuery.ftlh",
                Map.of("DATASET_URI", "<http://bauhaus/catalogue/jeuDeDonnees/d1000>"),
                () -> datasetQueries.getContributorsByDatasetUri("http://bauhaus/catalogue/jeuDeDonnees/d1000"));
    }

    private static Map<String, Object> datasetGraphAndId1() {
        return Map.of("DATASET_GRAPH", "<" + DATASET_GRAPH + ">", "ID", "\"1\"");
    }

    /** La requête rendue est celle du template {@code dataset/<template>}, construit avec exactement ces paramètres. */
    private static void assertDatasetRequest(String template, Map<String, Object> expectedParams, QueryCall call)
            throws RmesException {
        assertQueryBuiltFromTemplate("dataset/", template, "request", call, expectedParams::equals);
    }
}
