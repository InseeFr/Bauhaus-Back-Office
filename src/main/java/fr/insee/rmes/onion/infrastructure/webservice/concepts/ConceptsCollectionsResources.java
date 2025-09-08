package fr.insee.rmes.onion.infrastructure.webservice.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.PartialCollection;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/concepts-collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ConceptsCollections", description = "Concept Collections API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('concepts')")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class ConceptsCollectionsResources {

    @Autowired
    public ConceptsCollectionsResources(ConceptsService conceptsService, ConceptsCollectionService conceptsCollectionService) {
        this.conceptsService = conceptsService;
        this.conceptsCollectionService = conceptsCollectionService;
    }

    public enum Language {
        lg1, lg2
    }


    private final ConceptsService conceptsService;


    private final ConceptsCollectionService conceptsCollectionService;

    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/collections", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of collections",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = IdLabel.class))))})
    public List<PartialCollection> getCollections() throws RmesException {
        return conceptsCollectionService.getCollections();
    }

    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/export/{id}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text"})
    @Operation(summary = "Blob of collection")
    public ResponseEntity<?> getCollectionExport(@PathVariable(Constants.ID) String id, @RequestHeader(required = false) String accept) throws RmesException {
        return conceptsService.getCollectionExport(id, accept);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/export-zip/{id}/{type}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip"})
    @Operation(summary = "Blob of concept")
    public void exportZipCollection(
            @PathVariable(Constants.ID) String id,
            @PathVariable("type") String type,
            @RequestParam("langue") Language lg,
            @RequestHeader(required = false) String accept,
            @RequestParam("withConcepts") boolean withConcepts,
            HttpServletResponse response) throws RmesException {
        conceptsCollectionService.exportZipCollection(id, accept, response, lg, type, withConcepts);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/export/{id}/{type}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text"})
    @Operation(summary = "Blob of collection")
    public ResponseEntity<?> getCollectionExport(
            @PathVariable(Constants.ID) String id,
            @PathVariable("type") String type,
            @RequestParam("langue") Language lg,
            @RequestParam("withConcepts") boolean withConcepts,
            @RequestHeader(required = false) String accept,
            HttpServletResponse response)
            throws RmesException {

        if ("ods".equalsIgnoreCase(type)) {
            return conceptsCollectionService.getCollectionExportODS(id, accept, withConcepts, response);
        }
        return conceptsCollectionService.getCollectionExportODT(id, accept, lg, withConcepts, response);

    }
}
