package fr.insee.rmes.webservice.dataset;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/datasets")
//@SecurityRequirement(name = "bearerAuth")
@Tag(name = Constants.DOCUMENT, description = "DataSet API")
public class DatasetResources {

    @Autowired
    DatasetService datasetService;

    @GetMapping
    @Operation(operationId = "getDatasets", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public ResponseEntity<Object> getDatasets() {
        try {
            String datasets = this.datasetService.getDatasets();
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(datasets);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }

    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDataset", summary = "List of datasets",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public ResponseEntity<Object> getDataset(@PathVariable(Constants.ID) String id) {
        try {
            String dataset = this.datasetService.getDatasetByID(id);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(dataset);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @GetMapping("/{id}/distributions")
    @Operation(operationId = "getDistributionsByDataset", summary = "List of distributions for a dataset",
            responses = {@ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Dataset.class))))})
    public ResponseEntity<Object> getDistributionsByDataset(@PathVariable(Constants.ID) String id) {
        try {
            String distributions = this.datasetService.getDistributions(id);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(distributions);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PostMapping(value = "/{id}/distributions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addDistributionsForDataset", summary = "Add distributions for a dataset")
    public ResponseEntity<Object> createDataset(
            @PathVariable(Constants.ID) String datasetId,
            @Parameter(description = "Distributions", required = true) @RequestBody String body) {
        try {
            String id = this.datasetService.addDistributions(datasetId, body);
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDataset", summary = "Create a Dataset")
    public ResponseEntity<Object> setDataset(
            @Parameter(description = "Dataset", required = true) @RequestBody String body) {
        try {
            String id = this.datasetService.create(body);
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDataset", summary = "Update a Dataset")
    public ResponseEntity<Object> setDataset(
            @PathVariable("id") String datasetId,
            @Parameter(description = "Dataset", required = true) @RequestBody String body) {
        try {
            String id = this.datasetService.update(datasetId, body);
            return ResponseEntity.status(org.apache.http.HttpStatus.SC_OK).body(id);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }
}
