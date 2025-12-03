package fr.insee.rmes.modules.commons.webservice.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fr.insee.rmes.domain.model.Language;

@RestController
@RequestMapping("/concepts-collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ConceptsCollections", description = "Concept Collections API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('concepts')")
public class ConceptsCollectionsResources {

    @Autowired
    public ConceptsCollectionsResources(ConceptsService conceptsService, ConceptsCollectionService conceptsCollectionService) {
        this.conceptsService = conceptsService;
        this.conceptsCollectionService = conceptsCollectionService;
    }

    private final ConceptsService conceptsService;


    private final ConceptsCollectionService conceptsCollectionService;


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
