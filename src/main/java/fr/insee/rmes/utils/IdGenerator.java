package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import org.json.JSONObject;

public class IdGenerator {
    public static String generateNextId(JSONObject json, String prefix) {
        if (json.isEmpty()) {
            return prefix + "1000";
        }
        String id = json.getString(Constants.ID);
        if (Constants.UNDEFINED.equals(id)) {
            return prefix + "1000";
        }
        return prefix + (Integer.parseInt(id) + 1);
    }
}
