package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class ConceptsQueries extends GenericQueries {

	private static final String URI_CONCEPT = "uriConcept";
	public static final String CONCEPTS_GRAPH = "CONCEPTS_GRAPH";

	private ConceptsQueries() {
		throw new IllegalStateException("Utility class");
	}


	public static String lastConceptID() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getLastConceptId.ftlh", params);
	}


	public static String conceptsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConcepts.ftlh", params);
	}

	public static String conceptsSearchQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptsForAdvancedSearch.ftlh", params);
	}


	public static String conceptsToValidateQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptsToValidateQuery.ftlh", params);
	}


	public static String conceptQuery(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptQuery.ftlh", params);
	}

	public static String conceptQueryForDetailStructure(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptQueryForDetailStructure.ftlh", params);
	}


	public static String altLabel(String id, String lang) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG", lang);
		params.put("ID", id);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("altLabel.ftlh", params);
}
	

	public static String conceptNotesQuery(String id, int conceptVersion) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("CONCEPT_VERSION", conceptVersion);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("conceptNotesQuery.ftlh", params);
	}
	
	public static String conceptLinks(String idConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID_CONCEPT", idConcept);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptLinksById.ftlh", params);		
		//TODO Note for later : why "?concept skos:notation '" + id + "' . \n" doesn't work anymore => RDF4J add a type and our triplestore doesn't manage it. 		
	}
	

	public static String getNarrowers(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildConceptRequest("getNarrowers.ftlh", params);
	}
	
	public static String hasBroader(String id) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return buildConceptRequest("hasBroader.ftlh", params);
	}
	

	public static String getOwner(String uri) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		String cleanedUri = uri.replaceAll("[<>]", "");
		params.put("URI", cleanedUri);
		return buildConceptRequest("getOwner.ftlh", params);
	}
	
	public static String getManager(String uri) {
		return "SELECT ?manager { \n"
				+ "?concept dc:contributor ?manager . \n" 
				+ "VALUES ?concept { " + uri + " } \n"
				+ "}";
	}

	/**
	 * @param uriConcept
	 * @return ?idGraph
	 * @throws RmesException
	 */
	public static String getGraphWithConceptQuery(String uriConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getGraphWithConceptQuery.ftlh", params);	
	}

	/**
	 * @param uriConcept
	 * @return ?listConcepts
	 * @throws RmesException
	 */
	public static String getRelatedConceptsQuery(String uriConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getLinkedConceptsQuery.ftlh", params);	
	}
	
	/**
	 * @param idConcept
	 * @return String
	 * @throws RmesException
	 */
	public static String getConceptUriByIDQuery(String idConcept)  throws RmesException {
		Map<String, Object> params = initParams();
		params.put("idConcept", idConcept);
		return buildConceptRequest("getUriFromIdQuery.ftlh", params);	
	}

	/**
	 * @param uriConcept, uriGraph
	 * @return String
	 * @throws RmesException
	 */	
	public static String deleteConcept(String uriConcept, String uriGraph) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		params.put("uriGraph", uriGraph);
		return buildConceptRequest("deleteConceptAndNotesQuery.ftlh", params);	
	}

	public static String isConceptValidated(String conceptId) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		params.put("ID", conceptId);
		return buildConceptRequest("isConceptValidated.ftlh", params);
	}
	
	/**
	 * @param uriConcept
	 * @return String
	 * @throws RmesException
	 */	
	public static String getConceptVersions(String uriConcept) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getConceptVersionsQuery.ftlh", params);
	}
	
	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}
	
	private static String buildConceptRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("concepts/", fileName, params);
	}



	public static String checkIfExists(String id) {
			return "ASK \n"
					+ "WHERE  \n"
					+ "{ ?uri ?b ?c .\n "
					+ "FILTER(STRENDS(STR(?uri),'/concepts/definition/" + id + "')) . }";
			  	
	}
}
