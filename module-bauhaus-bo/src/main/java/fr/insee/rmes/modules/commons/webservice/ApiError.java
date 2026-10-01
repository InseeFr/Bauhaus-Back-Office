package fr.insee.rmes.modules.commons.webservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Corps d'erreur de l'API (ADR-1264) : {@code message} toujours présent, en anglais, sans détail
 * technique ; {@code code} stable, clé de traduction côté front ; {@code params} les valeurs à
 * interpoler dans cette traduction ; {@code errors}, réservé au 400 de validation, une erreur par
 * champ ({@code field} vaut {@code "body"} pour une erreur sur le corps entier).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String message, String code, Map<String, String> params, List<FieldError> errors) {

    /** Message de tout refus de validation : un lecteur qui ignore {@code errors} affiche une phrase. */
    public static final String INVALID_REQUEST_BODY_MESSAGE = "The submitted data is invalid";

    public static final String INVALID_REQUEST_BODY = "INVALID_REQUEST_BODY";

    /**
     * Erreur sur un champ. {@code field} vaut {@link #WHOLE_BODY} pour une erreur sur le corps entier ;
     * le front ({@code utils/api-errors.ts}) affiche alors le message seul.
     */
    public record FieldError(String field, String message) {

        public static final String WHOLE_BODY = "body";

        public static FieldError onWholeBody(String message) {
            return new FieldError(WHOLE_BODY, message);
        }
    }

    /** Des paramètres ou des erreurs vides sont absents : deux erreurs identiques restent égales. */
    public ApiError {
        params = params == null || params.isEmpty() ? null : Map.copyOf(params);
        errors = errors == null || errors.isEmpty() ? null : List.copyOf(errors);
    }

    public ApiError(String message, String code, Map<String, String> params) {
        this(message, code, params, null);
    }

    public ApiError(String message, String code) {
        this(message, code, null, null);
    }

    /** Refus de validation : une erreur par champ fautif. */
    public static ApiError invalid(String code, List<FieldError> errors) {
        return new ApiError(INVALID_REQUEST_BODY_MESSAGE, code, null, errors);
    }

    /**
     * Réponse générique d'un statut, pour une erreur dont on ne relaie pas le message : celui-ci
     * est technique (exception imprévue) ou n'a pas été écrit pour l'utilisateur
     * ({@code ResponseStatusException}, exceptions de Spring MVC).
     */
    public static ApiError of(HttpStatusCode status) {
        return new ApiError(genericMessageOf(status), codeOf(status));
    }

    /** Réponse d'un statut avec un message écrit pour le client, sans code métier propre. */
    public static ApiError of(HttpStatusCode status, String message) {
        return new ApiError(message, codeOf(status));
    }

    /** Même contenu, sous la forme attendue par la page {@code /error} de Spring Boot. */
    Map<String, Object> asMap() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("message", message);
        attributes.put("code", code);
        return attributes;
    }

    private static String codeOf(HttpStatusCode status) {
        HttpStatus known = HttpStatus.resolve(status.value());
        return known == null ? "HTTP_" + status.value() : known.name();
    }

    private static String genericMessageOf(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "The request is invalid.";
            case 401 -> "Authentication is required.";
            case 403 -> "You are not allowed to perform this action.";
            case 404 -> "The requested resource does not exist.";
            case 405 -> "This operation is not supported on this resource.";
            case 406 -> "The requested format is not available.";
            case 409 -> "The request conflicts with the current state of the resource.";
            case 415 -> "The submitted format is not supported.";
            case 502, 503, 504 -> "A service the application depends on is unavailable. Please try again later.";
            default ->
                status.is5xxServerError()
                        ? "An unexpected error occurred. Please try again later."
                        : "The request could not be processed.";
        };
    }
}
