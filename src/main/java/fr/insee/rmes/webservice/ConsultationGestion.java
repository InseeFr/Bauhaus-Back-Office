package fr.insee.rmes.webservice;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.consutation_gestion.ConsultationGestionService;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/consultation-gestion")
@Tag(name = "Consultation Gestion", description = "Consultation Gestion API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class ConsultationGestion extends GenericResources  {

    @Autowired
    ConsultationGestionService consultationGestionService;


    @GetMapping("/concept/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getDetailedConcept", summary = "Get a concept")
    public ResponseEntity<Object> getDetailedConcept(@PathVariable(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getDetailedConcept(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/concepts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllConcepts", summary = "Get all concepts")
    public ResponseEntity<Object> getAllConcepts() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllConcepts();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/structures")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllStructures", summary = "Get all structures")
    public ResponseEntity<Object> getAllStructures() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllStructures();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/composants")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllComponents", summary = "Get all components")
    public ResponseEntity<Object> getAllComponents() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllComponents();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/composant/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getComponentById", summary = "Get a component")
    public ResponseEntity<Object> getComponentById(@PathVariable(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getComponent(id).toString();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/structure/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getStructure", summary = "Get a structure")
    public ResponseEntity<Object> getStructure(@PathVariable(Constants.ID) String id) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getStructure(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/listesCodes")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllCodesLists", summary = "Get all codes lists")
    public ResponseEntity<Object> getAllCodesLists() {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getAllCodesLists();
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

    @GetMapping("/listeCode/{notation}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getCodesList", summary = "Get one codes list")
    public ResponseEntity<Object> getCodesList(@PathVariable(Constants.NOTATION) String notation) {
        String jsonResultat;
        try {
            jsonResultat = consultationGestionService.getCodesList(notation);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
        return ResponseEntity.status(HttpStatus.SC_OK).body(jsonResultat);
    }

}
