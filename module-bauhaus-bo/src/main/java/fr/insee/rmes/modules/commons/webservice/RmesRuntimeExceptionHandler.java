package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Ordre explicite : doit passer avant le filet {@link UnexpectedErrorHandler}. */
@RestControllerAdvice
@Order(3)
public class RmesRuntimeExceptionHandler {

    @ExceptionHandler(RmesRuntimeBadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(RmesRuntimeBadRequestException ex) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }
}
