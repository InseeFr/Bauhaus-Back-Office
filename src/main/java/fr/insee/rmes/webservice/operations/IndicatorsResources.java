package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.utils.XMLUtils;
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

	@GetMapping(value="/indicators", produces=MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicators", summary = "List of indicators", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public ResponseEntity<Object> getIndicators() throws RmesException {
		String jsonResultat = operationsService.getIndicators();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/indicators/withSims",produces= MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "annotations", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation= IdLabelAltLabelSims.class)))})
	public ResponseEntity<Object> getIndicatorsWIthSims() throws RmesException {
		String jsonResultat = operationsService.getIndicatorsWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/indicators/advanced-search", produces=MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorsForSearch", summary = "List of indicators for search",
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Indicator.class)))})
	public ResponseEntity<Object> getIndicatorsForSearch() throws RmesException {
		String jsonResultat = operationsService.getIndicatorsForSearch();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);

	}

	@GetMapping(value="/indicator/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorByID", summary = "Get an indicator",
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Indicator.class)))})
	public ResponseEntity<Object> getIndicatorByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true)@RequestHeader(required=false) String accept) throws RmesException {

		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			return ResponseEntity.status(HttpStatus.OK).body(XMLUtils.produceXMLResponse(operationsService.getIndicatorById(id)));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(operationsService.getIndicatorJsonByID(id));
		}
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	//TODO Test : admin then INDICATOR_CONTRIBUTOR with stamp fit then not
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
	@PutMapping(value="/indicator/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorById", summary = "Update an indicator")
	public ResponseEntity<Object> setIndicatorById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Indicator to update", required = true,
			content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) throws RmesException {

		operationsService.setIndicator(id, body);
		return ResponseEntity.noContent().build();
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
	@PutMapping(value="/indicator/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorValidation", summary = "Indicator validation")
	public ResponseEntity<Object> setIndicatorValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {

		operationsService.setIndicatorValidation(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN , T(fr.insee.rmes.config.auth.roles.Roles).INDICATOR_CONTRIBUTOR)")
	@PostMapping(value="/indicator", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicator", summary = "Create indicator",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))})
	public ResponseEntity<Object> setIndicator(
			@Parameter(description = "Indicator to create", required = true,

	content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) throws RmesException {
		String id =  operationsService.setIndicator(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
}
