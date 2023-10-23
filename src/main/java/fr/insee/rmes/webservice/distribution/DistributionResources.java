package fr.insee.rmes.webservice.distribution;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Distribution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distribution")
@SecurityRequirement(name = "bearerAuth")
@Tag(name= Constants.DOCUMENT, description="Distribution API")
public class DistributionResources {
    
    final DistributionService distributionService;

    public DistributionResources(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    @GetMapping
    @Operation(operationId = "getDistributions", summary = "List of distributions",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
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
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
    public ResponseEntity<Object> getDistribution(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = this.distributionService.getDistributionByID(id);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDistribution", summary = "Create a Distribution")
    public ResponseEntity<Object> createDistribution(
            @Parameter(description = "Distribution", required = true) @RequestBody String body) {
        try {
            String id = this.distributionService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDistribution", summary = "Update a Distribution")
    public ResponseEntity<Object> updateDistribution(
            @PathVariable("id") String distributionId,
            @Parameter(description = "Distribution", required = true) @RequestBody String body) {
        try {
            String id = this.distributionService.update(distributionId, body);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }
}
