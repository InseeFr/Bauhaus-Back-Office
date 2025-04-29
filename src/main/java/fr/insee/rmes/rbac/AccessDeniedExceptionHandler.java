package fr.insee.rmes.rbac;

import fr.insee.rmes.webservice.operations.FamilyResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice(assignableTypes = {FamilyResources.class})
public class AccessDeniedExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<String> handleAccessDeniedException(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
