package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingSchemeException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DdiResources.class)
public class DdiExceptionHandler {

    public record ErrorMessageResponse(String message) {}

    /** Erreur traduisible : le front traduit {@code code} avec {@code params}, {@code message} en repli. */
    public record CodedErrorResponse(String message, String code, Map<String, String> params) {}

    @ExceptionHandler(StudyUnitNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleStudyUnitNotFound(StudyUnitNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageResponse(ex.getMessage()));
    }

    /** Valeurs sentinelles (#1566) : labels obligatoires manquants dans le payload de save. */
    @ExceptionHandler(InvalidSentinelValuesException.class)
    public ResponseEntity<ErrorMessageResponse> handleInvalidSentinelValues(InvalidSentinelValuesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessageResponse(ex.getMessage()));
    }

    /**
     * Le Group ou la StudyUnit de l'instance n'expose pas le scheme sous lequel ranger ses objets :
     * ils sont créés en amont, le save n'en crée jamais à la volée.
     */
    @ExceptionHandler(MissingSchemeException.class)
    public ResponseEntity<CodedErrorResponse> handleMissingScheme(MissingSchemeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CodedErrorResponse(ex.getMessage(), ex.code().name(), ex.params()));
    }
}
