package fr.insee.rmes.webservice.dataset;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.model.dataset.Distribution;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @Operation(operationId = "getDatasets", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDatasets() throws RmesException {
        return this.datasetService.getDatasets();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(operationId = "getDataset", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDatasetByID(id);
    }

    @GetMapping("/{id}/distributions")
    @Operation(operationId = "getDistributionsByDataset", summary = "List of distributions for a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public String getDistributionsByDataset(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.datasetService.getDistributions(id);
    }

    @PreAuthorize("isAdmin() || isDatasetContributor()")
    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDataset", summary = "Create a Dataset")
    @ResponseStatus(HttpStatus.CREATED)
    public String setDataset(
            @Parameter(description = "Dataset", required = true) @RequestBody String body) throws RmesException {
        return this.datasetService.create(body);
    }

    @PreAuthorize("isAdmin() || isDatasetContributorWithStamp(#datasetId)")
    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDataset", summary = "Update a Dataset")
    public String setDataset(
            @PathVariable("id") String datasetId,
            @Parameter(description = "Dataset", required = true) @RequestBody String body) throws RmesException {

        return this.datasetService.update(datasetId, body);
    }

    @PreAuthorize("isAdmin() || isDatasetContributorWithStamp(#datasetId)")
    @PutMapping("/{id}/validate")
    @Operation(operationId = "publishDataset", summary = "Publish a Dataset",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
    public String publishDataset(@PathVariable(Constants.ID) String datasetId) throws RmesException {
        return this.datasetService.publishDataset(datasetId);
    }

    @GetMapping(value = "/archivageUnits", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "getArchivageUnits", summary = "Get all archivage units")
    public String getArchivageUnits() throws RmesException {
        return this.datasetService.getArchivageUnits();
    }

    @PatchMapping(value = "/{id}/observationNumber", consumes = APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateObservationNumber", summary = "Update Observation number of a dataset")
    public void patchDataset(
            @PathVariable("id") String datasetId,
            @Schema(name ="observationNumber", example = "1" )
            @Parameter(description = "Dataset", required = true)
            @RequestBody String observationNumber
    ) throws RmesException{
        this.datasetService.patchDataset(datasetId, observationNumber);
    }

    @PreAuthorize("isAdmin() || isDatasetContributorWithStamp(#datasetId)")
    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteDataset", summary = "Delete a Dataset",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
    public String deleteDataset(@PathVariable(Constants.ID) String datasetId) throws RmesException {
        return "Not Yet Implemented";
    }
}