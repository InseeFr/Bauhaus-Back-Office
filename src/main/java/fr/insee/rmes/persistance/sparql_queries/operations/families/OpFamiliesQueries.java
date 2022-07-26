package fr.insee.rmes.persistance.sparql_queries.operations.families;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.json.JSONObject;

public class OpFamiliesQueries extends GenericQueries{

	static Map<String,Object> params ;
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}

	public static String familiesSearchQuery() throws RmesException {
		HashMap params = new HashMap();
		params.put("OPERATIONS_GRAPH", config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return  buildRequest("getFamiliesForAdvancedSearch.ftlh", params);
	}



	public static String familiesQuery() throws RmesException {
		HashMap params = new HashMap();
		params.put("OPERATIONS_GRAPH", config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		return  buildRequest("getFamilies.ftlh", params);
	}


	public static String familyQuery(String id) throws RmesException {
		HashMap params = new HashMap();
		params.put("OPERATIONS_GRAPH", config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return  buildRequest("getFamily.ftlh", params);
	}

	public static String getSeries(String idFamily) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <"+config.getOperationsGraph()+"> \n"
				+ "WHERE { \n" 

				+ "?family dcterms:hasPart ?uri . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') . \n"
				+ "BIND(STRAFTER(STR(?uri),'/operations/serie/') AS ?id) . \n"


				+ "FILTER(STRENDS(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
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