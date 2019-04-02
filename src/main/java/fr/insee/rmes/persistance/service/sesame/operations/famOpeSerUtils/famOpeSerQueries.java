package fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.FreeMarkerUtils;

public class famOpeSerQueries {

	static Map<String,Object> params ;

	public static String lastIdProv() {
		return "SELECT ?id \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/produits> { \n"
				+ "?uri ?b ?c .\n "
				+ "BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"
				+ "FILTER regex(STR(?uri),'/produits/family/' | STR(?uri),'/produits/series/' | STR(?uri),'/produits/operation/') . \n"
				+ "}} \n"
				+ "ORDER BY DESC(?id) \n"
				+ "LIMIT 1";
	}	


	public static String lastId() throws RmesException {
		if (params==null) {initParams();}
		return buildOperationRequest("getLastIdQuery.ftlh", params);	
	}	

	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}
	
	private static String buildOperationRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
	}
}
