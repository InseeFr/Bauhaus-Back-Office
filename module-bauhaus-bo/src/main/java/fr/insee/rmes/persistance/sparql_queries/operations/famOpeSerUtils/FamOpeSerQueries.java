package fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;

public class FamOpeSerQueries extends GenericQueries{


	public static String lastId() throws RmesException {
		Map<String, Object> params = initParams();
		return buildOperationRequest("getLastIdQuery.ftlh", params);	
	}	

	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("OPERATIONS_GRAPH",config.getOperationsGraph());
		return params;
	}
	
	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String checkIfFamOpeSerExists(String uri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URI, uri);
		return buildOperationRequest("checkIfFamSerOpeExistsQuery.ftlh", params);	
	}
	
	public static String getPublicationState(String id) throws RmesException{
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		return buildOperationRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	public static String setPublicationState(IRI familyURI, String newState) throws RmesException{
		Map<String, Object> params = initParams();
		params.put(Constants.ID, familyURI);
		params.put("newState", newState);
		return buildOperationRequest("changePublicationStatusQuery.ftlh", params);	
	}
	
	
	private static String buildOperationRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}
	
	  private FamOpeSerQueries() {
		    throw new IllegalStateException("Utility class");
	}

}
