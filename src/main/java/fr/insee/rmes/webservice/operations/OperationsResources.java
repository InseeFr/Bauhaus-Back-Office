package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.PartialOperation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Qualifier("Operation")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class OperationsResources  {

	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public OperationsResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}


	@GetMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperations", summary = "List of operations", responses = {
			@ApiResponse(content = @Content(schema = @Schema(type = "array", implementation = IdLabelAltLabel.class))) })
	public List<PartialOperation> getOperations() throws RmesException {
		return operationsService.getOperations();
	}

	@GetMapping(
			value = "/operation/{id}",
			produces = {
				MediaType.APPLICATION_JSON_VALUE,
				MediaType.APPLICATION_XML_VALUE
			}
	)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationByID", summary = "Get an operation", responses = {
			@ApiResponse(content = @Content(schema = @Schema(implementation = Operation.class))) })
	public ResponseEntity<Operation> getOperationByID(@PathVariable(Constants.ID) String id) throws RmesException {
		return ResponseEntity.status(HttpStatus.OK).body(operationsService.getOperationById(id));
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CNIS)")
	@PutMapping(value = "/operation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationById", summary = "Update an operation")
	public ResponseEntity<Void> setOperationById(@PathVariable(Constants.ID) String id,
			@Parameter(description = "Operation to update", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) throws RmesException {

		operationsService.setOperation(id, body);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR)")
	@PostMapping(value = "/operation", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createOperation", summary = "Create operation")
	public ResponseEntity<String> createOperation(
			@Parameter(description = "Operation to create", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) throws RmesException {
		String id = operationsService.createOperation(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR)")
	@PutMapping(value = "/operation/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationValidation", summary = "Operation validation")
	public ResponseEntity<String> setOperationValidation(@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setOperationValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

}
