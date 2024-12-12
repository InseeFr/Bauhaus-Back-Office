package fr.insee.rmes.exceptions;

import fr.insee.rmes.webservice.UserResources;
import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import fr.insee.rmes.webservice.dataset.DatasetResources;
import fr.insee.rmes.webservice.distribution.DistributionResources;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import fr.insee.rmes.webservice.operations.MetadataReportResources;
import fr.insee.rmes.webservice.operations.OperationsResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.NoSuchFileException;

@ControllerAdvice(assignableTypes = {
        DatasetResources.class,
        DistributionResources.class,
        CodeListsResources.class,
        MetadataReportResources.class,
        OperationsResources.class,
        DocumentsResources.class,
        UserResources.class,

})
public class RmesExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({RmesBadRequestException.class, RmesNotFoundException.class, RmesNotAcceptableException.class, RmesUnauthorizedException.class})
    public final ResponseEntity<String> handleSubclassesOfRmesException(RmesException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception.getDetails());
    }

    @ExceptionHandler(RmesFileException.class)
    public final ResponseEntity<String> handleRmesFileException(RmesFileException exception){
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(exception.toString());
    }

    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<String> handleRmesException(RmesException exception){
        logger.error(exception.getMessageAndDetails(), exception);
        return ResponseEntity.internalServerError().body(exception.getMessage());
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<String> handleRmesException(NoSuchFileException exception){
        logger.error("NoSuchFileException "+ exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage() + " does not exist");
    }

}