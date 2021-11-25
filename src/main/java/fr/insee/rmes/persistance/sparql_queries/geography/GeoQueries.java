package fr.insee.rmes.persistance.sparql_queries.geography;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class GeoQueries {
	
	static Map<String,Object> params ;
	
	
	/**
	 * 
	 * @param id
	 * @return uri in COG if exists
	 * @throws RmesException
	 */
	public static String getGeoUriIfExists(String id) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.ID, id);
		return  buildRequest("getGeoUriIfExists.ftlh", params);
	}
	
	
	public static String getFeaturesQuery() throws RmesException {
		if (params==null) {initParams();}
		params.put("uriFeature", "");
		return  buildRequest("getGeoFeatures.ftlh", params);
	}
	
	public static String getUnionsForAFeatureQuery(String uriFeature) throws RmesException {
		return getUnionOrDifferenceForFeature(uriFeature, true);
	}
	
	public static String getDifferenceForAFeatureQuery(String uriFeature) throws RmesException {
		return getUnionOrDifferenceForFeature(uriFeature, false);
	}
	
	/**
	 * 
	 * @param uri = uri of geofeature
	 * @return
	 * @throws RmesException
	 */
	public static String getFeatureQuery(String uri) throws RmesException {
		if (params==null) {initParams();}
		params.put("uriFeature", uri);
		return  buildRequest("getGeoFeatures.ftlh", params);
	}

	private static String getUnionOrDifferenceForFeature(String uriFeature, boolean getUnion) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.URI, uriFeature);
		params.put("union", getUnion);
		return  buildRequest("getUnionOrDifferenceForUri.ftlh", params);
	}
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		params.put("COG_GRAPH", Config.GEOGRAPHY_GRAPH);
		params.put("GEO_SIMS_GRAPH", Config.DOCUMENTATIONS_GEO_GRAPH);
	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("geography/", fileName, params);
	}
	
	
	 private GeoQueries() {
		 throw new IllegalStateException("Utility class");
	 }


    public static String checkUnicityTerritory(String labelLg1) throws RmesException {
		if (params==null) {initParams();}
		params.put("LABEL", labelLg1);

		return  buildRequest("checkUnicityTerritory.ftlh", params);
    }
}
