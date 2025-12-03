package fr.insee.rmes.modules.datasets.distributions.webservice;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.datasets.datasets.model.Dataset;
import fr.insee.rmes.modules.datasets.datasets.model.PartialDataset;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.datasets.distributions.model.DistributionsForSearch;
import fr.insee.rmes.modules.datasets.distributions.model.PatchDistribution;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
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
    @Operation(summary = "List of distributions")
    public ResponseEntity<List<PartialDistributionResponse>> getDistributions() throws RmesException {
        List<PartialDistributionResponse> responses = this.distributionService.getDistributions().stream()
                .map(distribution -> {
                    var response = PartialDistributionResponse.fromDomain(distribution);
                    response.add(linkTo(DistributionResources.class).slash(distribution.id()).withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaTypes.HAL_JSON)
                .body(responses);
    }

    @GetMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get a distribution")
    public Distribution getDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.distributionService.getDistributionByID(id);
    }

    @PutMapping(value = "/{id}/validate")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.PUBLISH)
    @Operation(summary = "Publish a distribution")
    public void publishDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        this.distributionService.publishDistribution(id);
    }

    @GetMapping("/datasets")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of datasets")
    public List<PartialDataset> getDatasetsForDistributionCreation(@AuthenticationPrincipal Object principal) throws RmesException, MissingUserInformationException {
        var user = userDecoder.fromPrincipal(principal).get();

        if (user.hasRole(Roles.ADMIN)) {
            return this.datasetService.getDatasets();
        }
        return this.datasetService.getDatasetsForDistributionCreation(user.getStamp());
    }

    @GetMapping(value = "/search", produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DISTRIBUTION, privilege = RBAC.Privilege.READ)
    @Operation(operationId = "getDistributionsForSearch", summary = "List of distributions for advanced search")
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