package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class DistributionQueries extends GenericQueries {

    private static final String ROOT_DIRECTORY = "distribution/";

    public static String getDistributions() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DATASET_GRAPH", config.getDistributionsGraph());
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributions.ftlh", params);
    }

    public static String getDistribution(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DATASET_GRAPH", config.getDistributionsGraph());
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistribution.ftlh", params);
    }
}
