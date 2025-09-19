package fr.insee.rmes.persistance.sparql_queries.operations.families;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class OpFamiliesQueries extends GenericQueries{

	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";

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
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", "getFamiliesForAdvancedSearch.ftlh", params);
	}




	
	  private OpFamiliesQueries() {
		    throw new IllegalStateException("Utility class");
	}


}