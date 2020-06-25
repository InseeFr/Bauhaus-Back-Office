package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class DocumentsQueries {
	
	static Map<String,Object> params ;
	

	public static String deleteDocumentQuery(IRI uri, IRI graph) throws RmesException {
		if (params==null) {initParams();}
		params.put("uri", uri);
		params.put("graph", graph);
		return  buildRequest("deleteDocumentQuery.ftlh", params);
	}
	
	public static String getDocumentsQuery(String idSims, String idRubric) throws RmesException {
		if (params==null) {initParams();}
		params.put("idSims", idSims);
		params.put("idRubric", idRubric);
		params.put("DOCUMENTATIONS_GRAPH", Config.DOCUMENTATIONS_GRAPH);
		return  buildRequest("getAllDocumentsByIdSimsIdRubricQuery.ftlh", params);
	}
	
	public static String getDocumentUriQuery(IRI url, Resource graph) throws RmesException {
		Map<String, Object> root = new HashMap<>();
		root.put("url", url);
		root.put("graph", graph);
		return  buildRequest("getDocumentUriFromUrlQuery.ftlh", root);
	}
	
	public static String getDocumentQuery(String id) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.ID, id);
		return  buildRequest("getDocumentQuery.ftlh", params);
	}
	
	public static String getLinksToDocumentQuery(String id) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.ID, id);
		return  buildRequest("getLinksToDocumentQuery.ftlh", params);
	}


	public static String getAllDocumentsQuery() throws RmesException {
		if (params==null) {initParams();}
		return  buildRequest("getAllDocumentsQuery.ftlh", params);
	}

	public static String changeDocumentUrlQuery(String docId, String oldUrl, String newUrl, Resource graph) throws RmesException {
		if (params==null) {initParams();}
		params.put("uriGraph", graph.toString());
		params.put(Constants.ID, docId);
		params.put("oldUrl", oldUrl);
		params.put("newUrl", newUrl);
		return  buildRequest("changeDocumentUrlQuery.ftlh", params);
	}

	public static String lastDocumentID() throws RmesException {
        return buildRequest("lastDocumentIdQuery.ftlh", null);
	}	

	public static String lastLinkID() throws RmesException{
		return buildRequest("lastLinkIdQuery.ftlh", null);
	}


	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		params.put("DOCUMENTS_GRAPH", Config.DOCUMENTS_GRAPH);

	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/documentations/documents/", fileName, params);
	}
	
	
	 private DocumentsQueries() {
		 throw new IllegalStateException("Utility class");
	 }

	
}
