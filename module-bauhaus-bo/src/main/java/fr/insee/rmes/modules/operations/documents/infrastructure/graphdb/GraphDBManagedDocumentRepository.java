package fr.insee.rmes.modules.operations.documents.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentLanguage;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.ManagedDocumentRepository;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Documents et liens du graphe {@code qualite/documents}. Toute ressource y est désignée par son
 * IRI exacte, jamais par un motif sur l'IRI ; les écritures restent dans ce graphe et passent par
 * SPARQL (cf. règles de portabilité du CLAUDE.md).
 */
@ServerSideAdaptor
@Repository
public class GraphDBManagedDocumentRepository implements ManagedDocumentRepository {

    private static final Logger logger = LoggerFactory.getLogger(GraphDBManagedDocumentRepository.class);

    private static final String QUERIES_PATH = "operations/documentations/documents/";
    private static final String DOCUMENTS_GRAPH = "DOCUMENTS_GRAPH";
    private static final String URI = "URI";
    private static final int FIRST_ID = 1000;

    private final RepositoryGestion repositoryGestion;
    private final GraphsProperties graphs;
    private final BauhausUriProperties uris;
    private final BauhausLanguagesProperties languages;

    public GraphDBManagedDocumentRepository(
            RepositoryGestion repositoryGestion,
            GraphsProperties graphs,
            BauhausUriProperties uris,
            BauhausLanguagesProperties languages) {
        this.repositoryGestion = repositoryGestion;
        this.graphs = graphs;
        this.uris = uris;
        this.languages = languages;
    }

    /**
     * Documents et liens partagent la suite : le plus grand identifiant des deux, plus un ; 1000 pour
     * le tout premier.
     */
    @Override
    public String nextId() throws RmesException {
        int last = Math.max(lastId("lastDocumentIdQuery.ftlh"), lastId("lastLinkIdQuery.ftlh"));
        return Integer.toString(last + 1);
    }

    @Override
    public String uriOf(DocumentKind kind, String id) {
        return baseOf(kind) + id;
    }

    @Override
    public List<ManagedDocument> findAll() throws RmesException {
        List<ManagedDocument> documents = new ArrayList<>();
        for (Object row : select("getManagedDocumentsQuery.ftlh", labelParams())) {
            toDocument((JSONObject) row).ifPresent(documents::add);
        }
        return documents;
    }

    @Override
    public Optional<ManagedDocument> find(DocumentKind kind, String id) throws RmesException {
        Map<String, Object> params = labelParams();
        params.put(URI, SparqlLiterals.iri(uriOf(kind, id)));
        JSONArray rows = select("getManagedDocumentsQuery.ftlh", params);
        return rows.isEmpty() ? Optional.empty() : toDocument(rows.getJSONObject(0));
    }

    @Override
    public List<SimsReference> findSimsReferences(DocumentKind kind, String id) throws RmesException {
        Map<String, Object> params = labelParams();
        params.put(URI, SparqlLiterals.iri(uriOf(kind, id)));
        return JSONUtils.stream(select("getManagedDocumentSimsQuery.ftlh", params))
                .map(row -> new SimsReference(
                        row.getString("simsId"),
                        optional(row, "labelLg1"),
                        optional(row, "labelLg2"),
                        row.getString("rubricId"),
                        List.of()))
                .toList();
    }

    @Override
    public boolean isLabelUsedByAnother(String label, DocumentLanguage language, String excludedUri)
            throws RmesException {
        String lang = language == DocumentLanguage.FIRST ? languages.lg1() : languages.lg2();
        Map<String, Object> params = graphParam();
        params.put("LABEL", SparqlLiterals.literal(label, lang));
        params.put(URI, SparqlLiterals.iri(excludedUri));
        return repositoryGestion.getResponseAsBoolean(
                FreeMarkerUtils.buildRequest(QUERIES_PATH, "isManagedDocumentLabelUsedQuery.ftlh", params));
    }

    @Override
    public Optional<String> findUriByUrl(String url) throws RmesException {
        Map<String, Object> params = graphParam();
        params.put("url", SparqlLiterals.literal(url.toLowerCase(Locale.ROOT)));
        JSONArray rows = select("getDocumentUriFromUrlQuery.ftlh", params);
        return rows.isEmpty()
                ? Optional.empty()
                : Optional.of(rows.getJSONObject(0).getString("document"));
    }

