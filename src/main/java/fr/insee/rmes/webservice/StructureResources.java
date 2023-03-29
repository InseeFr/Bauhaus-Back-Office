package fr.insee.rmes.webservice;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/structures")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Data structure definitions", description = "Structure API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class StructureResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(StructureResources.class);

    @Autowired
    StructureService structureService;

    @Autowired
    StructureComponent structureComponentService;

    @GetMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructures", summary = "List of Structures",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructures() {
        String jsonResultat;
        try {
            jsonResultat = structureService.getStructures();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructuresForSearch", summary = "List of Structures for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructuresForSearch() {
        try {
            String jsonResultat = structureService.getStructuresForSearch();
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping(value = "/structure/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "getStructureById",
            summary = "Get a structure",
            responses = {
                    @ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))
            }
    )
    public ResponseEntity<Object> getStructureById(@PathVariable(Constants.ID) String id) {
        try {
            String structure = structureService.getStructureById(id);
            return ResponseEntity.status(HttpStatus.SC_OK).body(structure);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @GetMapping(value = "/structure/{id}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "publishStructureById", summary = "Publish a structure")
    public ResponseEntity<Object> publishStructureById(@PathVariable(Constants.ID) String id) {
        try {
            String response = structureService.publishStructureById(id);
            return ResponseEntity.status(HttpStatus.SC_OK).body(response);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        } 
    }

    @GetMapping(value = "/structure/{id}/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getStructureByIdDetails", summary = "Get all a details of a structure",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))})
    public ResponseEntity<Object> getStructureByIdDetails(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = structureService.getStructureByIdWithDetails(id);
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @PostMapping(value = "/structure",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setStructure", summary = "Create a structure")
    public ResponseEntity<Object> setStructure(
    		@Parameter(description = "Structure", required = true) @RequestBody String body) {
        try {
            String id = structureService.setStructure(body);
            return ResponseEntity.status(HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @PutMapping(value = "/structure/{structureId}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setStructure", summary = "Update a structure")
    public ResponseEntity<Object> setStructure(
    		@PathVariable("structureId") String structureId, 
    		@Parameter(description = "Structure", required = true) @RequestBody String body) {
        try {
            String id = structureService.setStructure(structureId, body);
            return ResponseEntity.status(HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @DeleteMapping("/structure/{structureId}")
    @Operation(operationId = "deleteStructure", summary = "Delete a structure")
    public ResponseEntity<Object> deleteStructure(@PathVariable("structureId") String structureId) {
        try {
            structureService.deleteStructure(structureId);
            return ResponseEntity.status(HttpStatus.SC_OK).body(structureId);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping(value = "/components/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponentsForSearch", summary = "Get all mutualized components for advanced search")
    public ResponseEntity<Object> getComponentsForSearch() {
        try {
            String jsonResultat = structureComponentService.getComponentsForSearch();
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping(value = "/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getAttributes", summary = "Get all mutualized attributes")
    public ResponseEntity<Object> getAttributes() {
        try {
            String jsonResultat = structureComponentService.getAttributes();
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping(value = "/components", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponents", summary = "Get all mutualized components")
    public ResponseEntity<Object> getComponents() {
        try {
            String jsonResultat = structureComponentService.getComponents();
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping(value = "/components/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getComponentById", summary = "Get all mutualized components")
    public ResponseEntity<Object> getComponentById(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = structureComponentService.getComponent(id);
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @GetMapping(value = "/components/{id}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "publishComponentById", summary = "Publish a component")
    public ResponseEntity<Object> publishComponentById(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = structureComponentService.publishComponent(id);
            return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @DeleteMapping(value = "/components/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "deleteComponentById", summary = "delete a mutualized component")
    public ResponseEntity<Object> deleteComponentById(@PathVariable(Constants.ID) String id) {
        try {
            structureComponentService.deleteComponent(id);
            return ResponseEntity.status(HttpStatus.SC_OK).build();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @PutMapping(value = "/components/{id}",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateComponent", summary = "Update a component")
    public ResponseEntity<Object> updateComponentById(@PathVariable(Constants.ID) String componentId, 
    		@Parameter(description = "Component", required = true) @RequestBody String body) {
        try {
            String id = structureComponentService.updateComponent(componentId, body);
            return ResponseEntity.status(HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @PostMapping(value = "/components",
    		consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createComponent", summary = "create a component")
    public ResponseEntity<Object> createComponent(
    		@Parameter(description = "Component", required = true) @RequestBody String body) {
        try {
            String id = structureComponentService.createComponent(body);
            return ResponseEntity.status(HttpStatus.SC_CREATED).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }
}
