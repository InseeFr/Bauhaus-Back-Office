package fr.insee.rmes.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpStatus;

import java.io.IOException;

public class Deserializer {
    private static final String IO_EXCEPTION = "IOException";
    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T deserializeBody(String body, Class target) throws RmesException {
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return (T) mapper.readValue(body, target);
        } catch (IOException e) {
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, e.getMessage(), IO_EXCEPTION);
        }
    }
}