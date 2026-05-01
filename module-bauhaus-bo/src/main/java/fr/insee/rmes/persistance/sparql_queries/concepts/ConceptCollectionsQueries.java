package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConceptCollectionsQueries {

	private final Config config;

	public ConceptCollectionsQueries(Config config) {
		this.config = config;
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("collections/", fileName, params);
	}

	public String collectionsQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return buildRequest("getCollections.ftlh", params);
	}

	public String collectionsDashboardQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return buildRequest("getCollectionsDashboard.ftlh", params);
	}

	public String collectionsToValidateQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return buildRequest("getCollectionsToValidate.ftlh", params);
	}

	public String collectionQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return buildRequest("getCollection.ftlh", params);
	}

	public String collectionMembersQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return buildRequest("getCollectionMembers.ftlh", params);
	}

	public String collectionConceptsQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("COLLECTION_ID", id);
		return buildRequest("getCollectionConcepts.ftlh", params);
	}

	public String collectionExistsById(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildRequest("collectionExistsById.ftlh", params);
	}

	public String getCollectionsByConceptId(String conceptId) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("CONCEPT_ID", conceptId);
		return buildRequest("getCollectionsByConceptId.ftlh", params);
	}

	public String findExistingCollectionIds(List<String> ids) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("IDS", ids);
		return buildRequest("findExistingCollectionIds.ftlh", params);
	}

	public String linkConceptToCollection(String collectionId, String conceptUri, String graph) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("COLLECTION_ID", collectionId);
		params.put("CONCEPT_URI", conceptUri);
		params.put("GRAPH", graph);
		return buildRequest("linkConceptToCollection.ftlh", params);
	}

	public String unlinkConceptFromCollection(String collectionId, String conceptUri, String graph) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("COLLECTION_ID", collectionId);
		params.put("CONCEPT_URI", conceptUri);
		params.put("GRAPH", graph);
		return buildRequest("unlinkConceptFromCollection.ftlh", params);
	}

}