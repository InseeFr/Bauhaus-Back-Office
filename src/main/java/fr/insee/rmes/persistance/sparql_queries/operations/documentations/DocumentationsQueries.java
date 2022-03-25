package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Resource;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.RangeType;

public class DocumentationsQueries {
	
	private static final String ID_SIMS = Constants.ID_SIMS;

	
	/**
	 * @return ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational 
	 * @throws RmesException
	 */
	public static String msdQuery() throws RmesException{
		Map<String,Object> params = initParams();
        return buildRequest("msdQuery.ftlh", params);
	}
	
	/**
	 * @param idMas
	 * @return ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational 
	 * @throws RmesException
	 */
	public static String getAttributeSpecificationQuery(String idMas) throws RmesException {
		Map<String,Object> params = initParams();
		params.put("idMas", idMas);
		params.put("uniqueAttr","true");
		params.put("MSD_GRAPH",Config.getMsdGraph());
		params.put("CODELIST_GRAPH",Config.getCodeListGraph());
		params.put("MSD_CONCEPTS_GRAPH", Config.getMsdConceptsGraph());
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
	}
	
	/**
	 * @return ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational
	 * @throws RmesException
	 */
	public static String getAttributesQuery() throws RmesException {
		Map<String,Object> params = initParams();
		params.put("uniqueAttr","false");
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
	}
	
	/**
	 * @return ?id ?uri 
	 * @throws RmesException
	 */
	public static String getAttributesUriQuery() throws RmesException {
		Map<String,Object> params = initParams();
        return buildRequest("getAttributesUriQuery.ftlh", params);
	}
	
	
	/**
	 * @param idSims
	 * @return ?labelLg1 ?labelLg2 ?idOperation
	 * @throws RmesException
	 */
	public static String getDocumentationTitleQuery(String idSims) throws RmesException {
		Map<String,Object> params = initParams();
		params.put(ID_SIMS, idSims);
        return buildRequest("getDocumentationTitleQuery.ftlh", params);
	}
	
	/**
	 * Get operation by documentation
	 * @return ?idOperation 
	 * @throws RmesException
	 */
	public static String getTargetByIdSims(String idSims) throws RmesException {
		Map<String,Object> params = initParams();
		params.put(ID_SIMS, idSims);
		return buildRequest("getTargetByIdSimsQuery.ftlh", params);	
	}

	/**
	 * Get documentation by operation
	 * @return ?idSims 
	 * @throws RmesException 
	 */
	public static String getSimsByTarget(String idTarget) throws RmesException {
		Map<String,Object> params = initParams();
		params.put("idTarget", idTarget);
		return buildRequest("getSimsByIdTargetQuery.ftlh", params);
	}
	
	/**
	 * @param idSims
	 * @return ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType 
	 * @throws RmesException
	 */
	public static String getDocumentationRubricsQuery(String idSims, String clLg1, String clLg2) throws RmesException {
		Map<String,Object> params = initParams();
		params.put(ID_SIMS, idSims);
		params.put("DATE", RangeType.DATE);
		params.put("STRING", RangeType.STRING);
		params.put("RICHTEXT", RangeType.RICHTEXT);
		params.put("ATTRIBUTE", RangeType.ATTRIBUTE);
		params.put("CODELIST", RangeType.CODELIST);
		params.put("ORGANIZATION", RangeType.ORGANIZATION);
		params.put("GEOGRAPHY", RangeType.GEOGRAPHY);
		
		params.put("ORGANIZATIONS_GRAPH", Config.getOrganizationsGraph());
		params.put("ORG_INSEE_GRAPH", Config.getOrgInseeGraph());
		
		params.put("LG1_CL",clLg1);
		params.put("LG2_CL",clLg2);
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
		Map<String,Object> params = initParams();
		params.put(Constants.ID_SIMS, id);
		params.put("DOCUMENTATIONS_GRAPH", Config.getDocumentationsGraph());
		return buildRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	
	private static Map<String,Object> initParams() {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", Config.getLg1());
		params.put("LG2", Config.getLg2());
		params.put("DOCUMENTATIONS_GRAPH", Config.getDocumentationsGraph());
		params.put("MSD_GRAPH",Config.getMsdGraph());
		params.put("CODELIST_GRAPH",Config.getCodeListGraph());
		params.put("MSD_CONCEPTS_GRAPH", Config.getMsdConceptsGraph());
		return params;
	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
	}
	
	
	 private DocumentationsQueries() {
		 throw new IllegalStateException("Utility class");
	 }

	public static String deleteGraph(Resource graph) throws RmesException {
		Map<String,Object> params = initParams();
		params.put("DOCUMENTATION_GRAPH", graph);
		return buildRequest("deleteGraph.ftlh", params);	
	}
}
