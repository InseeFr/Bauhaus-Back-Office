package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
@ConditionalOnModule("operations")
public class DocumentDescriptionResources {

    public record ErrorMessageResponse(String message) {}

    private final DocumentDescriptionService documentDescriptionService;

    public DocumentDescriptionResources(DocumentDescriptionService documentDescriptionService) {
        this.documentDescriptionService = documentDescriptionService;
    }

    @GetMapping(value = "/documents/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<DocumentDescriptionResponse> getDocument(@PathVariable("id") String id)
            throws RmesException, DocumentNotFoundException {
        return describe(DocumentKind.DOCUMENT, id);
    }

    @GetMapping(value = "/liens/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<DocumentDescriptionResponse> getLink(@PathVariable("id") String id)
            throws RmesException, DocumentNotFoundException {
        return describe(DocumentKind.LINK, id);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleDocumentNotFound(DocumentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageResponse(e.getMessage()));
    }

    private ResponseEntity<DocumentDescriptionResponse> describe(DocumentKind kind, String id)
            throws RmesException, DocumentNotFoundException {
        return ResponseEntity.ok(
                DocumentDescriptionResponse.fromDomain(documentDescriptionService.getDescription(kind, id)));
    }
}
