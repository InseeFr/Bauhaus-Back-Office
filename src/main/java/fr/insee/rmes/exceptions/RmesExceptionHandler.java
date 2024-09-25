package fr.insee.rmes.exceptions;

import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import fr.insee.rmes.webservice.distribution.DistributionResources;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import fr.insee.rmes.webservice.operations.MetadataReportResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(assignableTypes = {
        DatasetResources.class,
        DistributionResources.class,
        CodeListsResources.class,
        MetadataReportResources.class,
        DocumentsResources.class
})
public class RmesExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({RmesBadRequestException.class, RmesNotFoundException.class, RmesNotAcceptableException.class, RmesUnauthorizedException.class})
    public final ResponseEntity<String> handleSubclassesOfRmesException(RmesException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getDetails());
    }

    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<String> handleRmesException(RmesException exception){
        logger.error(exception.getMessageAndDetails(), exception);
        return ResponseEntity.internalServerError().body(exception.getMessage());
    }

}