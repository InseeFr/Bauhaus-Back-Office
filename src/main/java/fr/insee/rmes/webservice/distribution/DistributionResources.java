package fr.insee.rmes.webservice.distribution;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distribution")
@SecurityRequirement(name = "bearerAuth")
@Tag(name= "Distribution", description="Distribution API")
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
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
    public String getDistributions() throws RmesException {
        return this.distributionService.getDistributions();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getDistribution", summary = "List of distributions",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Distribution.class))))})
    public String getDistribution(@PathVariable(Constants.ID) String id) throws RmesException {
        return this.distributionService.getDistributionByID(id);
    }

    @GetMapping("/datasets")
    @Operation(operationId = "getDatasetsForDistributionCreation", summary = "List of datasets",
            responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= Dataset.class))))})
    public String getDatasetsForDistributionCreation(@AuthenticationPrincipal Object principal) throws RmesException {
        var user = userDecoder.fromPrincipal(principal).get();

        if(user.hasRole(Roles.ADMIN)){
            return this.datasetService.getDatasets();
        }
        return this.datasetService.getDatasetsForDistributionCreation(user.getStamp());
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createDistribution", summary = "Create a Distribution")
    @ResponseStatus(HttpStatus.CREATED)
    public String createDistribution(
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.create(body);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateDistribution", summary = "Update a Distribution")
    public String updateDistribution(
            @PathVariable("id") String distributionId,
            @Parameter(description = "Distribution", required = true) @RequestBody String body) throws RmesException {
        return this.distributionService.update(distributionId, body);
    }
}