package fr.insee.rmes.webservice.operations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@Qualifier("Indicator")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(value="/operations")
public class IndicatorsResources extends OperationsCommonResources {

	
	/***************************************************************************************************
	 * INDICATORS
	 ******************************************************************************************************/
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
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorByID", summary = "Indicator", 
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Indicator.class)))})
	public ResponseEntity<Object> getIndicatorByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true)@RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat=XMLUtils.produceXMLResponse(operationsService.getIndicatorById(id));
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		} else {
			try {
				resultat = operationsService.getIndicatorJsonByID(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultat);
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() || @AuthorizeMethodDecider.isIndicatorContributor()")
	@PutMapping(value="/indicator/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorById", summary = "Update indicator")
	public ResponseEntity<Object> setIndicatorById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Indicator to update", required = true,
			content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) {
		try {
			operationsService.setIndicator(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() || @AuthorizeMethodDecider.isIndicatorContributor()")
	@PutMapping(value="/indicator/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorValidation", summary = "Indicator validation")
	public ResponseEntity<Object> setIndicatorValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setIndicatorValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() || @AuthorizeMethodDecider.isIndicatorContributor()")
	@PostMapping(value="/indicator", consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicator", summary = "Create indicator",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))})
	public ResponseEntity<Object> setIndicator(
			@Parameter(description = "Indicator to create", required = true,
	content = @Content(schema = @Schema(implementation = Indicator.class))) @RequestBody String body) {
		logger.info("POST indicator");
		String id = null;
		try {
			id = operationsService.setIndicator(body); 
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		if (id == null) {return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(id);}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


}
