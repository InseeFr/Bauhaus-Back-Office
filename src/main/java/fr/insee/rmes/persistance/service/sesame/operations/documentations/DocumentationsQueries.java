package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.FreeMarkerUtils;

public class DocumentationsQueries {
	
	static Map<String,Object> params ;
	
	/**
	 * @return ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational 
	 * @throws RmesException
	 */
	public static String msdQuery() throws RmesException{
		if (params==null) {initParams();}
        return buildRequest("msdQuery.ftlh", params);
	}
	
	/**
	 * @param idMas
	 * @return ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational 
	 * @throws RmesException
	 */
	public static String getAttributeSpecificationQuery(String idMas) throws RmesException {
		if (params==null) {initParams();}
		params.put("idMas", idMas);
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
	}
	
	/**
	 * @return ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational
	 * @throws RmesException
	 */
	public static String getAttributesQuery() throws RmesException {
		if (params==null) {initParams();}
        return buildRequest("getAttributesQuery.ftlh", params);
	}
	
	/**
	 * @return ?id ?uri 
	 * @throws RmesException
	 */
	public static String getAttributesUriQuery() throws RmesException {
        return buildRequest("getAttributesUriQuery.ftlh", null);
	}
	
	
	/**
	 * @param idSims
	 * @return ?labelLg1 ?labelLg2 ?idOperation
	 * @throws RmesException
	 */
	public static String getDocumentationTitleQuery(String idSims) throws RmesException {
		if (params==null) {initParams();}
		params.put("idSims", idSims);
        return buildRequest("getDocumentationTitleQuery.ftlh", params);
	}
	
	/**
	 * Get operation by documentation
	 * @return ?idOperation 
	 * @throws RmesException
	 */
	public static String getTargetByIdSims(String idSims) throws RmesException {
		Map<String, Object> root = new HashMap<>();
		root.put("idSims", idSims);
		return buildRequest("getTargetByIdSimsQuery.ftlh", root);	
	}

	/**
	 * Get documentation by operation
	 * @return ?idSims 
	 * @throws RmesException 
	 */
	public static String getSimsByTarget(String idTarget) throws RmesException {
		Map<String, Object> root = new HashMap<>();
		root.put("idTarget", idTarget);
		return buildRequest("getSimsByIdTargetQuery.ftlh", root);
	}
	
	/**
	 * @param idSims
	 * @return ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType 
	 * @throws RmesException
	 */
	public static String getDocumentationRubricsQuery(String idSims) throws RmesException {
		if (params==null) {initParams();}
		params.put("idSims", idSims);
		params.put("DATE", RangeType.DATE);
		params.put("STRING", RangeType.STRING);
		params.put("RICHTEXT", RangeType.RICHTEXT);
		params.put("ATTRIBUTE", RangeType.ATTRIBUTE);
		params.put("CODELIST", RangeType.CODELIST);
		params.put("ORGANIZATION", RangeType.ORGANIZATION);
		return buildRequest("getDocumentationRubricsQuery.ftlh", params);	
	}
	
	/**
	 * @return ?idSims 
	 * @throws RmesException
	 */
	public static String lastID() throws RmesException {
        return buildRequest("lastID.ftlh", null);
	}	
	

	public static String getPublicationState(String id) throws RmesException{
		if (params==null) {initParams();}
		params.put("id", id);
		return buildRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
	}
	
	
	 private DocumentationsQueries() {
		 throw new IllegalStateException("Utility class");
	 }
}
