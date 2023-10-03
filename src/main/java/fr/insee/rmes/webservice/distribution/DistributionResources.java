package fr.insee.rmes.webservice.distribution;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/distribution")
@SecurityRequirement(name = "bearerAuth")
@Tag(name= Constants.DOCUMENT, description="Distribution API")
public class DistributionResources {
    
    @Autowired
    DistributionService distributionService;

    /**
     * TODO: change implementation Swagger metadata
     * @return
     */
    @GetMapping
    @Operation(operationId = "getDistributions", summary = "List of distributions",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Document.class))))})
    public ResponseEntity<Object> getDistributions() {
        try {
            String jsonResultat = this.distributionService.getDistributions();
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }

    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDistribution", summary = "List of distributions",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Document.class))))})
    public ResponseEntity<Object> getDistribution(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = this.distributionService.getDistributionByID(id);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }
}
