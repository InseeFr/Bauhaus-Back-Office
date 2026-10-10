package fr.insee.rmes.modules.operations.documents.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@ServerSideAdaptor
@Repository
public class GraphDBDocumentDescriptionRepository implements DocumentDescriptionRepository {

    private static final Logger logger = LoggerFactory.getLogger(GraphDBDocumentDescriptionRepository.class);

    private static final String QUERIES_PATH = "operations/documentations/documents/";

    /** Un identifiant qui n'est pas un segment d'IRI simple ne désigne aucun document. */
    private static final Pattern DOCUMENT_ID = Pattern.compile("[\\w-]+");

    private final RepositoryGestion repositoryGestion;
    private final GraphsProperties graphs;
    private final BauhausUriProperties uris;
    private final BauhausLanguagesProperties languages;

    public GraphDBDocumentDescriptionRepository(
            RepositoryGestion repositoryGestion,
            GraphsProperties graphs,
            BauhausUriProperties uris,
            BauhausLanguagesProperties languages) {
        this.repositoryGestion = repositoryGestion;
        this.graphs = graphs;
        this.uris = uris;
        this.languages = languages;
    }

    @Override
    public Optional<DocumentMetadata> findMetadata(DocumentKind kind, String id) throws RmesException {
        if (!DOCUMENT_ID.matcher(id).matches()) {
            return Optional.empty();
        }
        String uri = documentUri(kind, id);

        Map<String, Object> params = new HashMap<>();
        params.put("DOCUMENTS_GRAPH", SparqlLiterals.iri(graphs.documentsGraph()));
        params.put("URI", SparqlLiterals.iri(uri));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        JSONArray results = repositoryGestion.getResponseAsArray(
                FreeMarkerUtils.buildRequest(QUERIES_PATH, "getDocumentDescriptionQuery.ftlh", params));

        if (results.isEmpty()) {
            return Optional.empty();
        }
        JSONObject document = results.getJSONObject(0);
        return Optional.of(new DocumentMetadata(
                uri,
                localisedLabels(document, "labelLg1", "labelLg2"),
                localisedLabels(document, "commentLg1", "commentLg2"),
                optional(document, "updatedDate", value -> LocalDate.parse(value.substring(0, 10))),
                optional(document, "lang", Function.identity()),
                optional(document, "size", value -> fileSize(uri, value)),
                document.getString("url")));
    }

    @Override
    public Set<String> findRubricConcepts(String documentUri) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("MSD_GRAPH", SparqlLiterals.iri(graphs.msdGraph()));
        params.put("URI", SparqlLiterals.iri(documentUri));
        JSONArray results = repositoryGestion.getResponseAsArray(
                FreeMarkerUtils.buildRequest(QUERIES_PATH, "getDocumentRubricConceptsQuery.ftlh", params));

        return JSONUtils.stream(results)
                .map(concept -> concept.getString("concept"))
                .collect(Collectors.toSet());
    }

    private String documentUri(DocumentKind kind, String id) {
        String path = kind == DocumentKind.LINK ? uris.linksBaseUri() : uris.documentsBaseUri();
        return uris.baseUriGestion() + path + "/" + id;
    }

    private static List<LocalisedLabel> localisedLabels(JSONObject document, String lg1Field, String lg2Field) {
        List<LocalisedLabel> labels = new ArrayList<>();
        if (document.has(lg1Field)) {
            labels.add(LocalisedLabel.ofDefaultLanguage(document.getString(lg1Field)));
        }
        if (document.has(lg2Field)) {
            labels.add(LocalisedLabel.ofAlternativeLanguage(document.getString(lg2Field)));
        }
        return labels;
    }

    /**
     * {@code dct:extent} porte un nombre d'octets. Une valeur d'un autre format ne fait pas échouer la
     * description : la taille est seulement omise.
     */
    private static @Nullable FileSize fileSize(String uri, String value) {
        try {
            return new FileSize(Long.parseLong(value.trim()));
        } catch (IllegalArgumentException _) {
            logger.warn("Ignoring the size of {}: dct:extent is not a number of bytes ({})", uri, value);
            return null;
        }
    }

    private static <T> T optional(JSONObject document, String field, Function<String, T> mapper) {
        return document.has(field) ? mapper.apply(document.getString(field)) : null;
    }
}
