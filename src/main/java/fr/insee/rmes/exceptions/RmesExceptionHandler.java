package fr.insee.rmes.exceptions;

import fr.insee.rmes.webservice.dataset.DatasetResources;
import fr.insee.rmes.webservice.distribution.DistributionResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = {DatasetResources.class, DistributionResources.class})
public class RmesExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(RmesException.class)
    public ResponseEntity<RestMessage> handleRmesException(RmesException ex) {
        return new ResponseEntity<>(ex.toRestMessage(), HttpStatus.valueOf(ex.getStatus()));
    }
}