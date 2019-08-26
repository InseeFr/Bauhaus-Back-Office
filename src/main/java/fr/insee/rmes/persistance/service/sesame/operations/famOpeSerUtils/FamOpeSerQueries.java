package fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.FreeMarkerUtils;

public class FamOpeSerQueries {

	static Map<String,Object> params ;

	public static String lastId() throws RmesException {
		if (params==null) {initParams();}
		return buildOperationRequest("getLastIdQuery.ftlh", params);	
	}	

	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}
	
	/**
	 * Graph http://rdf.insee.fr/graphes/operations = Family/Series/Operation
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String checkIfOperationExists(String uri) throws RmesException {
		if (params==null) {initParams();}
		params.put("uri", uri);
		return buildOperationRequest("checkIfFamSerOpeExistsQuery.ftlh", params);	
	}
	
	public static String getPublicationState(String id) throws RmesException{
		if (params==null) {initParams();}
		params.put("id", id);
		return buildOperationRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	public static String setPublicationState(URI familyURI, String newState) throws RmesException{
		if (params==null) {initParams();}
		params.put("id", familyURI);
		params.put("newState", newState);
		return buildOperationRequest("changePublicationStatusQuery.ftlh", params);	
	}
	
	
	private static String buildOperationRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}
}
