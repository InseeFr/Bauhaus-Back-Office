package fr.insee.rmes.graphdb.exceptions;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(1)
public class GraphDbExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({ DatabaseQueryException.class })
    public final ResponseEntity<String> genericInternalServerException(DatabaseQueryException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
