package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/structures")
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


    @Produces(MediaType.APPLICATION_JSON)
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

    @GetMapping("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStructuresForSearch", summary = "List of Structures for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Structure.class))))})
    public ResponseEntity<Object> getStructuresForSearch() {
        String jsonResultat;
        try {
            jsonResultat = structureService.getStructuresForSearch();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/structure/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStructureById", summary = "Get a structure",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))})
    public ResponseEntity<Object> getStructureById(@PathVariable(Constants.ID) String id) {
        String jsonResultat = null;
        try {
            jsonResultat = structureService.getStructureById(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/structure/{id}/publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "publishStructureById", summary = "Publish a structure")
    public ResponseEntity<Object> publishStructureById(@PathVariable(Constants.ID) String id) {
        String jsonResultat = null;
        try {
            jsonResultat = structureService.publishStructureById(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        } 
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/structure/{id}/details")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStructureByIdDetails", summary = "Get all a details of a structure",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = StructureById.class)))})
    public ResponseEntity<Object> getStructureByIdDetails(@PathVariable(Constants.ID) String id) {
        String jsonResultat = null;
        try {
            jsonResultat = structureService.getStructureByIdWithDetails(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @PostMapping("/structure")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "setStructure", summary = "Create a structure")
    public ResponseEntity<Object> setStructure(
    		@Parameter(description = "Structure", required = true) @RequestBody String body) {
        String id = null;
        try {
            id = structureService.setStructure(body);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @PutMapping("/structure/{structureId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "setStructure", summary = "Update a structure")
    public ResponseEntity<Object> setStructure(
    		@PathVariable("structureId") String structureId, 
    		@Parameter(description = "Structure", required = true) @RequestBody String body) {
        String id = null;
        try {
            id = structureService.setStructure(structureId, body);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @DeleteMapping("/structure/{structureId}")
    @Operation(operationId = "deleteStructure", summary = "Delete a structure")
    public ResponseEntity<Object> deleteStructure(@PathVariable("structureId") String structureId) {
        try {
            structureService.deleteStructure(structureId);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(structureId);
    }

    @GetMapping("/components/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getComponentsForSearch", summary = "Get all mutualized components for advanced search")
    public ResponseEntity<Object> getComponentsForSearch() {
        String jsonResultat;
        try {
            jsonResultat = structureComponentService.getComponentsForSearch();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/components")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getComponents", summary = "Get all mutualized components")
    public ResponseEntity<Object> getComponents() {
        String jsonResultat;
        try {
            jsonResultat = structureComponentService.getComponents();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/components/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getComponentById", summary = "Get all mutualized components")
    public ResponseEntity<Object> getComponentById(@PathVariable(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = structureComponentService.getComponent(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/components/{id}/publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "publishComponentById", summary = "Publish a component")
    public ResponseEntity<Object> publishComponentById(@PathVariable(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = structureComponentService.publishComponent(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @DeleteMapping("/components/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "deleteComponentById", summary = "delete a mutualized component")
    public ResponseEntity<Object> deleteComponentById(@PathVariable(Constants.ID) String id) {
        try {
            structureComponentService.deleteComponent(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).build();
    }
    
    @PutMapping("/components/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "updateComponent", summary = "Update a component")
    public ResponseEntity<Object> updateComponentById(@PathVariable(Constants.ID) String componentId, 
    		@Parameter(description = "Component", required = true) @RequestBody String body) {
        String id = null;
        try {
            id = structureComponentService.updateComponent(componentId, body);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(id);
    }

    @PostMapping("/components")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "createComponent", summary = "create a component")
    public ResponseEntity<Object> createComponent(
    		@Parameter(description = "Component", required = true) @RequestBody String body) {
        String id = null;
        try {
            id = structureComponentService.createComponent(body);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(id);
    }
}
