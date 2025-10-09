package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class CollectionsQueries extends GenericQueries{

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("collections/", fileName, params);
	}

	public static String collectionsQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return  buildRequest("getCollections.ftlh", params);
	}
	
	public static String collectionsDashboardQuery() {
		return "SELECT ?id ?label ?created ?modified ?isValidated ?creator \n"
			+ "(count(?member) AS ?nbMembers) \n"
			+ "WHERE { \n"
			+ "?collection rdf:type skos:Collection . \n"
			+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
			+ "?collection dcterms:title ?label . \n"
			+ "?collection dcterms:created ?created . \n"
			+ "OPTIONAL {?collection dcterms:modified ?modified} . \n"
			+ "?collection insee:isValidated ?isValidated \n"
			+ "OPTIONAL {?collection dc:creator ?creator} . \n"
			+ "FILTER (lang(?label) = '" + config.getLg1() + "')"
			+ "?collection skos:member ?member . \n"
			+ " } \n"
			+ "GROUP BY ?id ?label ?created ?modified ?isValidated ?creator \n"
			+ "ORDER BY ?label";	
	}
	
	public static String collectionsToValidateQuery() {
		return "SELECT DISTINCT ?id ?label ?creator \n"
			+ "WHERE { \n"
			+ "?collection rdf:type skos:Collection . \n"
			+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
			+ "?collection dcterms:title ?label . \n"
			+ "?collection dc:creator ?creator . \n"
			+ "?collection insee:isValidated 'false'^^xsd:boolean . \n"
			+ "FILTER (lang(?label) = '" + config.getLg1() + "') } \n"
			+ "ORDER BY ?label ";	
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

	public static String isCollectionExist(String id) {
		return "ASK { \n"
				+ "?collection ?b ?c . \n"
				+ "FILTER(STRENDS(STR(?collection),'/concepts/definitions/" + id + "')) . \n"
				+ "}";
	}
	
	  private CollectionsQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
