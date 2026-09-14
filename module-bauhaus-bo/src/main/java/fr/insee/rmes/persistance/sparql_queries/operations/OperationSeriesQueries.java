package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class OperationSeriesQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

    public OperationSeriesQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
    }

    private static final String ID_SERIES = "ID_SERIES";
    private static final String PRODUCTS_GRAPH = "PRODUCTS_GRAPH";
    private static final String STAMP = "STAMP";
    private static final String URI_SERIES = "URI_SERIES";
    private static final String ORGANIZATIONS_GRAPH = "ORGANIZATIONS_GRAPH";
    private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
    private static final String ORG_INSEE_GRAPH = "ORG_INSEE_GRAPH";
    private static final String LINK_PREDICATE = "LINK_PREDICATE";
    private static final String SERIES_URI_SUFFIX = "SERIES_URI_SUFFIX";
    private static final String SERIES_PATH = "/operations/serie/";

    public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
        params.put("LABEL", SparqlLiterals.literal(label, lang));
        params.put("URI_SUFFIX", SparqlLiterals.literal(SERIES_PATH + id));
        params.put("TYPE", "insee:StatisticalOperationSeries");
        return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
    }

    public String oneSeriesQuery(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("ID", SparqlLiterals.literal(id));
        params.put(SERIES_URI_SUFFIX, SparqlLiterals.literal(SERIES_PATH + id));

        return FreeMarkerUtils.buildRequest("operations/series/", "getSeriesById.ftlh", params);
    }

    public String getSeriesForSearch(String stamp) throws RmesException {
        Map<String, Object> params = initParams();
        if (stamp != null && !stamp.isEmpty()) {
            params.put("stamp", SparqlLiterals.literal(stamp));
        }
        return buildSeriesRequest("getSeriesForAdvancedSearchQuery.ftlh", params);
    }

    //////////////////////////
    //   Using .flth files  //
    //////////////////////////

    private Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
        params.put(ORGANIZATIONS_GRAPH, SparqlLiterals.iri(graphs.organizationsGraph()));
        params.put(ORG_INSEE_GRAPH, SparqlLiterals.iri(graphs.orgInseeGraph()));
        params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
        return params;
    }

    /**
     * Un identifiant vide signifie « toutes les séries » : la clé reste alors absente et le template
     * n'ajoute pas son FILTER (il teste {@code <#if SERIES_URI_SUFFIX??>}).
     */
    private static void putSeriesUriSuffix(Map<String, Object> params, String idSeries) {
        if (idSeries != null && !idSeries.isEmpty()) {
            params.put(SERIES_URI_SUFFIX, SparqlLiterals.literal(SERIES_PATH + idSeries));
        }
    }

    private String buildSeriesRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
    }

    /**
     * @param idSeries
     * @return String
     * @throws RmesException
     */
    public String getFamily(String idSeries) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(SERIES_URI_SUFFIX, SparqlLiterals.literal(SERIES_PATH + idSeries));
        return buildSeriesRequest("getSeriesFamilyQuery.ftlh", params);
    }

    /**
     * @param uriSeries
     * @return String
     * @throws RmesException
     */
    public String getCreatorsBySeriesUri(String uriSeries) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
        params.put(URI_SERIES, SparqlLiterals.iri(uriSeries));
        return buildSeriesRequest("getSeriesCreatorsByUriQuery.ftlh", params);
    }

    /**
     * @param idSeries
     * @return String
     * @throws RmesException
     */
    public String getCreatorsById(String idSeries) throws RmesException {
        Map<String, Object> params = initParams();
        putSeriesUriSuffix(params, idSeries);
        return buildSeriesRequest("getSeriesCreatorsByIdQuery.ftlh", params);
    }

    /**
     * @param idSeries
     * @return String
     * @throws RmesException
     */
    public String getGeneratedWith(String idSeries) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(SERIES_URI_SUFFIX, SparqlLiterals.literal(SERIES_PATH + idSeries));
        return buildSeriesRequest("getSeriesGeneratedWithQuery.ftlh", params);
    }

    /**
     * @param idSeries
     * @return String
     * @throws RmesException
     */
    public String getOperations(String idSeries) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(SERIES_URI_SUFFIX, SparqlLiterals.literal(SERIES_PATH + idSeries));
        return buildSeriesRequest("getSeriesOperationsQuery.ftlh", params);
    }

    /**
     * @param idSeries, linkPredicate
     * @return String
     * @throws RmesException
     */
    public String seriesLinks(String idSeries, IRI linkPredicate, String resultType) throws RmesException {
        Map<String, Object> params = initParams();
        putSeriesUriSuffix(params, idSeries);
        params.put(LINK_PREDICATE, SparqlLiterals.iri(linkPredicate.stringValue()));
        if (Constants.ORGANIZATIONS.equals(resultType)) {
            return buildSeriesRequest("getSeriesOrganizationsLinksQuery.ftlh", params);
        }
        return buildSeriesRequest("getSeriesLinksQuery.ftlh", params);
    }

    /**
     * @return String
     * @throws RmesException
     */
    public String seriesWithSimsQuery() throws RmesException {
        Map<String, Object> params = initParams();
        params.put("withSims", true);
        return buildSeriesRequest("getSeriesQuery.ftlh", params);
    }

    /**
     * @param stamps
     * @return String
     * @throws RmesException
     */
    public String seriesWithStampQuery(Set<Stamp> stamps, boolean isAdmin) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(
                STAMP,
                stamps.stream()
                        .map(stamp -> SparqlLiterals.literal(stamp.stamp()))
                        .toList());
        params.put("ADMIN", isAdmin);
        return buildSeriesRequest("getSeriesWithStampQuery.ftlh", params);
    }

    /**
     * @return String
     * @throws RmesException
     */
    public String seriesQuery() throws RmesException {
        Map<String, Object> params = initParams();
        params.put("withSims", false);
        return buildSeriesRequest("getSeriesQuery.ftlh", params);
    }

    public String checkIfSeriesExists(List<String> iris) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(
                URI_SERIES,
                iris.stream()
                        .map(iri -> Map.of("iri", SparqlLiterals.iri(iri), "literal", SparqlLiterals.literal(iri)))
                        .toList());
        return buildSeriesRequest("checkIfSeriesExists.ftlh", params);
    }

    public String getPublishedOperationsForSeries(String iri) throws RmesException {
        Map<String, Object> params = initParams();
        params.put("SERIES_IRI", SparqlLiterals.iri(iri));
        return buildSeriesRequest("getPublishedOperationsForSeries.ftlh", params);
    }
}
