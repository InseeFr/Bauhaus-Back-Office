package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/bauhaus_services/distribution/DistributionQueriesTest.java
=======
import fr.insee.rmes.config.ConfigStub;
<<<<<<< HEAD
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/bauhaus_services/distribution/DistributionQueriesTest.java
import fr.insee.rmes.onion.domain.exceptions.RmesException;
=======
import fr.insee.rmes.domain.exceptions.RmesException;
>>>>>>> 895fe5ae (refactor: migrate getFamily et getFamilies to the hexagonale architecture (#995))
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

class DistributionQueriesTest {
    @BeforeAll
    static void init(){
        DistributionQueries.setConfig(new ConfigStub());
    }
    @Test
    void shouldCallGetDistributionQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("DATASET_ID", "");
                put("LG1", "fr");
                put("LG2", "en");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDistributions("distribution-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDistributionByIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistribution.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDistribution("1", "distribution-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDistributionByDistributionIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("DATASET_ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDatasetDistributions("1", "distribution-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetLastIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getLastDatasetId.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.lastDatasetId("distribution-graph");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetContributorsByDistributionUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DISTRIBUTION_GRAPH_URI", "distribution-graph-uri");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributionContributorsByUriQuery.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getContributorsByDistributionUri("distribution-graph-uri");
            Assertions.assertEquals("request", query);
        }
    }
}
