package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class DatasetQueries extends GenericQueries {

    private static final String ROOT_DIRECTORY = "dataset/";

    public static String getDatasets() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DATASET_GRAPH", config.getDatasetsGraph());
        params.put("LG1", config.getLg1());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasets.ftlh", params);
    }

    public static String getDataset(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DATASET_GRAPH", config.getDatasetsGraph());
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDataset.ftlh", params);
    }

    public static String lastDatasetId() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DATASET_GRAPH", config.getDatasetsGraph());

        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLastDatasetId.ftlh", params);
    }
}
