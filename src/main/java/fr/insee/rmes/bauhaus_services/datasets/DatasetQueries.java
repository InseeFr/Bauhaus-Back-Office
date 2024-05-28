package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;


public class DatasetQueries extends GenericQueries {

    private static final String ROOT_DIRECTORY = "dataset/";
    public static final String DATASET_GRAPH = "DATASET_GRAPH";

    public static String getArchivageUnits() throws RmesException {
        Map<String, Object> params = Map.of("LG1", config.getLg1());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getArchivageUnit.ftlh", params);
    }

    public static String getDatasets(String datasetsGraph, String stamp) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("LG1", config.getLg1());

        if(stamp != null){
            params.put("STAMP", stamp);
        }
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasets.ftlh", params);
    }

    public static String getDataset(String id, String datasetsGraph, String admsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ADMS_GRAPH", admsGraph);
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDataset.ftlh", params);
    }

    private static String getDatasetArrays(String path, String datasetsGraph, String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, path, params);
    }

    public static String getDatasetCreators(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetCreators.ftlh", datasetsGraph, id);
    }

    public static String getDatasetSpacialResolutions(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetSpacialResolutions.ftlh", datasetsGraph, id);
    }

    public static String getDatasetStatisticalUnits(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetStatisticalUnits.ftlh", datasetsGraph, id);
    }

    public static String lastDatasetId(String datasetsGraph) throws RmesException {
        Map<String, Object> params = Map.of(DATASET_GRAPH, datasetsGraph);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLastDatasetId.ftlh", params);
    }

    public static String getContributorsByDatasetUri(String iri) throws RmesException {
        Map<String, Object> params = Map.of("DATASET_GRAPH_URI", iri);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasetsContributorsByUriQuery.ftlh", params);
    }

    public static String getDatasetContributors(IRI iri, String datasetsGraph) throws RmesException {
        Map<String, Object> params = Map.of("GRAPH", datasetsGraph, "IRI", iri, "PREDICATE", "dc:contributor");
        return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
    }
}