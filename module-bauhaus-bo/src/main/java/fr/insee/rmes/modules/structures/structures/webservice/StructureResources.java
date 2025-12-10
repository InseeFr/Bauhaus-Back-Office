package fr.insee.rmes.modules.structures.structures.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.swagger.model.Id;
import fr.insee.rmes.modules.structures.structures.domain.model.Structure;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/structures")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('structures')")
public class StructureResources {


    final
    StructureService structureService;

    final
    StructureComponent structureComponentService;

    public StructureResources(StructureService structureService, StructureComponent structureComponentService) {
        this.structureService = structureService;
        this.structureComponentService = structureComponentService;
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PartialStructureResponse>> getStructures() throws RmesException {
        List<PartialStructureResponse> responses = this.structureService.getStructures().stream()
                .map(structure -> {
                    var response = PartialStructureResponse.fromDomain(structure);
                    response.add(linkTo(StructureResources.class).slash("structure").slash(structure.id()).withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaTypes.HAL_JSON)
                .body(responses);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getStructuresForSearch() throws RmesException {
        String structures = structureService.getStructuresForSearch();
        return ResponseEntity.status(HttpStatus.SC_OK).body(structures);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/structure/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getStructureById(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/structure/{id}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> publishStructureById(@PathVariable(Constants.ID) String id) throws RmesException {
        String response = structureService.publishStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(response);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/structure/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getStructureByIdDetails(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureByIdWithDetails(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }


    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.CREATE)
    @PostMapping(value = "/structure",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setStructure(@RequestBody String body) throws RmesException {
        String id = structureService.setStructure(body);
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/structure/{structureId}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> setStructure(
    		@PathVariable("structureId") String id,
    		@RequestBody String body) throws RmesException {
        return ResponseEntity.status(HttpStatus.SC_OK).body(structureService.setStructure(id, body));
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping("/structure/{id}")
    public ResponseEntity<Id> deleteStructure(@PathVariable("id") Id id) throws RmesException {
        structureService.deleteStructure(id.identifier());
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }
}
