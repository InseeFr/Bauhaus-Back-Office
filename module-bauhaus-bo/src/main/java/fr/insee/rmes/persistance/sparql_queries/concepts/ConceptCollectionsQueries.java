package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionPublicationStatusFilter;

import java.util.HashMap;
import java.util.Map;

public class ConceptCollectionsQueries extends GenericQueries{

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("collections/", fileName, params);
	}

	public static String collectionsQuery(CollectionPublicationStatusFilter filter) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("FILTER_UNPUBLISHED", filter.isUnpublished());
		return  buildRequest("getCollections.ftlh", params);
	}
	
	public static String collectionsDashboardQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return buildRequest("getCollectionsDashboard.ftlh", params);
	}
	
	public static String collectionsToValidateQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return buildRequest("getCollectionsToValidate.ftlh", params);
	}
	
	public static String collectionQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return  buildRequest("getCollection.ftlh", params);
	}
	
	public static String collectionMembersQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return  buildRequest("getCollectionMembers.ftlh", params);
	}

	public static String collectionConceptsQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CONCEPT_GRAPH", config.getConceptsGraph());
		params.put("STRUCTURES_COMPONENTS_GRAPH", config.getStructuresComponentsGraph());
		params.put("COLLECTION_ID", id);
		return  buildRequest("getCollectionConcepts.ftlh", params);
	}

	public static String isCollectionExist(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildRequest("isCollectionExist.ftlh", params);
	}
	
	  private ConceptCollectionsQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
