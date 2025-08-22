package fr.insee.rmes.persistance.sparql_queries.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;

public class DocumentsQueries extends GenericQueries{

	public static String checkLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("OPERATIONS_GRAPH", config.getDocumentsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "");
		params.put("TYPE", "foaf:Document");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	/**
	 * Can delete both document and link because the delete query is based on URI (not only id)
	 * @param uri
	 * @return
	 * @throws RmesException
	 */
	public static String deleteDocumentQuery(IRI uri) throws RmesException {
		Map<String, Object> params = initParams();
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
		Map<String, Object> params = initParams();
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
		Map<String, Object> params = initParams();
		params.put("ID", getDocType(isLink) + "/" + id);
		return buildRequest("getSimsByDocument.ftlh", params);
	}

	public static String getAllDocumentsQuery() throws RmesException {
		return getDocuments("","","", null, "") ;
	}
	
	private static String getDocuments(String id, String idSims, String idRubric, Boolean isLink, String uriLang) throws RmesException {
		Map<String, Object> params = initParams();
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
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		return  buildRequest("getLinksToDocumentQuery.ftlh", params);
	}


	public static String changeDocumentUrlQuery(String iri, String oldUrl, String newUrl) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("iri", iri);
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


	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("DOCUMENTS_GRAPH", config.getDocumentsGraph());
		return params;
	}
	
	
	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/documentations/documents/", fileName, params);
	}
	
	
	 private DocumentsQueries() {
		 throw new IllegalStateException("Utility class");
	 }


    public static String getDocumentsUriAndUrlForSims(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
		return  buildRequest("getDocumentsUriAndUrlForSims.ftlh", params);
    }
}
