package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.PartialOperationFamily;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Qualifier("Family")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(
		value = "/operations",
		produces = {
				MediaType.APPLICATION_JSON_VALUE,
				MediaType.TEXT_PLAIN_VALUE
		}
)
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class FamilyResources  {

	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public FamilyResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}


	@GetMapping("/families")
	@HasAccess(module = RBAC.Module.FAMILY, privilege =  RBAC.Privilege.READ)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilies", summary = "List of families", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public List<PartialOperationFamily> getFamilies() throws RmesException {
		return operationsService.getFamilies();
	}

	@GetMapping("/families/advanced-search")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamiliesForSearch", summary = "List of families for search",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Family.class))))})
	public ResponseEntity<Object> getFamiliesForSearch() throws RmesException {
		String families = operationsService.getFamiliesForSearch();
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(families);
	}

	@GetMapping("/families/{id}/seriesWithReport")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithReport", summary = "Series with metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation= Operation.class)))})
	public ResponseEntity<Object> getSeriesWithReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String series = operationsService.getSeriesWithReport(id);
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(series);
	}

	@GetMapping("/family/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilyByID", summary = "Get a family", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Family.class)))}
			)
	public ResponseEntity<Object> getFamilyByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String family = operationsService.getFamilyByID(id);
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(family);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PutMapping("/family/{id}")
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyById", summary = "Update an existing family" )
	public ResponseEntity<Object> setFamilyById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Family to update", required = true, content = @Content(schema = @Schema(implementation = Family.class))) @RequestBody String body) throws RmesException {
		operationsService.setFamily(id, body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PostMapping("/family")
	@io.swagger.v3.oas.annotations.Operation(operationId = "createFamily", summary = "Create a new family")
	public ResponseEntity<Object> createFamily(
			@Parameter(description = "Family to create", required = true, content = @Content(schema = @Schema(implementation = Family.class))) 
			@RequestBody String body) throws RmesException {
		String id = operationsService.createFamily(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PutMapping("/family/{id}/validate")
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyValidation", summary = "Validate a family")
	public ResponseEntity<Object> setFamilyValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setFamilyValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
