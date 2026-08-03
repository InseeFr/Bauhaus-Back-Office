package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DdiResources.class)
public class DdiExceptionHandler {

    public record ErrorMessageResponse(String message) {}

    @ExceptionHandler(StudyUnitNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleStudyUnitNotFound(StudyUnitNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorMessageResponse(ex.getMessage()));
    }

}
