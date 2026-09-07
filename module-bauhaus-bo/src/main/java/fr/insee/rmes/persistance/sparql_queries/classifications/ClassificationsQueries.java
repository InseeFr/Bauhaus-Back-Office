package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

@Component
public class ClassificationsQueries {

	public static final String CLASSIFICATIONS = "classifications/";
	private static final String CLASSIFICATION_URI_PATTERN = "CLASSIFICATION_URI_PATTERN";
	private static final String CODES_PATH = "/codes/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public ClassificationsQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String classificationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassifications.ftlh", params);
	}

	public String getGraphUriById(String classifId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classifId + "/"));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getGraphUriById.ftlh", params);
	}

	public String classificationQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + id + "/"));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put("GRAPH", SparqlLiterals.iri(graphs.classifFamiliesGraph()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassification.ftlh", params);
	}

	public String classificationQueryUri(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + id + "/"));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationUri.ftlh", params);
	}

	public String classificationItemsQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + id + "/"));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItems.ftlh", params);
	}

	public String classificationsUriById(String[] ids) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IDS", Arrays.stream(ids).map(SparqlLiterals::literal).toList());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationsUriById.ftlh", params);
	}

	public String classificationItemQueryUri(String classificationId, String itemId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classificationId + "/"));
		params.put("CLASSIFICATION_ITEM_ID", SparqlLiterals.literal(itemId));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItemUri.ftlh", params);
	}
}