package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class ParentQueries extends GenericQueries{

	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}
	
	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String checkIfExists(String uri) throws RmesException {
		Map<String, Object> params = initParams();
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
