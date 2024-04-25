package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
public class DistributionQueriesTest {
    @Autowired
    Config config;

    @Test
    void shouldCallGetDistributionQuery() throws RmesException {
        DistributionQueries.setConfig(config);
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
        DistributionQueries.setConfig(config);
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
        DistributionQueries.setConfig(config);
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
        DistributionQueries.setConfig(config);
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
        DistributionQueries.setConfig(config);
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
