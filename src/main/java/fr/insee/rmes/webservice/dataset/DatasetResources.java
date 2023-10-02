package fr.insee.rmes.webservice.dataset;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
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
@RequestMapping("/datasets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name= Constants.DOCUMENT, description="DataSet API")
public class DatasetResources {

    @Autowired
    DatasetService datasetService;

    /**
     * TODO: change implementation Swagger metadata
     * @return
     */
    @GetMapping
    @Operation(operationId = "getDatasets", summary = "List of datasets",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Document.class))))})
    public ResponseEntity<Object> getDatasets() {
        try {
            String jsonResultat = this.datasetService.getDatasets();
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }

    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDataset", summary = "List of datasets",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Document.class))))})
    public ResponseEntity<Object> getDataset(@PathVariable(Constants.ID) String id) {
        try {
            String jsonResultat = this.datasetService.getDatasetByID(id);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
        } catch (RmesException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getDetails());
        }
    }
}
