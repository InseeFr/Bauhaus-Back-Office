package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Qualifier("Operation")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class OperationsResources extends OperationsCommonResources {


	/***************************************************************************************************
	 * OPERATIONS
	 ******************************************************************************************************/

	@GetMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperations", summary = "List of operations", responses = {
			@ApiResponse(content = @Content(schema = @Schema(type = "array", implementation = IdLabelAltLabel.class))) })
	public ResponseEntity<Object> getOperations() throws RmesException {
		String jsonResultat = operationsService.getOperations();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);

	}

	@GetMapping(value = "/operation/{id}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationByID", summary = "Get an operation", responses = {
			@ApiResponse(content = @Content(/* mediaType = "application/json", */ schema = @Schema(implementation = Operation.class))) })
	public ResponseEntity<Object> getOperationByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat = XMLUtils.produceXMLResponse(operationsService.getOperationById(id));
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		} else {
			try {
				resultat = operationsService.getOperationJsonByID(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultat);
	}

	/**
	 * UPDATE
	 * 
	 * @param id
	 * @param body
	 * @return
	 */
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CNIS)")
	@PutMapping(value = "/operation/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationById", summary = "Update an operation")
	public ResponseEntity<Object> setOperationById(@PathVariable(Constants.ID) String id,
			@Parameter(description = "Operation to update", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) {
		try {
			operationsService.setOperation(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * CREATE
	 * 
	 * @param body
	 * @return
	 */
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR)")
	@PostMapping(value = "/operation", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createOperation", summary = "Create operation")
	public ResponseEntity<Object> createOperation(
			@Parameter(description = "Operation to create", required = true, content = @Content(schema = @Schema(implementation = Operation.class))) @RequestBody String body) {
		String id = null;
		try {
			id = operationsService.createOperation(body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	/**
	 * PUBLISH
	 * 
	 * @param id
	 * @return response
	 */
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN " + ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR)")
	@PutMapping(value = "/operation/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationValidation", summary = "Operation validation")
	public ResponseEntity<Object> setOperationValidation(@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setOperationValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

}
