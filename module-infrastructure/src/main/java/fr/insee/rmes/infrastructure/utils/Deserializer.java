package fr.insee.rmes.infrastructure.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class Deserializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static <T> T deserializeJsonString(String json, Class<T> target) throws RmesException {
        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, "while " , e.getMessage());
        }
    }

    public static <T> T deserializeJSONArray(JSONArray json, Class<T> target) throws RmesException {
        return deserializeJsonString(json.toString(), target);
    }

    public static <T> T deserializeJSONObject(JSONObject json, Class<T> target) throws RmesException {
        return deserializeJsonString(json.toString(), target);
    }
}