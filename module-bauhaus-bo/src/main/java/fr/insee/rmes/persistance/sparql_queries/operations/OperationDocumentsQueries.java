package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationDocumentsQueries {

	private final Config config;

	public OperationDocumentsQueries(Config config) {
		this.config = config;
	}

	public String checkLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("OPERATIONS_GRAPH", config.getDocumentsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "");
		params.put("TYPE", "foaf:Document");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public String deleteDocumentQuery(IRI uri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URI, uri);
		return buildRequest("deleteDocumentQuery.ftlh", params);
	}

	public String getDocumentUriQuery(String url) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.URL, StringUtils.lowerCase(url));
		return buildRequest("getDocumentUriFromUrlQuery.ftlh", params);
	}

	public String getDocumentsForSimsRubricQuery(String idSims, String idRubric, String uriLang) throws RmesException {
		return getDocuments("", idSims, idRubric, null, uriLang);
	}

	public String getDocumentsForSimsQuery(String idSims) throws RmesException {
		return getDocuments("", idSims, "", false, "");
	}

	public String getLinksForSimsQuery(String idSims) throws RmesException {
		return getDocuments("", idSims, "", true, "");
	}

	public String getDocumentQuery(String id, boolean isLink) throws RmesException {
		return getDocuments(id, "", "", isLink, "");
	}

	public String getSimsByDocument(String id, boolean isLink) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", getDocType(isLink) + "/" + id);
		return buildRequest("getSimsByDocument.ftlh", params);
	}

	public String getAllDocumentsQuery() throws RmesException {
		return getDocuments("", "", "", null, "");
	}

	private String getDocuments(String id, String idSims, String idRubric, Boolean isLink, String uriLang) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		params.put(Constants.ID_SIMS, idSims);
		params.put("idRubric", idRubric);
		params.put("type", getDocType(isLink));
		params.put("LANG", uriLang);
		params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
		return buildRequest("getDocumentQuery.ftlh", params);
	}

	private String getDocType(Boolean isLink) {
		if (isLink == null) {
			return "";
		}
		return (Boolean.TRUE.equals(isLink) ? config.getLinksBaseUri() : config.getDocumentsBaseUri());
	}

	public String getLinksToDocumentQuery(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		return buildRequest("getLinksToDocumentQuery.ftlh", params);
	}

	public String changeDocumentUrlQuery(String iri, String oldUrl, String newUrl) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("iri", iri);
		params.put("oldUrl", oldUrl);
		params.put("newUrl", newUrl);
		return buildRequest("changeDocumentUrlQuery.ftlh", params);
	}

	public String lastDocumentID() throws RmesException {
		return buildRequest("lastDocumentIdQuery.ftlh", null);
	}

	public String lastLinkID() throws RmesException {
		return buildRequest("lastLinkIdQuery.ftlh", null);
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("DOCUMENTS_GRAPH", config.getDocumentsGraph());
		return params;
	}

	private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/documentations/documents/", fileName, params);
	}

	public String getDocumentsUriAndUrlForSims(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(Constants.ID, id);
		params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
		return buildRequest("getDocumentsUriAndUrlForSims.ftlh", params);
	}
}