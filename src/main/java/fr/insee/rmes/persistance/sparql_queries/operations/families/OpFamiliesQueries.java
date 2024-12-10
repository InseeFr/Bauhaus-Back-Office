package fr.insee.rmes.persistance.sparql_queries.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class OpFamiliesQueries extends GenericQueries{

	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}

	public static String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/operations/famille/");
		params.put("TYPE", "insee:StatisticalOperationFamily");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public static String familiesSearchQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return  buildRequest("getFamiliesForAdvancedSearch.ftlh", params);
	}

	public static String familiesQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		return  buildRequest("getFamilies.ftlh", params);
	}


	public static String familyQuery(String id, boolean familiesRichTextNextStructure) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("FAMILIES_RICH_TEXT_NEXT_STRUCTURE", familiesRichTextNextStructure);
		return  buildRequest("getFamily.ftlh", params);
	}

	public static String getSeries(String idFamily) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", idFamily);
		return  buildRequest("getSeries.ftlh", params);
	}

	public static String getSubjects(String idFamily) {
		return "SELECT  ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <"+config.getOperationsGraph()+"> \n"
				+ "WHERE { \n" 

				+ "?family dcterms:subject ?subjectUri . \n"
				+ "?subjectUri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "?subjectUri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') . \n"
				
				+ "?subjectUri skos:notation ?id . \n"

				+ "FILTER(STRENDS(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
				+ "}"
				+ " ORDER BY ?subjectUri"
				;
	}
	
	  private OpFamiliesQueries() {
		    throw new IllegalStateException("Utility class");
	}


}