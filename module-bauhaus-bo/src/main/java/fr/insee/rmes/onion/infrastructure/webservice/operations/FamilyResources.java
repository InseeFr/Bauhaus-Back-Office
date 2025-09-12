package fr.insee.rmes.onion.infrastructure.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.model.operations.Operation;
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
	protected final FamilyService familyService;
	protected final OperationsDocumentationsService documentationsService;

	public FamilyResources(OperationsService operationsService, FamilyService familyService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
        this.familyService = familyService;
        this.documentationsService = documentationsService;
	}


	@GetMapping("/families")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	@io.swagger.v3.oas.annotations.Operation(summary = "List of families",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public List<PartialOperationFamily> getFamilies() throws RmesException {
		return familyService.getFamilies();
	}

	@GetMapping("/family/{id}")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	@io.swagger.v3.oas.annotations.Operation(summary = "Get a family",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Family.class)))}
	)
	public OperationFamily getFamilyByID(@PathVariable(Constants.ID) String id) throws RmesException {
		return familyService.getFamily(id);
	}

	@GetMapping("/families/advanced-search")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	@io.swagger.v3.oas.annotations.Operation(summary = "List of families for search",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Family.class))))})
	public ResponseEntity<Object> getFamiliesForSearch() throws RmesException {
		String families = operationsService.getFamiliesForSearch();
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(families);
	}

	@GetMapping("/families/{id}/seriesWithReport")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.READ)
	@io.swagger.v3.oas.annotations.Operation(summary = "Series with metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation= Operation.class)))})
	public ResponseEntity<Object> getSeriesWithReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String series = operationsService.getSeriesWithReport(id);
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(series);
	}


	@PutMapping("/family/{id}")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.UPDATE)
	@io.swagger.v3.oas.annotations.Operation(summary = "Update an existing family" )
	public ResponseEntity<Object> setFamilyById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Family to update", required = true, content = @Content(schema = @Schema(implementation = Family.class))) @RequestBody String body) throws RmesException {
		operationsService.setFamily(id, body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@PostMapping("/family")
	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.CREATE)
	@io.swagger.v3.oas.annotations.Operation(summary = "Create a new family")
	public ResponseEntity<Object> createFamily(
			@Parameter(description = "Family to create", required = true, content = @Content(schema = @Schema(implementation = Family.class))) 
			@RequestBody String body) throws RmesException {
		String id = operationsService.createFamily(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_FAMILY, privilege =  RBAC.Privilege.PUBLISH)
	@PutMapping("/family/{id}/validate")
	@io.swagger.v3.oas.annotations.Operation(summary = "Validate a family")
	public ResponseEntity<Object> setFamilyValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setFamilyValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
