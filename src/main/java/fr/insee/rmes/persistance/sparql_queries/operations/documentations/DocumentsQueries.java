package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class DocumentsQueries {
	
	static Map<String,Object> params ;
	

	/**
	 * Can delete both document and link because the delete query is based on URI (not only id)
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String deleteDocumentQuery(IRI uri) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.URI, uri);
		return  buildRequest("deleteDocumentQuery.ftlh", params);
	}

	
	public static String getDocumentUriQuery(IRI url) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.URL, url);
		return  buildRequest("getDocumentUriFromUrlQuery.ftlh", params);
	}
	
	public static String getDocumentsForSimsQuery(String idSims, String idRubric, String uriLang) throws RmesException {
		return getDocuments("",idSims,idRubric,null, uriLang) ;
	}
	
	public static String getDocumentQuery(String id, boolean isLink) throws RmesException {
		return getDocuments(id,"","", isLink, "") ;
	}

	public static String getSimsByDocument(String id) throws RmesException {
		if (params==null) {initParams();}
		params.put("ID", Config.DOCUMENTS_BASE_URI + "/" + id);
		return buildRequest("getSimsByDocument.ftlh", params);
	}

	public static String getAllDocumentsQuery() throws RmesException {
		return getDocuments("","","", null, "") ;
	}
	
	private static String getDocuments(String id, String idSims, String idRubric, Boolean isLink, String uriLang) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.ID, id);
		params.put(Constants.ID_SIMS, idSims);
		params.put("idRubric", idRubric);
		params.put("type", getDocType(isLink) );
		params.put("LANG", uriLang);
		params.put("DOCUMENTATIONS_GRAPH", Config.DOCUMENTATIONS_GRAPH);
		return  buildRequest("getDocumentQuery.ftlh", params);
	}

	private static String getDocType(Boolean isLink) {
		if (isLink == null) {
			return "";
		}
		return (Boolean.TRUE.equals(isLink) ? "/page/" :"/document/");
	}
	
	
	public static String getLinksToDocumentQuery(String id) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.ID, id);
		return  buildRequest("getLinksToDocumentQuery.ftlh", params);
	}


	public static String changeDocumentUrlQuery(String docId, String oldUrl, String newUrl) throws RmesException {
		if (params==null) {initParams();}
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
