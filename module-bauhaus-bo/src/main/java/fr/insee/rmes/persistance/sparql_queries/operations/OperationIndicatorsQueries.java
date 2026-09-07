package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationIndicatorsQueries {

	public static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	public static final String PRODUCTS_GRAPH = "PRODUCTS_GRAPH";
	public static final String PRODUCT_BASE_URI = "PRODUCT_BASE_URI";
	private static final String INDICATOR_URI_SUFFIX = "INDICATOR_URI_SUFFIX";

    private final BauhausUriProperties uris;
    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public OperationIndicatorsQueries(BauhausUriProperties uris, BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.uris = uris;
        this.languages = languages;
        this.graphs = graphs;
	}

	private String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/indicators/", fileName, params);
	}

	public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put("LABEL", SparqlLiterals.literal(label, lang));
		params.put("URI_SUFFIX", SparqlLiterals.literal("/produits/indicateur/" + id));
		params.put("TYPE", "insee:StatisticalIndicator");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public String getPublicationState(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put(Constants.ID, SparqlLiterals.literal(id));
		return buildIndicatorRequest("getPublicationStatusQuery.ftlh", params);
	}

	public String indicatorsQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put("PRODUCT_BASE_URI_PREFIX", SparqlLiterals.literal("/" + uris.productsBaseUri() + "/"));
		return buildIndicatorRequest("getIndicators.ftlh", params);
	}

	public String indicatorQuery(String id) throws RmesException {
		return indicatorFullObjectQuery(id, true);
	}

	private String indicatorFullObjectQuery(String id, boolean withLimit) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal("/produits/indicateur/" + id));
		params.put("WITH_LIMIT", withLimit);
		return buildIndicatorRequest("getIndicator.ftlh", params);
	}

	public String getCreatorsById(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal("/" + uris.productsBaseUri() + "/" + id));
		return buildIndicatorRequest("getCreatorsById.ftlh", params);
	}

	public String getPublishersById(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal(uris.productsBaseUri() + "/" + id));
		return buildIndicatorRequest("getPublishersById.ftlh", params);
	}

	public String getContributorsById(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal(uris.productsBaseUri() + "/" + id));
		return buildIndicatorRequest("getContributorsById.ftlh", params);
	}

	public String indicatorLinks(String id, IRI linkPredicate) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal("/" + uris.productsBaseUri() + "/" + id));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put("LINKPREDICATE", SparqlLiterals.iri(linkPredicate.stringValue()));
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
		params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		return buildIndicatorRequest("getIndicatorLinks.ftlh", params);
	}

	public String lastID() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put("PRODUCT_BASE_URI_PREFIX", SparqlLiterals.literal("/" + uris.productsBaseUri() + "/"));
		return buildIndicatorRequest("getLastIndicatorId.ftlh", params);
	}

	public String checkIfExists(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(PRODUCTS_GRAPH, SparqlLiterals.iri(graphs.productsGraph()));
		params.put(INDICATOR_URI_SUFFIX, SparqlLiterals.literal("/" + uris.productsBaseUri() + "/" + id));
		return buildIndicatorRequest("checkIfIndicatorExists.ftlh", params);
	}

	public String indicatorsWithSimsQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return buildIndicatorRequest("getIndicatorsWithSims.ftlh", params);
	}
}
