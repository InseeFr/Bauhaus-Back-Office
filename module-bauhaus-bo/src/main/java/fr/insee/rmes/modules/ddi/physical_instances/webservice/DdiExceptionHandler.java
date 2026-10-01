package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.DdiItemNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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

    public record ErrorMessageResponse(String message) {}

    /** Erreur traduisible : le front traduit {@code code} avec {@code params}, {@code message} en repli. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record CodedErrorResponse(String message, String code, Map<String, String> params) {}

    /** Groupe, étude ou instance physique inconnus de Colectica. */
    @ExceptionHandler(DdiItemNotFoundException.class)
    public ResponseEntity<CodedErrorResponse> handleItemNotFound(DdiItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CodedErrorResponse(ex.getMessage(), ex.code().name(), ex.params()));
    }

    /** Valeurs sentinelles (#1566) : labels obligatoires manquants dans le payload de save. */
    @ExceptionHandler(InvalidSentinelValuesException.class)
    public ResponseEntity<ErrorMessageResponse> handleInvalidSentinelValues(InvalidSentinelValuesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessageResponse(ex.getMessage()));
    }

    /**
     * Colectica injoignable ou en erreur. Spring cherche aussi le type dans les causes : les accès
     * Colectica enveloppent souvent l'erreur du client HTTP dans une {@code RuntimeException}.
     */
    @ExceptionHandler({ResourceAccessException.class, HttpServerErrorException.class})
    public ResponseEntity<CodedErrorResponse> handleColecticaUnavailable(Exception ex, HttpServletRequest request) {
        logger.error("Colectica unavailable on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new CodedErrorResponse(
                        "The DDI repository (Colectica) is unavailable. Please try again later.",
                        COLECTICA_UNAVAILABLE,
                        null));
    }
}
