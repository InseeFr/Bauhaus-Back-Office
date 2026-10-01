package fr.insee.rmes.modules.commons.webservice;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Filet des erreurs que personne d'autre ne traite (ticket 1 de l'audit #1264, ADR-1264) : la
 * réponse garde son statut et porte un {@link ApiError} générique ; le détail technique ne va que
 * dans les logs.
 * <p>
 * {@link Order}({@link Ordered#LOWEST_PRECEDENCE}) est indispensable : Spring interroge les
 * advices dans l'ordre et s'arrête au premier qui sait traiter l'exception. Un filet sur
 * {@link Exception} placé avant un gestionnaire spécialisé le rendrait muet ; tout autre advice
 * doit donc déclarer un ordre plus prioritaire (vérifié par {@code UnexpectedErrorContractTest}).
 * <p>
 * Remplace la réponse {@code ProblemDetail} que produisait l'héritage de
 * {@code ResponseEntityExceptionHandler}, retiré par l'ADR.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class UnexpectedErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(UnexpectedErrorHandler.class);

    /**
     * Les exceptions de Spring MVC ({@code NoResourceFoundException}, {@code
     * HttpRequestMethodNotSupportedException}…) et les {@code ResponseStatusException} implémentent
     * {@link ErrorResponse} : leur statut est juste, leur texte n'est pas écrit pour l'utilisateur.
     * Toute autre exception est une panne imprévue : 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handle(Exception exception, HttpServletRequest request) {
        if (!(exception instanceof ErrorResponse errorResponse)) {
            logger.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), exception);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR));
        }
        HttpStatusCode status = errorResponse.getStatusCode();
        if (status.is5xxServerError()) {
            logger.error(
                    "{} {} failed with status {}", request.getMethod(), request.getRequestURI(), status, exception);
        } else {
            logger.debug(
                    "{} {} rejected with status {}", request.getMethod(), request.getRequestURI(), status, exception);
        }
        return ResponseEntity.status(status)
                .headers(errorResponse.getHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(status));
    }
}
