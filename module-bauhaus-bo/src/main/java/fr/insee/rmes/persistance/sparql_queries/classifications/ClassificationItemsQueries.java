package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClassificationItemsQueries {

    public static final String CLASSIFICATION_ID = "CLASSIFICATION_ID";
    public static final String ITEM_ID = "ITEM_ID";
    private static final String CLASSIFICATION_URI_PATTERN = "CLASSIFICATION_URI_PATTERN";
    private static final String ITEM_URI_SUFFIX = "ITEM_URI_SUFFIX";
    private static final String CODES_PATH = "/codes/";

    private final BauhausLanguagesProperties languages;

    public ClassificationItemsQueries(BauhausLanguagesProperties languages) {
        this.languages = languages;
    }

    private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("classifications/", fileName, params);
    }

    public String itemQuery(String classificationId, String itemId) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put(CLASSIFICATION_ID, SparqlLiterals.literal(classificationId));
        params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classificationId + "/"));
        params.put(ITEM_URI_SUFFIX, SparqlLiterals.literal("/" + itemId));

        return buildRequest("getClassificationItem.ftlh", params);
    }

    public String itemAltQuery(String classificationId, String itemId) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put(CLASSIFICATION_ID, SparqlLiterals.literal(classificationId));
        params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classificationId + "/"));
        params.put(ITEM_URI_SUFFIX, SparqlLiterals.literal("/" + itemId));

        return buildRequest("getClassificationItemAltLabels.ftlh", params);
    }

    public String itemNotesQuery(String classificationId, String itemId, int conceptVersion) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put(CLASSIFICATION_ID, SparqlLiterals.literal(classificationId));
        params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classificationId + "/"));
        params.put(ITEM_URI_SUFFIX, SparqlLiterals.literal("/" + itemId));
        params.put("CONCEPT_VERSION", SparqlLiterals.literal(String.valueOf(conceptVersion)));

        return buildRequest("getClassificationItemNotes.ftlh", params);
    }

    public String itemNarrowersQuery(String classificationId, String itemId) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put(CLASSIFICATION_ID, SparqlLiterals.literal(classificationId));
        params.put(CLASSIFICATION_URI_PATTERN, SparqlLiterals.literal(CODES_PATH + classificationId + "/"));
        params.put(ITEM_URI_SUFFIX, SparqlLiterals.literal("/" + itemId));

        return buildRequest("getClassificationItemNarrowers.ftlh", params);
    }
}
