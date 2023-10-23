package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DistributionQueriesTest {
    @Mock
    Config config;

    @Test
    void shouldCallGetDistributionQuery() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getDistributionsGraph()).thenReturn("distribution-graph");
        DistributionQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("DATASET_ID", "");
                put("LG1", "fr");
                put("LG2", "en");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDistributions();
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetDistributionByIdQuery() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getDistributionsGraph()).thenReturn("distribution-graph");
        DistributionQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistribution.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDistribution("1");
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetDistributionByDistributionIdQuery() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getDistributionsGraph()).thenReturn("distribution-graph");
        DistributionQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("DATASET_ID", "1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getDistributions.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.getDatasetDistributions("1");
            Assertions.assertEquals(query, "request");
        }
    }

    @Test
    void shouldCallGetLastIdQuery() throws RmesException {
        when(config.getDistributionsGraph()).thenReturn("distribution-graph");
        DistributionQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("DATASET_GRAPH", "distribution-graph");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("distribution/"), eq("getLastDatasetId.ftlh"), eq(map))).thenReturn("request");
            String query = DistributionQueries.lastDatasetId();
            Assertions.assertEquals(query, "request");
        }
    }
}
