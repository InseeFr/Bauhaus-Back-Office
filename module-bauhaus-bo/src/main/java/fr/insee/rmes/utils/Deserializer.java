package fr.insee.rmes.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.domain.exceptions.RmesException;
import java.io.IOException;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

public class Deserializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T deserializeJsonString(String json, Class<T> target) throws RmesException {
        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            // Le message de Jackson cite les classes Java : il reste dans la cause, pour les logs.
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, "The submitted data is invalid", null, e);
        }
    }

    public static <T> T deserializeJSONArray(JSONArray json, Class<T> target) throws RmesException {
        return deserializeJsonString(json.toString(), target);
    }

    public static <T> T deserializeJSONObject(JSONObject json, Class<T> target) throws RmesException {
        return deserializeJsonString(json.toString(), target);
    }
}
