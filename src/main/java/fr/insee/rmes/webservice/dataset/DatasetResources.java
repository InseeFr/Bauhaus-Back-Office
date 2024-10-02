package fr.insee.rmes.webservice.dataset;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external.services.rbac.RBACService;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.model.dataset.PatchDataset;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.model.rbac.Privilege;
import fr.insee.rmes.model.rbac.Strategy;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static fr.insee.rmes.config.auth.RBACConfiguration.toRolesNames;
import static fr.insee.rmes.model.rbac.Strategy.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/datasets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "DataSet API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('datasets')")
public class DatasetResources {

    final DatasetService datasetService;
    private final RBACService rbacService;
    private final UserDecoder userDecoder;

    public DatasetResources(DatasetService datasetService, RBACService rbacService, UserDecoder userDecoder) {
        this.datasetService = datasetService;
        this.rbacService = rbacService;
        this.userDecoder = userDecoder;
    }

    @PreAuthorize("canReadDataset(#datasetId)")
    @GetMapping(produces = "application/json")
    @Operation(operationId = "getDatasets", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDatasets() throws RmesException {
        return this.datasetService.getDatasets();
    }

    @PreAuthorize("canReadDataset(#datasetId)")
    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(operationId = "getDataset", summary = "Get a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public Dataset getDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDatasetByID(id);
    }

    @PreAuthorize("canReadDataset(#datasetId)")
    @GetMapping("/{id}/distributions")
    @Operation(operationId = "getDistributionsByDataset", summary = "List of distributions for a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDistributionsByDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDistributions(id);
    }

    @PreAuthorize("canCreateDataset()")
    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDataset", summary = "Create a dataset")
    @ResponseStatus(HttpStatus.CREATED)
    public String createDataset(
            @Parameter(description = "Dataset", required = true) @RequestBody Dataset body,
            Object principal
    ) throws RmesException {
        User user = this.userDecoder.fromPrincipal(principal).orElseThrow(()->new RmesException(500, "User informations mandatories for this endpoint", "Unable to retrieve user from "+principal));
        if (hasStrategyAllForCreation(user)) {
            return this.datasetService.create(body);
        }
        return this.datasetService.createWithStamp(body, user.stamp());
    }

    private boolean hasStrategyAllForCreation(User user) {
        return creationStrategy(user).map(ALL::equals).orElse(false);
    }

    private Optional<Strategy> creationStrategy(User user) {
        return this.rbacService.computeRbac(toRolesNames(user.roles()))
                .privilegesForModule(Module.DATASET)
                .strategyFor(Privilege.CREATE);
    }

    @PreAuthorize("canUpdateDataset(#datasetId)")
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDataset", summary = "Update a dataset")
    public String updateDataset(
            @PathVariable("id") String datasetId,
            @Parameter(description = "Dataset", required = true) @RequestBody String body) throws RmesException {
        return this.datasetService.update(datasetId, body);
    }

    @PreAuthorize("canPublishDataset(#datasetId)")
    @PutMapping("/{id}/validate")
    @Operation(operationId = "publishDataset", summary = "Publish a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public String publishDataset(@PathVariable(Constants.ID) String datasetId) throws RmesException {
        return this.datasetService.publishDataset(datasetId);
    }

    @PreAuthorize("canReadAllDataset()")
    @GetMapping(value = "/archivageUnits", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "getArchivageUnits", summary = "Get all archivage units")
    public String getArchivageUnits() throws RmesException {
        return this.datasetService.getArchivageUnits();
    }

    @PreAuthorize("canUpdateDataset(#datasetId)")
    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "patchDataset", summary = "Update a dataset")
    public void patchDataset(
            @PathVariable("id") String datasetId,
            @RequestBody PatchDataset dataset
    ) throws RmesException {
        this.datasetService.patchDataset(datasetId, dataset);
    }

    @PreAuthorize("canDeleteDataset(#datasetId)")
    @DeleteMapping("/{id}")
    @Operation(
            operationId = "deleteDataset",
            summary = "Delete a dataset"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The dataset has been  deleted"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to call this endpoint"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "This dataset does not exist")
    })
    public ResponseEntity<Void> deleteDataset(@PathVariable(Constants.ID) String datasetId) throws RmesException {
        datasetService.deleteDatasetId(datasetId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}