package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class ClassifFamiliesQueries extends GenericQueries{
	
	public static String familiesQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("GRAPH", config.getClassifFamiliesGraph());
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest("classifications/", "getFamilies.ftlh", params);

	}
	
	public static String familyQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("GRAPH", config.getClassifFamiliesGraph());
		params.put("LG1", config.getLg1());
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest("classifications/", "getFamily.ftlh", params);
	}
	
	public static String familyMembersQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest("classifications/", "getFamilyMembers.ftlh", params);
	}
}