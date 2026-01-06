package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.swagger.model.operations.documentation.DocumentId;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

@RestController
@RequestMapping("/documents")
@ConditionalOnExpression("operations")
public class DocumentsResources {

    static final Logger logger = LoggerFactory.getLogger(DocumentsResources.class);

    private final DocumentsService documentsService;

    @Value("${fr.insee.rmes.bauhaus.extensions}")
    private String properties;

    public DocumentsResources(DocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<String> getDocuments() throws RmesException {
        String documents = documentsService.getDocuments();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(documents);
    }


    @GetMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<String> getDocument(@PathVariable(Constants.ID) String id) throws RmesException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentsService.getDocument(id).toString());
    }

    @GetMapping(value = "/document/{id}/file", produces = "*/*")
    public ResponseEntity<Resource> downloadDocument(@PathVariable(Constants.ID) String id) throws RmesException {
        return documentsService.downloadDocument(id);
    }

    @PostMapping(value = "/document",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<String> setDocument(
            @RequestParam(value = "body") String body,
            @RequestParam(value = "file") MultipartFile documentFile
    ) throws RmesException, IOException {
        String id;
        String documentName = documentFile.getOriginalFilename();
        id = documentsService.createDocument(body, documentFile.getInputStream(), documentName);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }

    @PutMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<String> setDocument(
            @PathVariable(Constants.ID) DocumentId id,
            @RequestBody String body) throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        documentsService.setDocument(documentIdString, body);
        logger.info("Update document : {}", id);
        return ResponseEntity.ok(documentIdString);
    }


    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/document/{id}/file",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> changeDocument(
            @RequestParam(value = "file") MultipartFile documentFile,
            @PathVariable(Constants.ID) String id
    ) throws RmesException, IOException {
        String documentName = documentFile.getOriginalFilename();
        verifyExtension(documentName);
        var url = documentsService.changeDocument(id, documentFile.getInputStream(), documentName);
        return ResponseEntity.status(HttpStatus.OK).body(url);
    }


    @DeleteMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<Object> deleteDocument(
            @PathVariable(Constants.ID) DocumentId id)
            throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        return ResponseEntity.status(documentsService.deleteDocument(documentIdString)).body(documentIdString);
    }


    @GetMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    public ResponseEntity<String> getLink(@PathVariable(Constants.ID) String id) throws RmesException {

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(documentsService.getLink(id).toString());

    }

    @PostMapping(value = "/link",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<Object> setLink(
            @RequestParam(value = "body") String body
    ) throws RmesException, IOException {
        return ResponseEntity.status(HttpStatus.OK).body(documentsService.setLink(body));
    }

    @PutMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    public ResponseEntity<Object> setLink(
            @PathVariable(Constants.ID) DocumentId id,
            @RequestBody String body
    )
            throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        return ResponseEntity.ok(documentsService.setLink(documentIdString, body));
    }

    @DeleteMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    public ResponseEntity<Object> deleteLink(
            @PathVariable(Constants.ID) DocumentId id
    ) throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        return ResponseEntity.status(documentsService.deleteLink(documentIdString)).body(documentIdString);
    }


    // Méthode pour encoder et valider le DocumentID
    private String sanitizeDocumentId(String documentIdString) {
        if (documentIdString == null || documentIdString.isEmpty()) {
            return null;
        }
        //on peut ajouter d'autres contrôles
        return documentIdString.replaceAll("[/<>:\"]", "");
    }

    private void verifyExtension(String fileName) throws RmesException {

        String[] extensionsExpected = properties.split(",");
        String[] fileNameElements = fileName.split("\\.");

        if (fileNameElements.length<2){
            throw new RmesException(0,"RmesException","Invalid File Extension");
        }
        else{
            String extensionsActual=fileNameElements[fileNameElements.length-1];
            boolean isKnownActualExtension = Arrays.stream(extensionsExpected).anyMatch(extensionExpected -> extensionExpected.equals(extensionsActual));

            if (!isKnownActualExtension){
                throw new RmesException(0,"RmesException","Invalid File Extension");
            }
        }
    }
}
