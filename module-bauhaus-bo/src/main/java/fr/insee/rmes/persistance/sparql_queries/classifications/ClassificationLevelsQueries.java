package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClassificationLevelsQueries {

    private final BauhausLanguagesProperties languages;

    public ClassificationLevelsQueries(BauhausLanguagesProperties languages) {
        this.languages = languages;
    }

    public String levelsQuery(String classificationId) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("CLASSIFICATION_URI_PATTERN", SparqlLiterals.literal("/codes/" + classificationId + "/"));
        params.put("CLASSIFICATION_ID_SEGMENT", SparqlLiterals.literal("/" + classificationId + "/"));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        return buildRequest("getClassificationLevels.ftlh", params);
    }

    public String levelQuery(String classificationId, String levelId) throws RmesException {
        return buildRequest("getClassificationLevel.ftlh", levelParams(classificationId, levelId));
    }

    public String levelMembersQuery(String classificationId, String levelId) throws RmesException {
        return buildRequest("getClassificationLevelMembers.ftlh", levelParams(classificationId, levelId));
    }

    private Map<String, Object> levelParams(String classificationId, String levelId) {
        Map<String, Object> params = new HashMap<>();
        params.put("LEVEL_URI_SUFFIX", SparqlLiterals.literal("/codes/" + classificationId + "/" + levelId));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        return params;
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("classifications/", fileName, params);
    }
}
