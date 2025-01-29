package fr.insee.rmes.exceptions;

import fr.insee.rmes.webservice.*;
import fr.insee.rmes.webservice.classifications.ClassificationsResources;
import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import fr.insee.rmes.webservice.concepts.ConceptsCollectionsResources;
import fr.insee.rmes.webservice.concepts.ConceptsResources;
import fr.insee.rmes.webservice.datasets.DatasetResources;
import fr.insee.rmes.webservice.datasets.DistributionResources;
import fr.insee.rmes.webservice.operations.*;
import fr.insee.rmes.webservice.structures.StructureResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.NoSuchFileException;

@ControllerAdvice(assignableTypes = {
        CodeListsResources.class,
        MetadataReportResources.class,
        UserResources.class,
        ConceptsResources.class,
        ConceptsCollectionsResources.class,
        
        StructureResources.class,
        ClassificationsResources.class,

        // <DatasetModule>
        DatasetResources.class,
        DistributionResources.class,
        // </DatasetModule>

        // <OperationModule>
        FamilyResources.class,
        SeriesResources.class,
        OperationsResources.class,
        IndicatorsResources.class,
        DocumentsResources.class,
        MetadataReportResources.class
        // </OperationModule>
})
public class RmesExceptionHandler extends ResponseEntityExceptionHandler {

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
    public final ResponseEntity<String> handleRmesFileException(RmesFileException exception){
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(exception.toString());
    }

    @ExceptionHandler(RmesException.class)
    public final ResponseEntity<String> handleRmesException(RmesException exception){
        logger.error(exception.getMessageAndDetails(), exception);
        return ResponseEntity.internalServerError().body(exception.getDetails());
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<String> handleRmesException(NoSuchFileException exception){
        logger.error("NoSuchFileException "+ exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage() + " does not exist");
    }

}