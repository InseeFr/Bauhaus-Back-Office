package fr.insee.rmes.persistance.sparql_queries.datasets;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DatasetDistributionQueries {

    private static final String ROOT_DIRECTORY = "distribution/";
    public static final String DATASET_GRAPH = "DATASET_GRAPH";
    public static final String ADMS_GRAPH = "ADMS_GRAPH";

    private final Config config;

    public DatasetDistributionQueries(Config config) {
        this.config = config;
    }

    public String getDistributions(String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, distributionGraph);
        params.put("DATASET_ID", "");
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributions.ftlh", params);
    }

    public String getDistributionsForSearch(String distributionGraph, String admsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, distributionGraph);
        params.put(ADMS_GRAPH, admsGraph);
        params.put("LG1", config.getLg1());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributionsForSearch.ftlh", params);
    }

    public String getDistribution(String id, String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, distributionGraph);
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistribution.ftlh", params);
    }

    public String getDatasetDistributions(String id, String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, distributionGraph);
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("DATASET_ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributions.ftlh", params);
    }

    public String lastDatasetId(String distributionGraph) throws RmesException {
        Map<String, Object> params = Map.of(DATASET_GRAPH, distributionGraph);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLastDatasetId.ftlh", params);
    }

    public String getContributorsByDistributionUri(String uri) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DISTRIBUTION_GRAPH_URI", uri);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributionContributorsByUriQuery.ftlh", params);
    }
}
