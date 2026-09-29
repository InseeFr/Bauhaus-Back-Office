package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.webservice.ApiError;
import java.nio.file.NoSuchFileException;
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
 * Une {@link RmesException} garde son statut et son message (ticket 2) : le corps reste celui de
 * {@link RmesException#getDetails()}, que le front lit déjà ({@code code}, {@code message},
 * {@code details} et paramètres de traduction), complété d'un {@code message} quand il en manque.
 */
@ControllerAdvice
@Order(2)
public class RmesExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RmesExceptionHandler.class);

    private static final String MESSAGE = "message";

    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<String> handleRmesException(RmesException exception) {
        HttpStatus status = statusOf(exception);
        if (status.is5xxServerError()) {
            logger.error("RmesException (status {}): {}", status.value(), exception.getDetails(), exception);
        } else {
            logger.warn("RmesException (status {}): {}", status.value(), exception.getDetails(), exception);
        }
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyOf(exception, status).toString());
    }

    @ExceptionHandler(RmesFileException.class)
    public final ResponseEntity<ApiError> handleRmesFileException(RmesFileException exception) {
        logger.error("File error on {}", exception.getFileName(), exception);
        return ResponseEntity.internalServerError().body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<String> handleRmesException(NoSuchFileException exception) {
        logger.error("NoSuchFileException " + exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage() + " does not exist");
    }

    /** Un statut hors des erreurs HTTP (0, 200…) ne peut pas décrire un échec : c'est une panne. */
    private static HttpStatus statusOf(RmesException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatus());
        return status != null && status.isError() ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Des {@code details} qui ne sont pas du JSON sont un texte brut : relayé pour une erreur de
     * saisie, écrite pour l'utilisateur ; remplacé par un message générique pour une panne, où
     * c'est le message technique d'une cause ({@code RmesException(String, Exception)}).
     */
    private static JSONObject bodyOf(RmesException exception, HttpStatus status) {
        String details = exception.getDetails();
        JSONObject body;
        try {
            body = details == null ? new JSONObject() : new JSONObject(details);
        } catch (JSONException notJson) {
            body = new JSONObject();
            if (status.is4xxClientError()) {
                body.put(MESSAGE, details);
            }
        }
        if (body.optString(MESSAGE).isBlank()) {
            body.put(MESSAGE, ApiError.of(status).message());
        }
        return body;
    }
}
