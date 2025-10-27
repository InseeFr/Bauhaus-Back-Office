package fr.insee.rmes.persistance.sparql_queries.operations.families;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;

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




	
	  private OpFamiliesQueries() {
		    throw new IllegalStateException("Utility class");
	}


}