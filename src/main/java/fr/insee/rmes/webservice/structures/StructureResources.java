package fr.insee.rmes.webservice.structures;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.config.swagger.model.structure.StructureById;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.Structure;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructures", summary = "List of structures",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructures() throws RmesException {
        String structures = structureService.getStructures();
        return ResponseEntity.status(HttpStatus.SC_OK).body(structures);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructuresForSearch", summary = "List of structures for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructuresForSearch() throws RmesException {
        String structures = structureService.getStructuresForSearch();
        return ResponseEntity.status(HttpStatus.SC_OK).body(structures);
    }

    @GetMapping(value = "/structure/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "getStructureById",
            summary = "Get a structure",
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))
            }
    )
    public ResponseEntity<Object> getStructureById(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }

    @PreAuthorize("isAdmin() || isStructureContributor(#id)")
    @PutMapping(value = "/structure/{id}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "publishStructureById", summary = "Publish a structure")
    public ResponseEntity<Object> publishStructureById(@PathVariable(Constants.ID) @P("id") String id) throws RmesException {
        String response = structureService.publishStructureById(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(response);
    }

    @GetMapping(value = "/structure/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructureByIdDetails", summary = "Get all details of a structure",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))})
    public ResponseEntity<Object> getStructureByIdDetails(@PathVariable(Constants.ID) String id) throws RmesException {
        String structure = structureService.getStructureByIdWithDetails(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
    }

    @PreAuthorize("isAdmin() || isStructureAndComponentContributor(#body)")
    @PostMapping(value = "/structure",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setStructure", summary = "Create a structure")
    public ResponseEntity<Object> setStructure(
    		@Parameter(description = "Structure", required = true) @RequestBody String body) throws RmesException {
        String id = structureService.setStructure(body);
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @PreAuthorize("isAdmin() || isStructureContributor(#structureId)")
    @PutMapping(value = "/structure/{structureId}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setStructure", summary = "Update a structure")
    public ResponseEntity<Object> setStructure(
    		@PathVariable("structureId") @P("structureId") String structureId,
    		@Parameter(description = "Structure", required = true) @RequestBody String body) throws RmesException {
        String id = structureService.setStructure(structureId, body);
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @PreAuthorize("isAdmin() || isStructureContributor(#structureId)")
    @DeleteMapping("/structure/{structureId}")
    @Operation(operationId = "deleteStructure", summary = "Delete a structure")
    public ResponseEntity<Object> deleteStructure(@PathVariable("structureId") @P("structureId")  String structureId) throws RmesException {
        structureService.deleteStructure(structureId);
        String safeSId = StringEscapeUtils.escapeHtml4(structureId); // Échappe les caractères spéciaux
        return ResponseEntity.status(HttpStatus.SC_OK)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(safeSId);
    }

    @GetMapping(value = "/components/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponentsForSearch", summary = "Get all mutualized components for advanced search")
    public ResponseEntity<Object> getComponentsForSearch() throws RmesException {
        String components = structureComponentService.getComponentsForSearch();
        return ResponseEntity.status(HttpStatus.SC_OK).body(components);
    }

    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getAttributes", summary = "Get all mutualized attributes")
    public ResponseEntity<Object> getAttributes() throws RmesException {
        String attributes = structureComponentService.getAttributes();
        return ResponseEntity.status(HttpStatus.SC_OK).body(attributes);
    }

    @GetMapping(value = "/components", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponents", summary = "Get all mutualized components")
    public ResponseEntity<Object> getComponents() throws RmesException {
        String components = structureComponentService.getComponents();
        return ResponseEntity.status(HttpStatus.SC_OK).body(components);
    }

    @GetMapping(value = "/components/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponentById", summary = "Get a component")
    public ResponseEntity<Object> getComponentById(@PathVariable(Constants.ID) String id) throws RmesException {
        String component = structureComponentService.getComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(component);
    }

    @PreAuthorize("isAdmin() || isStructureContributor(#structureId)")
    @PutMapping(value = "/components/{id}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "publishComponentById", summary = "Publish a component")
    public ResponseEntity<Object> publishComponentById(@PathVariable(Constants.ID) @P("structureId") String id) throws RmesException {
        String result = structureComponentService.publishComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(result);
    }

    @PreAuthorize("isAdmin() || isComponentContributor(#id)")
    @DeleteMapping(value = "/components/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "deleteComponentById", summary = "Delete a mutualized component")
    public ResponseEntity<Object> deleteComponentById(@PathVariable(Constants.ID) @P("id") String id) throws RmesException {
        structureComponentService.deleteComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).build();
    }

    @PreAuthorize("isAdmin() || isComponentContributor(#componentId)")
    @PutMapping(value = "/components/{id}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateComponent", summary = "Update a component")
    public ResponseEntity<Object> updateComponentById(@PathVariable(Constants.ID) @P("componentId") String componentId,
    		@Parameter(description = "Component", required = true) @RequestBody String body) throws RmesException {
        String id = structureComponentService.updateComponent(componentId, body);
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @PreAuthorize("isAdmin() || isStructureAndComponentContributor(#body)")
    @PostMapping(value = "/components",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createComponent", summary = "Create a component")
    public ResponseEntity<Object> createComponent(
    		@Parameter(description = "Component", required = true) @RequestBody String body) throws RmesException {
        String id = structureComponentService.createComponent(body);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(id);
    }
}
