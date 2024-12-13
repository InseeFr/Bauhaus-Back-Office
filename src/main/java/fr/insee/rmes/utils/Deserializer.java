package fr.insee.rmes.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.IOException;

public class Deserializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Deserializer() {
    }

    public static <T> T deserializeJsonString(String json, Class<T> target) throws RmesException {
        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, "while " , e.getMessage());
        }
    }

    //TODO Use a mapper directly from JSONObject to java class
    public static Dataset deserializeJSONObject(JSONObject json, Class<Dataset> target) throws RmesException {
        return deserializeJsonString(json.toString(), target);
    }
}