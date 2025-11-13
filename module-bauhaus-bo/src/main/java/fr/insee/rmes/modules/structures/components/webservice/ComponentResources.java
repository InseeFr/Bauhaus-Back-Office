package fr.insee.rmes.modules.structures.components.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructureComponent;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/structures/components")
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
public class ComponentResources {


    final
    StructureService structureService;

    final
    StructureComponent structureComponentService;

    public ComponentResources(StructureService structureService, StructureComponent structureComponentService) {
        this.structureService = structureService;
        this.structureComponentService = structureComponentService;
    }


    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all mutualized components for advanced search")
    public ResponseEntity<Object> getComponentsForSearch() throws RmesException {
        String components = structureComponentService.getComponentsForSearch();
        return ResponseEntity.status(HttpStatus.SC_OK).body(components);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all mutualized attributes")
    public ResponseEntity<Object> getAttributes() throws RmesException {
        String attributes = structureComponentService.getAttributes();
        return ResponseEntity.status(HttpStatus.SC_OK).body(attributes);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all mutualized components")
    public ResponseEntity<List<PartialStructureComponentResponse>> getComponents() throws RmesException {
        List<PartialStructureComponentResponse> responses = this.structureComponentService.getComponents().stream()
                .map(component -> {
                    var response = PartialStructureComponentResponse.fromDomain(component);
                    response.add(linkTo(ComponentResources.class).slash(component.id()).withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaTypes.HAL_JSON)
                .body(responses);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a component")
    public ResponseEntity<Object> getComponentById(@PathVariable(Constants.ID) String id) throws RmesException {
        String component = structureComponentService.getComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(component);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/{id}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Publish a component")
    public ResponseEntity<Object> publishComponentById(@PathVariable(Constants.ID) String id) throws RmesException {
        String result = structureComponentService.publishComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).body(result);
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a mutualized component")
    public ResponseEntity<Object> deleteComponentById(@PathVariable(Constants.ID) String id) throws RmesException {
        structureComponentService.deleteComponent(id);
        return ResponseEntity.status(HttpStatus.SC_OK).build();
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a component")
    public ResponseEntity<Object> updateComponentById(@PathVariable(Constants.ID) String id,
    		@Parameter(description = "Component", required = true) @RequestBody String body) throws RmesException {
        return ResponseEntity.status(HttpStatus.SC_OK).body(structureComponentService.updateComponent(id, body));
    }

    @HasAccess(module = RBAC.Module.STRUCTURE_COMPONENT, privilege = RBAC.Privilege.CREATE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a component")
    public ResponseEntity<Object> createComponent(
    		@Parameter(description = "Component", required = true) @RequestBody String body) throws RmesException {
        String id = structureComponentService.createComponent(body);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }
}