    @Override
    public List<String> findSimsTextsCiting(String uri) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put(URI, SparqlLiterals.iri(uri));
        return JSONUtils.stream(select("getManagedDocumentCitingTextsQuery.ftlh", params))
                .map(row -> row.getString("text"))
                .toList();
    }

    @Override
    public void save(ManagedDocument document) throws RmesException {
        DocumentForm form = document.form();
        Map<String, Object> params = graphParam();
        params.put(URI, SparqlLiterals.iri(document.uri()));
        putIfPresent(params, "URL", form.url(), SparqlLiterals::iri);
        putIfPresent(params, "LABEL_LG1", form.labelLg1(), value -> SparqlLiterals.literal(value, languages.lg1()));
        putIfPresent(params, "LABEL_LG2", form.labelLg2(), value -> SparqlLiterals.literal(value, languages.lg2()));
        putIfPresent(
                params,
                "DESCRIPTION_LG1",
                form.descriptionLg1(),
                value -> SparqlLiterals.literal(value, languages.lg1()));
        putIfPresent(
                params,
                "DESCRIPTION_LG2",
                form.descriptionLg2(),
                value -> SparqlLiterals.literal(value, languages.lg2()));
        putIfPresent(params, "LANG", form.lang(), SparqlLiterals::literal);
        putIfPresent(
                params,
                "UPDATED_DATE",
                form.updatedDate(),
                value -> SparqlLiterals.literal(RdfUtils.setLiteralDate(value).getLabel()));
        FileSize size = document.size();
        if (size != null) {
            params.put("SIZE", SparqlLiterals.literal(Long.toString(size.bytes())));
        }
        repositoryGestion.executeUpdate(
                FreeMarkerUtils.buildRequest(QUERIES_PATH, "saveManagedDocumentQuery.ftlh", params));
    }

    @Override
    public void delete(String uri) throws RmesException {
        Map<String, Object> params = graphParam();
        params.put("uri", SparqlLiterals.iri(uri));
        repositoryGestion.executeUpdate(FreeMarkerUtils.buildRequest(QUERIES_PATH, "deleteDocumentQuery.ftlh", params));
    }

    /** Une valeur vide n'est pas écrite : le template omet alors son triplet. */
    private static void putIfPresent(
            Map<String, Object> params, String name, @Nullable String value, UnaryOperator<String> token) {
        if (isPresent(value)) {
            params.put(name, token.apply(value));
        }
    }

    private Optional<ManagedDocument> toDocument(JSONObject row) {
        String uri = row.getString("uri");
        DocumentKind kind = uri.startsWith(baseOf(DocumentKind.LINK)) ? DocumentKind.LINK : DocumentKind.DOCUMENT;
        if (!uri.startsWith(baseOf(kind))) {
            return Optional.empty();
        }
        DocumentForm form = new DocumentForm(
                optional(row, "labelLg1"),
                optional(row, "labelLg2"),
                optional(row, "descriptionLg1"),
                optional(row, "descriptionLg2"),
                optional(row, "updatedDate"),
                optional(row, "lang"),
                optional(row, "url"));
        return Optional.of(new ManagedDocument(
                uri.substring(baseOf(kind).length()), kind, uri, form, sizeOf(uri, optional(row, "size"))));
    }

    /** {@code dct:extent} porte un nombre d'octets ; toute autre valeur est ignorée. */
    private static @Nullable FileSize sizeOf(String uri, @Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return new FileSize(Long.parseLong(value.trim()));
        } catch (IllegalArgumentException _) {
            logger.warn("Ignoring the size of {}: dcterms:extent is not a number of bytes", uri);
            return null;
        }
    }

    private String baseOf(DocumentKind kind) {
        String path = kind == DocumentKind.LINK ? uris.linksBaseUri() : uris.documentsBaseUri();
        return uris.baseUriGestion() + path + "/";
    }

    private int lastId(String query) throws RmesException {
        JSONObject row = repositoryGestion.getResponseAsObject(
                FreeMarkerUtils.buildRequest(QUERIES_PATH, query, new HashMap<>()));
        String id = row.optString("id", "");
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException _) {
            return FIRST_ID - 1;
        }
    }

    private JSONArray select(String query, Map<String, Object> params) throws RmesException {
        return repositoryGestion.getResponseAsArray(FreeMarkerUtils.buildRequest(QUERIES_PATH, query, params));
    }

    private Map<String, Object> graphParam() {
        Map<String, Object> params = new HashMap<>();
        params.put(DOCUMENTS_GRAPH, SparqlLiterals.iri(graphs.documentsGraph()));
        return params;
    }

    private Map<String, Object> labelParams() {
        Map<String, Object> params = graphParam();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        return params;
    }

    private static @Nullable String optional(JSONObject row, String field) {
        return row.has(field) ? row.getString(field) : null;
    }

    private static boolean isPresent(@Nullable String value) {
        return value != null && !value.isEmpty();
    }
}
