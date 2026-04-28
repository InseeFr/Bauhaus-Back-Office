package fr.insee.rmes.persistance.sparql_queries.datasets;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component("sparqlDatasetQueries")
public class DatasetQueries {

    private static final String ROOT_DIRECTORY = "dataset/";
    public static final String DATASET_GRAPH = "DATASET_GRAPH";

    private final Config config;

    public DatasetQueries(Config config) {
        this.config = config;
    }

    public String getArchivageUnits() throws RmesException {
        Map<String, Object> params = Map.of("LG1", config.getLg1());
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getArchivageUnit.ftlh", params);
    }

    public String getDatasets(String datasetsGraph, Set<String> stamps) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("LG1", config.getLg1());

        if(stamps != null && !stamps.isEmpty()){
            params.put("STAMP", stamps);
        }
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasets.ftlh", params);
    }

    public String getDatasetsForSearch(String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("LG1", config.getLg1());

        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasetsForSearch.ftlh", params);
    }

    public String getDataset(String id, String datasetsGraph, String admsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ADMS_GRAPH", admsGraph);
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDataset.ftlh", params);
    }

    private String getDatasetArrays(String path, String datasetsGraph, String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, path, params);
    }

    public String getDatasetWasGeneratedIris(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetWasGeneratedIris.ftlh", datasetsGraph, id);
    }

    public String getDatasetCreators(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetCreators.ftlh", datasetsGraph, id);
    }

    public String getDatasetSpacialResolutions(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetSpacialResolutions.ftlh", datasetsGraph, id);
    }

    public String getDatasetStatisticalUnits(String id, String datasetsGraph) throws RmesException {
        return getDatasetArrays("getDatasetStatisticalUnits.ftlh", datasetsGraph, id);
    }

    public String lastDatasetId(String datasetsGraph) throws RmesException {
        Map<String, Object> params = Map.of(DATASET_GRAPH, datasetsGraph);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLastDatasetId.ftlh", params);
    }

    public String getContributorsByDatasetUri(String iri) throws RmesException {
        Map<String, Object> params = Map.of("DATASET_GRAPH_URI", iri);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasetsContributorsByUriQuery.ftlh", params);
    }

    public String getDatasetContributors(IRI iri, String datasetsGraph) throws RmesException {
        Map<String, Object> params = Map.of("GRAPH", datasetsGraph, "IRI", iri, "PREDICATE", "dc:contributor");
        return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
    }

    public String getDerivedDataset(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDerivedDataset.ftlh", params);
    }

    public String deleteTempWhiteNode(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "deleteDatasetTemporalCoverageWhiteNode.ftlh", params);
    }

    public String getDatasetDerivedFrom(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDatasetDerivedFrom.ftlh", params);
    }

    public String deleteDatasetQualifiedDerivationWhiteNode(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "deleteDatasetQualifiedDerivationWhiteNode.ftlh", params);
    }

    public String getLinkedDocuments(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLinkedDocuments.ftlh", params);
    }

    public String getKeywords(String id, String datasetsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, datasetsGraph);
        params.put("ID", id);
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getKeywords.ftlh", params);
    }
}
