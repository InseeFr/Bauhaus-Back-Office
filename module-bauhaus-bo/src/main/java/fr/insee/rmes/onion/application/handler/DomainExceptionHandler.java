package fr.insee.rmes.onion.application.handler;

import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class DomainExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({ GenericInternalServerException.class })
    public final ResponseEntity<String> genericInternalServerException(GenericInternalServerException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getDetails());
    }

    @ExceptionHandler({ NotFoundAttributeException.class })
    public final ResponseEntity<String> notFoundAttributeException(NotFoundAttributeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getId());
    }

    @ExceptionHandler({ OperationDocumentationRubricWithoutRangeException.class })
    public final ResponseEntity<String> operationDocumentationRubricWithoutRangeException(OperationDocumentationRubricWithoutRangeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least one attribute don't have range " + exception.getId());
    }
}
