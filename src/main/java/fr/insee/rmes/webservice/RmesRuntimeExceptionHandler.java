package fr.insee.rmes.webservice;

import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RmesRuntimeExceptionHandler {

    @ExceptionHandler(RmesRuntimeBadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(RmesRuntimeBadRequestException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}