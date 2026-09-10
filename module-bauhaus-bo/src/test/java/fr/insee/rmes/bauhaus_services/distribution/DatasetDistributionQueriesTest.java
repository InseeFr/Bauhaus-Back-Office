package fr.insee.rmes.bauhaus_services.distribution;

import static org.mockito.ArgumentMatchers.eq;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DatasetDistributionQueriesTest {

    DatasetDistributionQueries datasetDistributionQueries =
            new DatasetDistributionQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void shouldCallGetDistributionQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/distribution>");
                    put("LG1", "\"fr\"");
                    put("LG2", "\"en\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map)))
                    .thenReturn("request");
            String query =
                    datasetDistributionQueries.getDistributions("http://rdf.insee.fr/graphes/catalogue/distribution");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDistributionByIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/distribution>");
                    put("LG1", "\"fr\"");
                    put("LG2", "\"en\"");
                    put("ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistribution.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetDistributionQueries.getDistribution(
                    "1", "http://rdf.insee.fr/graphes/catalogue/distribution");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDistributionByDistributionIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/distribution>");
                    put("LG1", "\"fr\"");
                    put("LG2", "\"en\"");
                    put("DATASET_ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetDistributionQueries.getDatasetDistributions(
                    "1", "http://rdf.insee.fr/graphes/catalogue/distribution");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetLastIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/distribution>");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq("distribution/"), eq("getLastDistributionId.ftlh"), eq(map)))
                    .thenReturn("request");
            String query =
                    datasetDistributionQueries.lastDistributionId("http://rdf.insee.fr/graphes/catalogue/distribution");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetContributorsByDistributionUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DISTRIBUTION_URI", "<http://bauhaus/catalogue/distribution/d1000>");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq("distribution/"), eq("getDistributionContributorsByUriQuery.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetDistributionQueries.getContributorsByDistributionUri(
                    "http://bauhaus/catalogue/distribution/d1000");
            Assertions.assertEquals("request", query);
        }
    }
}
