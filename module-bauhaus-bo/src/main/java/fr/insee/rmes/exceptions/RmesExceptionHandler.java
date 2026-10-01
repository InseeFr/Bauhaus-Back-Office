package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.webservice.ApiError;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Traduction des {@link RmesException}, pour tous les contrôleurs. Elle était réservée à une liste
 * {@code assignableTypes} : un contrôleur oublié laissait sortir l'exception brute (ticket 1 de
 * l'audit #1264).
 * <p>
 * Une {@link RmesException} garde son statut et son message (ticket 2) et répond un
 * {@link ApiError} (ADR-1264, ticket 18), construit à partir de {@link RmesException#getDetails()} :
 * <ul>
 *   <li>{@code code}, entier ou chaîne, est relayé en chaîne ;</li>
 *   <li>les autres clés de premier niveau (constructeur {@code JSONObject}) sont les paramètres de
 *       traduction ;</li>
 *   <li>{@code details} ne part que dans les logs : il est souvent technique.</li>
 * </ul>
 */
@ControllerAdvice
@Order(2)
public class RmesExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RmesExceptionHandler.class);

    private static final String MESSAGE = "message";

    private static final String CODE = "code";

    private static final String DETAILS = "details";

    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<ApiError> handleRmesException(RmesException exception) {
        HttpStatus status = statusOf(exception);
        if (status.is5xxServerError()) {
            logger.error("RmesException (status {}): {}", status.value(), exception.getDetails(), exception);
        } else {
            logger.warn("RmesException (status {}): {}", status.value(), exception.getDetails(), exception);
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyOf(exception, status));
    }

    @ExceptionHandler(RmesFileException.class)
    public final ResponseEntity<ApiError> handleRmesFileException(RmesFileException exception) {
        logger.error("File error on {}", exception.getFileName(), exception);
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<ApiError> handleRmesException(NoSuchFileException exception) {
        logger.error("NoSuchFileException {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "The requested file does not exist."));
    }

    /** Un statut hors des erreurs HTTP (0, 200…) ne peut pas décrire un échec : c'est une panne. */
    private static HttpStatus statusOf(RmesException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatus());
        return status != null && status.isError() ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static ApiError bodyOf(RmesException exception, HttpStatus status) {
        JSONObject json = parse(exception.getDetails(), status);
        String message = json.optString(MESSAGE);
        String code = json.has(CODE) ? String.valueOf(json.get(CODE)) : null;
        Map<String, String> params = new TreeMap<>();
        json.keySet().stream()
                .filter(key -> !Set.of(MESSAGE, CODE, DETAILS).contains(key))
                .forEach(key -> params.put(key, String.valueOf(json.get(key))));
        return new ApiError(message.isBlank() ? ApiError.of(status).message() : message, code, params);
    }

    /**
     * Des {@code details} qui ne sont pas du JSON sont un texte brut : relayé pour une erreur de
     * saisie, écrite pour l'utilisateur ; remplacé par un message générique pour une panne, où
     * c'est le message technique d'une cause ({@code RmesException(String, Exception)}).
     */
    private static JSONObject parse(String details, HttpStatus status) {
        try {
            return details == null ? new JSONObject() : new JSONObject(details);
        } catch (JSONException notJson) {
            JSONObject json = new JSONObject();
            if (status.is4xxClientError()) {
                json.put(MESSAGE, details);
            }
            return json;
        }
    }
}
