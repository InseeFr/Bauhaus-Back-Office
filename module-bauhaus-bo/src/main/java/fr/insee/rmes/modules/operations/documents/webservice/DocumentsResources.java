package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.StoredFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.StoredFile;
import fr.insee.rmes.modules.operations.documents.domain.model.UploadedFile;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentManagementService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Gestion des documents et des liens des rapports qualité. Le contrat HTTP est celui que l'IHM
 * utilise depuis toujours ; les refus du domaine y sont traduits en erreurs portant les codes
 * historiques, rendues par RmesExceptionHandler.
 */
@RestController
@RequestMapping("/documents")
@ConditionalOnModule("operations")
public class DocumentsResources {

    /** Un identifiant qui n'est pas un segment d'IRI simple ne désigne aucun document. */
    private static final Pattern DOCUMENT_ID = Pattern.compile("[\\w-]+");

    private final DocumentManagementService documents;

    public DocumentsResources(DocumentManagementService documents) {
        this.documents = documents;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<DocumentResponse>> getDocuments() throws RmesException {
        return ResponseEntity.ok(
                documents.getAll().stream().map(DocumentResponse::fromDomain).toList());
    }

    @GetMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable("id") String id) throws RmesException {
        return ResponseEntity.ok(describe(DocumentKind.DOCUMENT, id));
    }

    @PublicEndpoint
    @GetMapping(value = "/document/{id}/file", produces = "*/*")
    public ResponseEntity<Resource> downloadDocument(@PathVariable("id") String id) throws RmesException {
        try {
            StoredFile file = documents.download(checkedId(DocumentKind.DOCUMENT, id));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.name() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(file.content()));
        } catch (DocumentNotFoundException _) {
            throw notFound(DocumentKind.DOCUMENT, id);
        } catch (StoredFileNotFoundException e) {
            throw new RmesNotFoundException(e.getMessage(), e.getMessage());
        }
    }

    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<String> createDocument(
            @RequestParam("body") String body, @RequestParam("file") MultipartFile file)
            throws RmesException, IOException {
        try {
            String id = documents.createDocument(DocumentRequest.fromJson(body).toForm(), uploaded(file));
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            return ResponseEntity.created(location).body(id);
        } catch (DocumentRuleViolationException e) {
            throw refused(e);
        }
    }

    @PutMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<String> updateDocument(
            @PathVariable("id") String id, @Valid @RequestBody DocumentRequest body) throws RmesException {
        return ResponseEntity.ok(update(DocumentKind.DOCUMENT, id, body));
    }

    /** Répond la nouvelle URL du document, ou un corps vide quand le fichier garde son nom. */
    @PutMapping(value = "/document/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<String> replaceFile(@PathVariable("id") String id, @RequestParam("file") MultipartFile file)
            throws RmesException, IOException {
        try {
            return ResponseEntity.ok(documents
                    .replaceFile(checkedId(DocumentKind.DOCUMENT, id), uploaded(file))
                    .orElse(""));
        } catch (DocumentNotFoundException _) {
            throw notFound(DocumentKind.DOCUMENT, id);
        } catch (DocumentRuleViolationException e) {
            throw refused(e);
        }
    }

    @DeleteMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<String> deleteDocument(@PathVariable("id") String id) throws RmesException {
        return ResponseEntity.ok(delete(DocumentKind.DOCUMENT, id));
    }

    @GetMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<DocumentResponse> getLink(@PathVariable("id") String id) throws RmesException {
        return ResponseEntity.ok(describe(DocumentKind.LINK, id));
    }

    @PostMapping("/link")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<String> createLink(@RequestParam("body") String body) throws RmesException {
        try {
            return ResponseEntity.ok(
                    documents.createLink(DocumentRequest.fromJson(body).toForm()));
        } catch (DocumentRuleViolationException e) {
            throw refused(e);
        }
    }

    @PutMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<String> updateLink(@PathVariable("id") String id, @Valid @RequestBody DocumentRequest body)
            throws RmesException {
        return ResponseEntity.ok(update(DocumentKind.LINK, id, body));
    }

    @DeleteMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<String> deleteLink(@PathVariable("id") String id) throws RmesException {
        return ResponseEntity.ok(delete(DocumentKind.LINK, id));
    }

    private DocumentResponse describe(DocumentKind kind, String id) throws RmesException {
        try {
            return DocumentResponse.fromDomain(documents.get(kind, checkedId(kind, id)));
        } catch (DocumentNotFoundException _) {
            throw notFound(kind, id);
        }
    }

    private String update(DocumentKind kind, String id, DocumentRequest body) throws RmesException {
        try {
            documents.update(kind, checkedId(kind, id), body.toForm());
            return id;
        } catch (DocumentNotFoundException _) {
            throw notFound(kind, id);
        } catch (DocumentRuleViolationException e) {
            throw refused(e);
        }
    }

    private String delete(DocumentKind kind, String id) throws RmesException {
        try {
            documents.delete(kind, checkedId(kind, id));
            return id;
        } catch (DocumentNotFoundException _) {
            throw notFound(kind, id);
        } catch (DocumentRuleViolationException e) {
            throw refused(e);
        }
    }

    private static String checkedId(DocumentKind kind, String id) throws DocumentNotFoundException {
        if (!DOCUMENT_ID.matcher(id).matches()) {
            throw new DocumentNotFoundException(kind, id);
        }
        return id;
    }

    private static UploadedFile uploaded(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        return new UploadedFile(name, file.getInputStream(), file.getSize());
    }

    private static RmesNotFoundException notFound(DocumentKind kind, String id) {
        String what = kind == DocumentKind.LINK ? "Link" : "Document";
        return new RmesNotFoundException(
                ErrorCodes.DOCUMENT_UNKNOWN_ID, "Cannot find " + what + " with id : " + id, id);
    }

    /** Les codes et statuts que l'IHM connaît pour chacun des refus. */
    private static RmesException refused(DocumentRuleViolationException e) {
        String message = e.getMessage();
        String detail = e.detail();
        return switch (e.violation()) {
            case LABEL_LG1_ALREADY_USED ->
                new RmesBadRequestException(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1, message);
            case LABEL_LG2_ALREADY_USED ->
                new RmesBadRequestException(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG2, message);
            case LINK_EMPTY_URL -> new RmesNotAcceptableException(ErrorCodes.LINK_EMPTY_URL, message, detail);
            case LINK_BAD_URL -> new RmesNotAcceptableException(ErrorCodes.LINK_BAD_URL, message, detail);
            case LINK_URL_ALREADY_USED -> new RmesNotAcceptableException(ErrorCodes.LINK_EXISTING_URL, message, detail);
            case FILE_EMPTY_NAME -> new RmesNotAcceptableException(ErrorCodes.DOCUMENT_EMPTY_NAME, message, detail);
            case FILE_FORBIDDEN_CHARACTERS ->
                new RmesNotAcceptableException(ErrorCodes.DOCUMENT_FORBIDDEN_CHARACTER_NAME, message, detail);
            case FILE_ALREADY_EXISTS ->
                new RmesBadRequestException(ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE, message, detail);
            case FILE_EXTENSION_NOT_ALLOWED -> new RmesBadRequestException(message, detail);
            case REFERENCED_BY_SIMS ->
                new RmesBadRequestException(ErrorCodes.DOCUMENT_DELETION_LINKED, message, detail);
        };
    }
}
