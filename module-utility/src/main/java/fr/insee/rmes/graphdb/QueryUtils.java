package fr.insee.rmes.graphdb;

import fr.insee.rmes.json.JSONUtils;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;

public class QueryUtils {

    private static final String TYPE_OF_OBJECT_PARAM = "typeOfObject";

    /**
     * Hack to fix Sparql groupconcat which returns an empty object in array
     * instead of empty array
     * @param res
     * @return
     */
    public static String correctEmptyGroupConcat(String res) {
        if (res.equals("[{\"altLabel\":\"\"}]")) {
            return "[]";
        }
        return res;
    }

    public static JSONArray transformRdfTypeInString(JSONArray jArray) {
        JSONUtils.stream(jArray)
                .filter(jsonObject -> jsonObject.has(TYPE_OF_OBJECT_PARAM))
                .forEach(jsonObject -> {
                    String typeOfObject = jsonObject.getString(TYPE_OF_OBJECT_PARAM);
                    String type = ObjectType.getLabelType(
                            SimpleValueFactory.getInstance().createIRI(typeOfObject));
                    jsonObject.put("type", type);
                    jsonObject.remove(TYPE_OF_OBJECT_PARAM);
                });
        return jArray;
    }
}
