package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Ordre explicite : doit passer avant le filet {@link UnexpectedErrorHandler}. */
@ControllerAdvice
@Order(3)
public class DomainExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DomainExceptionHandler.class);

    /** Les détails sont ceux d'une cause technique : ils ne partent que dans les logs. */
    @ExceptionHandler({GenericInternalServerException.class})
    public final ResponseEntity<ApiError> genericInternalServerException(GenericInternalServerException exception) {
        logger.error("GenericInternalServerException: {}", exception.getDetails(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler({NotFoundAttributeException.class})
    public final ResponseEntity<ApiError> notFoundAttributeException(NotFoundAttributeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.NOT_FOUND, "The attribute " + exception.getId() + " does not exist."));
    }

    @ExceptionHandler({OperationDocumentationRubricWithoutRangeException.class})
    public final ResponseEntity<ApiError> operationDocumentationRubricWithoutRangeException(
            OperationDocumentationRubricWithoutRangeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "At least one attribute has no range: " + exception.getId()));
    }
}
