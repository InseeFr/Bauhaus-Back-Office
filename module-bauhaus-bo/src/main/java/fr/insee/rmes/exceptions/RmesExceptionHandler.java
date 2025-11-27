package fr.insee.rmes.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentsResources;
import fr.insee.rmes.modules.operations.families.webservice.FamilyResources;
import fr.insee.rmes.modules.operations.indicators.webservice.IndicatorsResources;
import fr.insee.rmes.modules.operations.msd.webservice.MetadataReportResources;
import fr.insee.rmes.modules.operations.operations.webservice.OperationsResources;
import fr.insee.rmes.modules.operations.series.webservice.SeriesResources;
import fr.insee.rmes.modules.commons.webservice.PublicResources;
import fr.insee.rmes.modules.users.webservice.UserResources;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.ClassificationsResources;
import fr.insee.rmes.modules.codeslists.codeslists.webservice.CodesListsResources;
import fr.insee.rmes.modules.commons.webservice.concepts.ConceptsCollectionsResources;
import fr.insee.rmes.modules.concepts.concept.webservice.ConceptsResources;
import fr.insee.rmes.modules.datasets.datasets.webservice.DatasetResources;
import fr.insee.rmes.modules.datasets.distributions.webservice.DistributionResources;
import fr.insee.rmes.modules.structures.structures.webservice.StructureResources;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.NoSuchFileException;

@ControllerAdvice(assignableTypes = {
        PublicResources.class,

        CodesListsResources.class,
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
@Order(2)
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
        logger.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(exception.getDetails());
    }

    @ExceptionHandler(NoSuchFileException.class)
    public final ResponseEntity<String> handleRmesException(NoSuchFileException exception){
        logger.error("NoSuchFileException "+ exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage() + " does not exist");
    }

}