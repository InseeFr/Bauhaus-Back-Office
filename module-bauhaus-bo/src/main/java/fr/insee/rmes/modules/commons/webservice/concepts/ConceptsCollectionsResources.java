package fr.insee.rmes.modules.commons.webservice.concepts;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Language;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/concepts-collections")
@ConditionalOnModule("concepts")
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
    public ResponseEntity<?> getCollectionExport(@PathVariable(Constants.ID) String id, @RequestHeader(required = false) String accept) throws RmesException {
        return conceptsService.getCollectionExport(id, accept);
    }

    @HasAccess(module = RBAC.Module.CONCEPT_COLLECTION, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/export-zip/{id}/{type}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip"})
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
