package fr.insee.rmes.modules.commons.webservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Corps d'erreur de l'API (ADR-1264) : {@code message} toujours présent, en anglais, sans détail
 * technique ; {@code code} stable, clé de traduction côté front.
 * <p>
 * Seuls les champs utiles au filet des erreurs imprévues sont déclarés ici ; {@code params} et
 * {@code errors} arriveront avec les producteurs qui en ont besoin (ticket 18).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String message, String code) {

    /**
     * Réponse générique d'un statut, pour une erreur dont on ne relaie pas le message : celui-ci
     * est technique (exception imprévue) ou n'a pas été écrit pour l'utilisateur
     * ({@code ResponseStatusException}, exceptions de Spring MVC).
     */
    public static ApiError of(HttpStatusCode status) {
        return new ApiError(genericMessageOf(status), codeOf(status));
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
