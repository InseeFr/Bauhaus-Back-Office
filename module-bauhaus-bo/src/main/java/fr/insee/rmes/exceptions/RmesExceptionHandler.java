package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import java.nio.file.NoSuchFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Traduction des {@link RmesException}, pour tous les contrôleurs. Elle était réservée à une liste
 * {@code assignableTypes} : un contrôleur oublié laissait sortir l'exception brute (ticket 1 de
 * l'audit #1264).
 */
@ControllerAdvice
@Order(2)
public class RmesExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RmesExceptionHandler.class);

    @ExceptionHandler({
        RmesBadRequestException.class,
        RmesNotFoundException.class,
        RmesNotAcceptableException.class,
        RmesUnauthorizedException.class
    })
    public final ResponseEntity<String> handleSubclassesOfRmesException(RmesException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getDetails());
    }

    @ExceptionHandler(RmesFileException.class)
    public final ResponseEntity<String> handleRmesFileException(RmesFileException exception) {
        logger.error("File error: {}", exception, exception);
        return ResponseEntity.internalServerError().body(exception.toString());
    }

    /** Le message d'une {@link RmesException} est le plus souvent {@code null} : c'est {@code details} qui parle. */
    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<String> handleRmesException(RmesException exception) {
        logger.error("RmesException (status {}): {}", exception.getStatus(), exception.getDetails(), exception);
        return ResponseEntity.internalServerError().body(exception.getDetails());
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<String> handleRmesException(NoSuchFileException exception) {
        logger.error("NoSuchFileException " + exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage() + " does not exist");
    }
}
