package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

public class DocumentsQueries extends GenericQueries{
	
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

	/**
	 * 
	 * @param url = link or filename
	 * @return
	 * @throws RmesException
	 */
	public static String getDocumentUriQuery(String url) throws RmesException {
		if (params==null) {initParams();}
		params.put(Constants.URL, StringUtils.lowerCase(url));
		return  buildRequest("getDocumentUriFromUrlQuery.ftlh", params);
	}
	
	public static String getDocumentsForSimsRubricQuery(String idSims, String idRubric, String uriLang) throws RmesException {
		return getDocuments("",idSims,idRubric,null, uriLang) ;
	}
	
	public static String getDocumentsForSimsQuery(String idSims) throws RmesException {
		return getDocuments("",idSims,"",false, "") ;
	}
	
	public static String getLinksForSimsQuery(String idSims) throws RmesException {
		return getDocuments("",idSims,"",true, "") ;
	}
	
	public static String getDocumentQuery(String id, boolean isLink) throws RmesException {
		return getDocuments(id,"","", isLink, "") ;
	}

	public static String getSimsByDocument(String id, boolean isLink) throws RmesException {
		if (params==null) {initParams();}
		params.put("ID", getDocType(isLink) + "/" + id);
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
		params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
		return  buildRequest("getDocumentQuery.ftlh", params);
	}

	private static String getDocType(Boolean isLink) {
		if (isLink == null) {
			return "";
		}
		return (Boolean.TRUE.equals(isLink) ? config.getLinksBaseUri() :config.getDocumentsBaseUri());
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
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("DOCUMENTS_GRAPH", config.getDocumentsGraph());

	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/documentations/documents/", fileName, params);
	}
	
	
	 private DocumentsQueries() {
		 throw new IllegalStateException("Utility class");
	 }



}
