package fr.insee.rmes.webservice.exceptions.handlers;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import fr.insee.rmes.webservice.distribution.DistributionResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = {DatasetResources.class, DistributionResources.class})
public class RmesExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<Object> handleException(RmesException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getDetails());
    }
}