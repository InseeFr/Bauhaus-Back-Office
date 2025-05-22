package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.PartialOperationIndicator;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Qualifier("Indicator")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value="/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class IndicatorsResources {
	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public IndicatorsResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getIndicators", summary = "List of indicators",
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public List<PartialOperationIndicator> getIndicators() throws RmesException {
		return operationsService.getIndicators();
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators/withSims",produces= MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "annotations", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation= IdLabelAltLabelSims.class)))})
	public ResponseEntity<Object> getIndicatorsWIthSims() throws RmesException {
		String indicators = operationsService.getIndicatorsWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(indicators);
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicators/advanced-search", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "List of indicators for search",
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Indicator.class)))})
	public ResponseEntity<Object> getIndicatorsForSearch() throws RmesException {
		String indicators = operationsService.getIndicatorsForSearch();
		return ResponseEntity.status(HttpStatus.OK).body(indicators);

	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.READ)
	@GetMapping(value="/indicator/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@Operation(summary = "Get an indicator",
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Indicator.class)))})
	public ResponseEntity<Object> getIndicatorByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true)@RequestHeader(required=false) String accept) throws RmesException {

		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			return ResponseEntity.status(HttpStatus.OK).body(XMLUtils.produceXMLResponse(operationsService.getIndicatorById(id)));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(operationsService.getIndicatorJsonByID(id));
		}
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.UPDATE)
	@PutMapping(value="/indicator/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update an indicator")
	public ResponseEntity<Object> setIndicatorById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Indicator to update", required = true,
			content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) throws RmesException {

		operationsService.setIndicator(id, body);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.PUBLISH)
	@PutMapping(value="/indicator/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Indicator validation")
	public ResponseEntity<Object> setIndicatorValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {

		operationsService.validateIndicator(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@HasAccess(module = RBAC.Module.OPERATION_INDICATOR, privilege = RBAC.Privilege.CREATE)
	@PostMapping(value="/indicator", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create indicator",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))})
	public ResponseEntity<Object> setIndicator(
			@Parameter(description = "Indicator to create", required = true,

	content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) throws RmesException {
		String id =  operationsService.setIndicator(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
