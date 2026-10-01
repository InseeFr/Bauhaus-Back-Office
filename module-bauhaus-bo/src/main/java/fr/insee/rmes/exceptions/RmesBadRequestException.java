package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

public class RmesBadRequestException extends RmesException {

    private static final long serialVersionUID = 400L;

    private RmesBadRequestException(JSONObject body) {
        super(HttpStatus.SC_BAD_REQUEST, body.toString());
    }

    /**
     * Refus porteur d'un code chaîne, clé de traduction du front (ADR-1264). Les constructeurs
     * {@code (String, String)} lisent {@code (message, details)} : une clé passée en premier
     * argument finissait dans {@code message}.
     */
    public static RmesBadRequestException coded(String code, String message) {
        return new RmesBadRequestException(new JSONObject().put("code", code).put("message", message));
    }

    public RmesBadRequestException(String message) {
        super(HttpStatus.SC_BAD_REQUEST, message, "");
    }

    public RmesBadRequestException(String message, String details) {
        super(HttpStatus.SC_BAD_REQUEST, message, details);
    }

    public RmesBadRequestException(String message, JSONArray details) {
        super(HttpStatus.SC_BAD_REQUEST, message, details);
    }

    public RmesBadRequestException(int errorCode, String message, String details) {
        super(HttpStatus.SC_BAD_REQUEST, errorCode, message, details);
    }

    public RmesBadRequestException(int errorCode, String message, JSONArray details) {
        super(HttpStatus.SC_BAD_REQUEST, errorCode, message, details);
    }

    public RmesBadRequestException(int errorCode, String message) {
        super(HttpStatus.SC_BAD_REQUEST, errorCode, message, "");
    }

    public RmesBadRequestException(int errorCode, String message, JSONObject details) {
        super(HttpStatus.SC_BAD_REQUEST, errorCode, message, details);
    }
}
