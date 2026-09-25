package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class OperationDocumentsQueries {

    private final BauhausUriProperties uris;
    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

    public OperationDocumentsQueries(
            BauhausUriProperties uris, BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.uris = uris;
        this.languages = languages;
        this.graphs = graphs;
    }

    public String getDocumentUriQuery(String url) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(Constants.URL, SparqlLiterals.literal(StringUtils.lowerCase(url)));
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

    private String getDocuments(String id, String idSims, String idRubric, Boolean isLink, String uriLang)
            throws RmesException {
        Map<String, Object> params = initParams();
        // une clé absente signifie « pas de filtre » : le template teste sa présence
        putIfNotEmpty(params, Constants.ID, id);
        putIfNotEmpty(params, "idRubric", idRubric);
        putIfNotEmpty(params, "type", getDocType(isLink));
        if (idSims != null && !idSims.isEmpty()) {
            params.put(Constants.ID_SIMS, SparqlLiterals.literal(idSims));
            params.put("DOCUMENTATION_GRAPH_IRI", SparqlLiterals.iri(graphs.documentationsGraph() + "/" + idSims));
        }
        if (uriLang != null && !uriLang.isEmpty()) {
            params.put("LANG", SparqlLiterals.iri(uriLang));
        }
        return buildRequest("getDocumentQuery.ftlh", params);
    }

    private static void putIfNotEmpty(Map<String, Object> params, String key, String value) {
        if (value != null && !value.isEmpty()) {
            params.put(key, SparqlLiterals.literal(value));
        }
    }

    private String getDocType(Boolean isLink) {
        if (isLink == null) {
            return "";
        }
        return (Boolean.TRUE.equals(isLink) ? uris.linksBaseUri() : uris.documentsBaseUri());
    }

    public String lastDocumentID() throws RmesException {
        return buildRequest("lastDocumentIdQuery.ftlh", null);
    }

    public String lastLinkID() throws RmesException {
        return buildRequest("lastLinkIdQuery.ftlh", null);
    }

    private Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("DOCUMENTS_GRAPH", SparqlLiterals.iri(graphs.documentsGraph()));
        return params;
    }

    private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/documentations/documents/", fileName, params);
    }

    public String getDocumentPredicatesAndObjects(Resource documentUri) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(Constants.URI, SparqlLiterals.iri(documentUri.stringValue()));
        return buildRequest("getDocumentPredicatesAndObjects.ftlh", params);
    }

    public String getDocumentsUriAndUrlForSims(String id) throws RmesException {
        Map<String, Object> params = initParams();
        params.put("DOCUMENTATION_GRAPH_IRI", SparqlLiterals.iri(graphs.documentationsGraph() + "/" + id));
        return buildRequest("getDocumentsUriAndUrlForSims.ftlh", params);
    }
}
