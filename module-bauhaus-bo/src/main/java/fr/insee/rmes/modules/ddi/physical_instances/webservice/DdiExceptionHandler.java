package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.webservice.ApiError;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.DdiItemNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Erreurs des contrôleurs DDI, au format de l'ADR-1264 : {@code message} (anglais, repli) et, pour
 * les erreurs fréquentes, un {@code code} que le front traduit. Tout le reste remonte au filet
 * {@code UnexpectedErrorHandler} (500 {@code {message}}) : les contrôleurs n'interceptent plus eux-mêmes.
 *
 * <p>Ordre explicite : doit passer avant le filet global des erreurs imprévues.
 */
@RestControllerAdvice(basePackageClasses = DdiExceptionHandler.class)
@Order(3)
public class DdiExceptionHandler {

    static final String COLECTICA_UNAVAILABLE = "COLECTICA_UNAVAILABLE";

    private static final Logger logger = LoggerFactory.getLogger(DdiExceptionHandler.class);

    /** Groupe, étude ou instance physique inconnus de Colectica. */
    @ExceptionHandler(DdiItemNotFoundException.class)
    public ResponseEntity<ApiError> handleItemNotFound(DdiItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(ex.getMessage(), ex.code().name(), ex.params()));
    }

    /** Valeurs sentinelles (#1566) : labels obligatoires manquants dans le payload de save. */
    @ExceptionHandler(InvalidSentinelValuesException.class)
    public ResponseEntity<ApiError> handleInvalidSentinelValues(InvalidSentinelValuesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(ex.getMessage(), ex.code().name(), ex.params()));
    }

    /**
     * Colectica injoignable ou en erreur. Spring cherche aussi le type dans les causes : les accès
     * Colectica enveloppent souvent l'erreur du client HTTP dans une {@code RuntimeException}.
     */
    @ExceptionHandler({ResourceAccessException.class, HttpServerErrorException.class})
    public ResponseEntity<ApiError> handleColecticaUnavailable(Exception ex, HttpServletRequest request) {
        logger.error("Colectica unavailable on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ApiError(
                        "The DDI repository (Colectica) is unavailable. Please try again later.",
                        COLECTICA_UNAVAILABLE));
    }
}
