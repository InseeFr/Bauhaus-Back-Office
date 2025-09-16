package fr.insee.rmes.onion.infrastructure.webservice.structures;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.config.swagger.model.Id;
import fr.insee.rmes.config.swagger.model.structure.StructureById;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.structures.PartialStructure;
import fr.insee.rmes.model.structures.Structure;
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
import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/structures")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Data structure definitions", description = "Structure API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('structures')")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
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
    @Operation(summary = "List of structures",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public List<PartialStructure> getStructures() throws RmesException {
        return structureService.getStructures();
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of structures for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructuresForSearch() throws RmesException {
        String structures = structureService.getStructuresForSearch();
        return ResponseEntity.status(HttpStatus.SC_OK).body(structures);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/structure/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get a structure",
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))
            }
    )
    public ResponseEntity<Object> getStructureById(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/structure/{id}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publish a structure")
    public ResponseEntity<Object> publishStructureById(@PathVariable(Constants.ID) String id) throws RmesException {
        String response = structureService.publishStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(response);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/structure/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all details of a structure",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))})
    public ResponseEntity<Object> getStructureByIdDetails(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureByIdWithDetails(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }


    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.CREATE)
    @PostMapping(value = "/structure",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a structure")
    public ResponseEntity<Object> setStructure(
    		@Parameter(description = "Structure", required = true) @RequestBody String body) throws RmesException {
        String id = structureService.setStructure(body);
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/structure/{structureId}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a structure")
    public ResponseEntity<Object> setStructure(
    		@PathVariable("structureId") String id,
    		@Parameter(description = "Structure", required = true) @RequestBody String body) throws RmesException {
        return ResponseEntity.status(HttpStatus.SC_OK).body(structureService.setStructure(id, body));
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_STRUCTURE, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping("/structure/{id}")
    @Operation(summary = "Delete a structure")
    public ResponseEntity<Id> deleteStructure(@PathVariable("id") Id id) throws RmesException {
        structureService.deleteStructure(id.identifier());
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }
}
