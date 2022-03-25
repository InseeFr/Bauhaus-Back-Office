package fr.insee.rmes.persistance.sparql_queries.operations;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class ParentQueries {

	static Map<String,Object> params ;

	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.getLg1());
		params.put("LG2", Config.getLg2());
	}
	
	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String checkIfExists(String uri) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.URI, uri);
		return buildRequest("checkIfExistsQuery.ftlh", params);	
	}
		
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("", fileName, params);
	}
	
	  private ParentQueries() {
		    throw new IllegalStateException("Utility class");
	}

}
