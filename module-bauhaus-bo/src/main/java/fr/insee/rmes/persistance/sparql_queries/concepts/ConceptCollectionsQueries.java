package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConceptCollectionsQueries {

	private static final String COLLECTION_URI_SUFFIX = "COLLECTION_URI_SUFFIX";
	private static final String COLLECTIONS_PATH = "/concepts/definitions/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public ConceptCollectionsQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("collections/", fileName, params);
	}

	public String collectionsQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return buildRequest("getCollections.ftlh", params);
	}

	public String collectionsDashboardQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return buildRequest("getCollectionsDashboard.ftlh", params);
	}

	public String collectionsToValidateQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return buildRequest("getCollectionsToValidate.ftlh", params);
	}

	public String collectionQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(COLLECTION_URI_SUFFIX, SparqlLiterals.literal(COLLECTIONS_PATH + id));
		return buildRequest("getCollection.ftlh", params);
	}

	public String collectionMembersQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(COLLECTION_URI_SUFFIX, SparqlLiterals.literal(COLLECTIONS_PATH + id));
		return buildRequest("getCollectionMembers.ftlh", params);
	}

	public String collectionConceptsQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put("CONCEPT_GRAPH", SparqlLiterals.iri(graphs.conceptsGraph()));
		params.put("STRUCTURES_COMPONENTS_GRAPH", SparqlLiterals.iri(graphs.structuresComponentsGraph()));
		params.put("COLLECTION_ID", SparqlLiterals.literal(id));
		return buildRequest("getCollectionConcepts.ftlh", params);
	}

	public String collectionExistsById(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(COLLECTION_URI_SUFFIX, SparqlLiterals.literal(COLLECTIONS_PATH + id));
		return buildRequest("collectionExistsById.ftlh", params);
	}

	public String getCollectionsByConceptId(String conceptId) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("CONCEPT_ID", SparqlLiterals.literal(conceptId));
		return buildRequest("getCollectionsByConceptId.ftlh", params);
	}

	public String findExistingCollectionIds(List<String> ids) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("IDS", ids.stream().map(SparqlLiterals::literal).toList());
		return buildRequest("findExistingCollectionIds.ftlh", params);
	}

	public String findValidatedCollectionIds(List<String> ids) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("IDS", ids.stream().map(SparqlLiterals::literal).toList());
		return buildRequest("findValidatedCollectionIds.ftlh", params);
	}

	public String linkConceptToCollection(String collectionId, String conceptUri, String graph) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(COLLECTION_URI_SUFFIX, SparqlLiterals.literal(COLLECTIONS_PATH + collectionId));
		params.put("CONCEPT_URI", SparqlLiterals.iri(conceptUri));
		params.put("GRAPH", SparqlLiterals.iri(graph));
		return buildRequest("linkConceptToCollection.ftlh", params);
	}

	public String unlinkConceptFromCollection(String collectionId, String conceptUri, String graph) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(COLLECTION_URI_SUFFIX, SparqlLiterals.literal(COLLECTIONS_PATH + collectionId));
		params.put("CONCEPT_URI", SparqlLiterals.iri(conceptUri));
		params.put("GRAPH", SparqlLiterals.iri(graph));
		return buildRequest("unlinkConceptFromCollection.ftlh", params);
	}

}