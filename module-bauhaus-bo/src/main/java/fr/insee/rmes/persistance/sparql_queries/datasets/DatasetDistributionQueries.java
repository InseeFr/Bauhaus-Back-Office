package fr.insee.rmes.persistance.sparql_queries.datasets;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DatasetDistributionQueries {

    private static final String ROOT_DIRECTORY = "distribution/";
    public static final String DATASET_GRAPH = "DATASET_GRAPH";
    public static final String ADMS_GRAPH = "ADMS_GRAPH";

    private final BauhausLanguagesProperties languages;

    public DatasetDistributionQueries(BauhausLanguagesProperties languages) {
        this.languages = languages;
    }

    public String getDistributions(String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, SparqlLiterals.iri(distributionGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributions.ftlh", params);
    }

    public String getDistributionsForSearch(String distributionGraph, String admsGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, SparqlLiterals.iri(distributionGraph));
        params.put(ADMS_GRAPH, SparqlLiterals.iri(admsGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributionsForSearch.ftlh", params);
    }

    public String getDistribution(String id, String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, SparqlLiterals.iri(distributionGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("ID", SparqlLiterals.literal(id));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistribution.ftlh", params);
    }

    public String getDatasetDistributions(String id, String distributionGraph) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(DATASET_GRAPH, SparqlLiterals.iri(distributionGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("DATASET_ID", SparqlLiterals.literal(id));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributions.ftlh", params);
    }

    public String lastDistributionId(String distributionGraph) throws RmesException {
        Map<String, Object> params = Map.of(DATASET_GRAPH, SparqlLiterals.iri(distributionGraph));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getLastDistributionId.ftlh", params);
    }

    public String getContributorsByDistributionUri(String uri) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("DISTRIBUTION_URI", SparqlLiterals.iri(uri));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getDistributionContributorsByUriQuery.ftlh", params);
    }
}
