package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.Bauhaus;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.config.swagger.model.operations.documentation.DocumentId;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

@RestController
@RequestMapping("/documents")
@SecurityRequirement(name = "bearerAuth")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
@Tag(name = Constants.DOCUMENT, description = "Document API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class DocumentsResources {

    static final Logger logger = LoggerFactory.getLogger(DocumentsResources.class);

    private final DocumentsService documentsService;

    private final Properties properties;

    public DocumentsResources(DocumentsService documentsService, Properties properties) {
        this.documentsService = documentsService;
        this.properties = properties;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of documents and links",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Document.class))))})
    public ResponseEntity<String> getDocuments() throws RmesException {
        String documents = documentsService.getDocuments();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(documents);
    }


    @GetMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get a Document",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = Document.class)))})
    public ResponseEntity<String> getDocument(@PathVariable(Constants.ID) String id) throws RmesException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(documentsService.getDocument(id).toString());
    }

    @GetMapping(value = "/document/{id}/file", produces = "*/*")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Download the Document file")
    public ResponseEntity<Resource> downloadDocument(@PathVariable(Constants.ID) String id) throws RmesException {
        return documentsService.downloadDocument(id);
    }

    @PostMapping(value = "/document",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    @Operation(summary = "Create document")
    public ResponseEntity<String> setDocument(
            @Parameter(description = Constants.DOCUMENT, required = true, schema = @Schema(implementation = Document.class))
            @RequestParam(value = "body") String body,
            @Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file"))
            @RequestParam(value = "file") MultipartFile documentFile
    ) throws RmesException, IOException {
        String id;
        String documentName = documentFile.getOriginalFilename();
        id = documentsService.createDocument(body, documentFile.getInputStream(), documentName);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @PutMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    @Operation(summary = "Update document ")
    public ResponseEntity<String> setDocument(
            @Parameter(
                    description = "Id",
                    required = true,
                    schema = @Schema (type=Constants.TYPE_STRING)
            )
            @PathVariable(Constants.ID) DocumentId id,
            @Parameter(
                    description = Constants.DOCUMENT,
                    required = true,
                    schema = @Schema(implementation = Document.class)
            )
            @RequestBody String body) throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        documentsService.setDocument(documentIdString, body);
        logger.info("Update document : {}", id);
        return ResponseEntity.ok(documentIdString);
    }


    @Operation(summary = "Change document file")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/document/{id}/file",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> changeDocument(
            @Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file"))
            @RequestParam(value = "file") MultipartFile documentFile,
            @Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id
    ) throws RmesException, IOException {
        String documentName = documentFile.getOriginalFilename();
        verifyExtension(documentName);
        var url = documentsService.changeDocument(id, documentFile.getInputStream(), documentName);
        return ResponseEntity.status(HttpStatus.OK).body(url);
    }


    @DeleteMapping("/document/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    @Operation(summary = "Delete a document")
    public ResponseEntity<Object> deleteDocument(
            @Parameter(
                    required = true,
                    schema = @Schema (type=Constants.TYPE_STRING)
            )
            @PathVariable(Constants.ID) DocumentId id)
            throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        return ResponseEntity.status(documentsService.deleteDocument(documentIdString)).body(documentIdString);
    }


    @GetMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get a Link",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = Document.class)))})
    public ResponseEntity<String> getLink(@PathVariable(Constants.ID) String id) throws RmesException {

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(documentsService.getLink(id).toString());

    }

    @PostMapping(value = "/link",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    "application/vnd.oasis.opendocument.text",
                    MediaType.APPLICATION_JSON_VALUE})
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.CREATE)
    @Operation(summary = "Create link")
    public ResponseEntity<Object> setLink(
            @Parameter(description = "Link", required = true, schema = @Schema(implementation = Document.class))
            @RequestParam(value = "body") String body
    ) throws RmesException, IOException {
        return ResponseEntity.status(HttpStatus.OK).body(documentsService.setLink(body));
    }

    @PutMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.UPDATE)
    @Operation(summary = "Update link")
    public ResponseEntity<Object> setLink(
            @Parameter(
                    description = "Id",
                    required = true,
                    schema = @Schema (type=Constants.TYPE_STRING)
            )
            @PathVariable(Constants.ID) DocumentId id,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = Document.class)
            )
            @RequestBody String body
    )
            throws RmesException {
        String documentIdString = (id.getDocumentId() != null) ? sanitizeDocumentId(id.getDocumentId()) : null;
        return ResponseEntity.ok(documentsService.setLink(documentIdString, body));
    }

    @DeleteMapping("/link/{id}")
    @HasAccess(module = RBAC.Module.OPERATION_DOCUMENT, privilege = RBAC.Privilege.DELETE)
    @Operation(summary = "Delete a link")
    public ResponseEntity<Object> deleteLink(
            @Parameter(
                    required = true,
                    schema = @Schema (type=Constants.TYPE_STRING)
            )
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

    private void verifyExtension(String fileName) throws IOException, RmesException {
        InputStream input = Bauhaus.class.getClassLoader().getResourceAsStream("bauhaus-core.properties");
        properties.load(input);
        String[] extensionsExpected = properties.getProperty("fr.insee.rmes.bauhaus.extensions").split(",");
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
