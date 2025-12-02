package fr.insee.rmes.onion.infrastructure.webservice.datasets;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.port.serverside.UserDecoder;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.dataset.*;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of distributions",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public List<PartialDistribution> getDistributions() throws RmesException {
        return this.distributionService.getDistributions();
    }

    @GetMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get a distribution",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public Distribution getDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.distributionService.getDistributionByID(id);
    }

    @PutMapping(value = "/{id}/validate")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.PUBLISH)
    @Operation(summary = "Publish a distribution",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public void publishDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        this.distributionService.publishDistribution(id);
    }

    @GetMapping("/datasets")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public List<PartialDataset> getDatasetsForDistributionCreation(@AuthenticationPrincipal Object principal) throws RmesException {
        var user = userDecoder.fromPrincipal(principal).get();

        if (user.hasRole(Roles.ADMIN)) {
            return this.datasetService.getDatasets();
        }

        if(user.stamp() == null){
            return Collections.emptyList();
        }

        return this.datasetService.getDatasetsForDistributionCreation(user.stamp().stamp());
    }

    @GetMapping(value = "/search", produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(operationId = "getDistributionsForSearch", summary = "List of distributions for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public List<DistributionsForSearch> getDistributionsForSearch() throws RmesException {
        return this.distributionService.getDistributionsForSearch();
    }


    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.CREATE)
    @Operation(summary = "Create a distribution")
    @ResponseStatus(HttpStatus.CREATED)
    public String createDistribution(
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.create(body);
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.UPDATE)
    @Operation(summary = "Update a distribution")
    public String updateDistribution(
            @PathVariable("id") String id,
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.DELETE)
    @Operation(
            summary = "Delete a distribution"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The distribution has been  deleted"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to call this endpoint"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "This distribution does not exist")
    })
    public ResponseEntity<Void> deleteDistribution(
            @PathVariable(Constants.ID) String id) throws RmesException{
        distributionService.deleteDistributionId(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.UPDATE)
    @Operation(summary = "Update a distribution")
    public void patchDistribution(
            @PathVariable("id") String id,
            @RequestBody PatchDistribution distribution
    ) throws RmesException{
        this.distributionService.patchDistribution(id, distribution);
    }
}