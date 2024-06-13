package fr.insee.rmes.webservice.distribution;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.model.dataset.PatchDistribution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/distribution")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Distribution", description = "Distribution API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('datasets')")
public class DistributionResources {

    final DistributionService distributionService;
    final DatasetService datasetService;

    final UserDecoder userDecoder;

    public DistributionResources(DistributionService distributionService, DatasetService datasetService, UserDecoder userDecoder) {
        this.distributionService = distributionService;
        this.datasetService = datasetService;
        this.userDecoder = userDecoder;
    }

    @GetMapping
    @Operation(operationId = "getDistributions", summary = "List of distributions",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public String getDistributions() throws RmesException {
        return this.distributionService.getDistributions();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDistribution", summary = "List of distributions",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public String getDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.distributionService.getDistributionByID(id);
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("isAdmin() || isDistributionContributorWithStamp(#distributionId)")
    @Operation(operationId = "publishDistribution", summary = "Publish a distribution",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public String publishDistribution(@PathVariable(Constants.ID) String distributionId) throws RmesException {
        return this.distributionService.publishDistribution(distributionId);
    }

    @GetMapping("/datasets")
    @Operation(operationId = "getDatasetsForDistributionCreation", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDatasetsForDistributionCreation(@AuthenticationPrincipal Object principal) throws RmesException {
        var user = userDecoder.fromPrincipal(principal).get();

        if (user.hasRole(Roles.ADMIN)) {
            return this.datasetService.getDatasets();
        }
        return this.datasetService.getDatasetsForDistributionCreation(user.getStamp());
    }

    @PreAuthorize("isAdmin() || isDatasetContributor()")
    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDistribution", summary = "Create a distribution")
    @ResponseStatus(HttpStatus.CREATED)
    public String createDistribution(
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.create(body);
    }

    @PreAuthorize("isAdmin() || isDistributionContributorWithStamp(#distributionId)")
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDistribution", summary = "Update a distribution")
    public String updateDistribution(
            @PathVariable("id") String distributionId,
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.update(distributionId, body);
    }

    @PreAuthorize("isAdmin() || isDistributionContributorWithStamp(#distributionId)")
    @DeleteMapping("/{id}")
    @Operation(
            operationId = "deleteDistribution",
            summary = "Delete a distribution"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "You are not authorized to call this endpoint"),
            @ApiResponse(responseCode = "501", description = "This endpoint is not implemented")
    })
    public ResponseEntity deleteDistribution(
            @PathVariable(Constants.ID) String distributionId
    )throws RmesException{

        return this.distributionService.deleteDistributionId(distributionId);
    }

    @PreAuthorize("isAdmin() || isDistributionContributorWithStamp(#distributionId)")
    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "patchDistribution", summary = "Update a distribution")
    public void patchDistribution(
            @PathVariable("id") String distributionId,
            @RequestBody PatchDistribution distribution
    ) throws RmesException{
        this.distributionService.patchDistribution(distributionId, distribution);
    }
}