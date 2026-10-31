package fr.insee.rmes.modules.commons.webservice;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

/**
 * Corps de la page {@code /error} de Spring Boot, atteinte par les erreurs qui échappent à
 * {@link UnexpectedErrorHandler} ({@code sendError}, exception dans un filtre) : même contrat
 * {@link ApiError} que le reste de l'API, au lieu de {@code {timestamp, status, error, path}}.
 */
@Component
public class ApiErrorAttributes extends DefaultErrorAttributes {

    private static final String STATUS = "status";

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> defaults = super.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        return ApiError.of(statusOf(defaults)).asMap();
    }

    private static HttpStatusCode statusOf(Map<String, Object> defaults) {
        return defaults.get(STATUS) instanceof Integer status
                ? HttpStatusCode.valueOf(status)
                : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
