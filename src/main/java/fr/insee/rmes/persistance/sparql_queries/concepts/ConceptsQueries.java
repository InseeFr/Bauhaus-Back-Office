package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class ConceptsQueries extends GenericQueries{

	private static final String URI_CONCEPT = "uriConcept";
	public static final String CONCEPTS_GRAPH = "CONCEPTS_GRAPH";
	static Map<String,Object> params ;
	
	private ConceptsQueries() {
		throw new IllegalStateException("Utility class");
	}

	
	public static String lastConceptID() throws RmesException {
		params = new HashMap<>();
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getLastConceptId.ftlh", params);
	}
	
	
	public static String conceptsQuery() throws RmesException {
		if (params==null) {initParams();}
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConcepts.ftlh", params);
	}
	
	public static String conceptsSearchQuery() throws RmesException {
		if (params==null) {initParams();}
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptsForAdvancedSearch.ftlh", params);
	}
		

	public static String conceptsToValidateQuery() throws RmesException {
		if (params==null) {initParams();}
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
	
	public static String altLabel(String id, String lang) {
		return "SELECT ?altLabel \n"
				+ "WHERE { \n"
				+ "?concept skos:altLabel ?altLabel \n"
				+ "FILTER (lang(?altLabel) = '" + lang + "') . \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + id + "')) . \n"
				+ "}";
		
	}
	
	public static String conceptNotesQuery(String id, int conceptVersion) { 
		return "SELECT ?definitionLg1 ?definitionLg2 ?scopeNoteLg1 ?scopeNoteLg2 "
				+ "?editorialNoteLg1 ?editorialNoteLg2 ?changeNoteLg1 ?changeNoteLg2 \n"
				+ "WHERE { GRAPH <"+config.getConceptsGraph()+"> { \n"
				+ "?concept skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) . \n" 
				// Def Lg1
				+ "OPTIONAL {?concept skos:definition ?defLg1 . \n"
				+ "?defLg1 dcterms:language '" + config.getLg1() + "'^^xsd:language . \n"
				+ "?defLg1 evoc:noteLiteral ?definitionLg1 . \n"
				+ "?defLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				// Def Lg2
				+ "OPTIONAL {?concept skos:definition ?defLg2 . \n"
				+ "?defLg2 dcterms:language '" + config.getLg2() + "'^^xsd:language . \n"
				+ "?defLg2 evoc:noteLiteral ?definitionLg2 . \n"
				+ "?defLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				// Def courte Lg1
				+ "OPTIONAL {?concept skos:scopeNote ?scopeLg1 . \n"
				+ "?scopeLg1 dcterms:language '" + config.getLg1() + "'^^xsd:language . \n"
				+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
				+ "?scopeLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				// Def courte Lg2
				+ "OPTIONAL {?concept skos:scopeNote ?scopeLg2 . \n"
				+ "?scopeLg2 dcterms:language '" + config.getLg2() + "'^^xsd:language . \n"
				+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
				+ "?scopeLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// Note edit Lg1
				+ "OPTIONAL {?concept skos:editorialNote ?editorialLg1 . \n"
				+ "?editorialLg1 dcterms:language '" + config.getLg1() + "'^^xsd:language . \n"
				+ "?editorialLg1 evoc:noteLiteral ?editorialNoteLg1 . \n"
				+ "?editorialLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// Note edit Lg2
				+ "OPTIONAL {?concept skos:editorialNote ?editorialLg2 . \n"
				+ "?editorialLg2 dcterms:language '" + config.getLg2() + "'^^xsd:language . \n"
				+ "?editorialLg2 evoc:noteLiteral ?editorialNoteLg2 . \n"
				+ "?editorialLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// Note changement Lg1
				+ "OPTIONAL {?concept skos:changeNote ?noteChangeLg1 . \n"
				+ "?noteChangeLg1 dcterms:language '" + config.getLg1() + "'^^xsd:language . \n"
				+ "?noteChangeLg1 evoc:noteLiteral ?changeNoteLg1 . \n"
				+ "?noteChangeLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int} . \n"
				// Note changement Lg2
				+ "OPTIONAL {?concept skos:changeNote ?noteChangeLg2 . \n"
				+ "?noteChangeLg2 dcterms:language '" + config.getLg2() + "'^^xsd:language . \n"
				+ "?noteChangeLg2 evoc:noteLiteral ?changeNoteLg2 . \n"
				+ "?noteChangeLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int} . \n"
				+ "}} \n";
	}
	
	public static String conceptLinks(String idConcept) throws RmesException {
		if (params==null) {initParams();}
		params.put("ID_CONCEPT", idConcept);
		params.put(CONCEPTS_GRAPH, config.getConceptsGraph());
		return buildConceptRequest("getConceptLinksById.ftlh", params);		
		//TODO Note for later : why "?concept skos:notation '" + id + "' . \n" doesn't work anymore => RDF4J add a type and our triplestore doesn't manage it. 		
	}
	
	public static String getNarrowers(String id) {
		return "SELECT ?narrowerId { \n"
				//+ "?concept skos:notation '" + id + "' . \n" 
				+ "?concept skos:narrower ?narrower . \n"
				+ "?narrower skos:notation ?narrowerIdStr \n"
				+ "BIND (STR(?narrowerIdStr) AS ?narrowerId) \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + id + "')) . \n"

				+ "}";
	}
	
	public static String hasBroader(String id) {
		return "ASK { \n"
				+ "?concept skos:broader ?broader \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + id + "')) . \n"
				+ "}";			
	}
	
	public static String getOwner(String uri) {
		return "SELECT ?owner { \n"
				+ "?concept dc:creator ?owner . \n" 
				+ "VALUES ?concept { " + uri + " } \n"
				+ "}";
	}
	
	public static String getManager(String uri) {
		return "SELECT ?manager { \n"
				+ "?concept dc:contributor ?manager . \n" 
				+ "VALUES ?concept { " + uri + " } \n"
				+ "}";
	}

	/**
	 * @param idConcept
	 * @return ?idGraph
	 * @throws RmesException
	 */
	public static String getGraphWithConceptQuery(String uriConcept) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getGraphWithConceptQuery.ftlh", params);	
	}

	/**
	 * @param uriConcept
	 * @return ?listConcepts
	 * @throws RmesException
	 */
	public static String getRelatedConceptsQuery(String uriConcept) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getLinkedConceptsQuery.ftlh", params);	
	}
	
	/**
	 * @param idConcept
	 * @return String
	 * @throws RmesException
	 */
	public static String getConceptUriByIDQuery(String idConcept)  throws RmesException {
		if (params==null) {initParams();}
		params.put("idConcept", idConcept);
		return buildConceptRequest("getUriFromIdQuery.ftlh", params);	
	}

	/**
	 * @param uriConcept, uriGraph
	 * @return String
	 * @throws RmesException
	 */	
	public static String deleteConcept(String uriConcept, String uriGraph) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_CONCEPT, uriConcept);
		params.put("uriGraph", uriGraph);
		return buildConceptRequest("deleteConceptAndNotesQuery.ftlh", params);	
	}

	public static String isConceptValidated(String conceptId) throws RmesException {
		if (params==null) {initParams();}
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
		if (params==null) {initParams();}
		params.put(URI_CONCEPT, uriConcept);
		return buildConceptRequest("getConceptVersionsQuery.ftlh", params);	
	}
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
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
