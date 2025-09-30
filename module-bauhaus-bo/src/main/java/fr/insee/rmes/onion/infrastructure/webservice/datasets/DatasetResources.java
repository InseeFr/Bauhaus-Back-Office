package fr.insee.rmes.onion.infrastructure.webservice.datasets;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/datasets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dataset", description = "DataSet API")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('datasets')")
public class DatasetResources {

    final DatasetService datasetService;

    public DatasetResources(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping(produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public List<PartialDataset> getDatasets() throws RmesException {
        return this.datasetService.getDatasets();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public Dataset getDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDatasetByID(id);
    }

    @GetMapping("/{id}/distributions")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @Operation(summary = "List of distributions for a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDistributionsByDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDistributions(id);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = "application/json")
    @Operation(operationId = "getDatasetsForSearch", summary = "List of datasets for advanced search",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public List<DatasetsForSearch> getDatasetsForSearch() throws RmesException {
        return this.datasetService.getDatasetsForSearch();
    }


    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.CREATE)
    @Operation(summary = "Create a dataset")
    @ResponseStatus(HttpStatus.CREATED)
    public String setDataset(
            @Parameter(description = "Dataset", required = true) @RequestBody String body) throws RmesException {
        return this.datasetService.create(body);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a dataset")
    public void setDataset(
            @PathVariable("id") String id,
            @Parameter(description = "Dataset", required = true) @RequestBody String body) throws RmesException {
        this.datasetService.update(id, body);
    }

    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping(value = "/{id}/validate")
    @Operation(summary = "Publish a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Distribution.class))))})
    public void publishDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        this.datasetService.publishDataset(id);
    }

    @GetMapping(value = "/archivageUnits", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Get all archivage units")
    public String getArchivageUnits() throws RmesException {
        return this.datasetService.getArchivageUnits();
    }

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.UPDATE)
    @Operation(summary = "Update a dataset")
    public void patchDataset(
            @PathVariable("id") String id,
            @RequestBody PatchDataset dataset
    ) throws RmesException {
        this.datasetService.patchDataset(id, dataset);
    }

    @DeleteMapping("/{id}")
    @HasAccess(module = RBAC.Module.DATASET_DATASET, privilege = RBAC.Privilege.DELETE)
    @Operation(
            summary = "Delete a dataset"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The dataset has been  deleted"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to call this endpoint"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "This dataset does not exist")
    })
    public ResponseEntity<Void> deleteDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        datasetService.deleteDatasetId(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}