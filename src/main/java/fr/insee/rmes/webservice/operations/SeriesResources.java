package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
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


@Qualifier("Series")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class SeriesResources extends OperationsCommonResources {

	
	/***************************************************************************************************
	 * SERIES
	 ******************************************************************************************************/
	@GetMapping(value = "/series", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeries", summary = "List of series", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public ResponseEntity<Object> getSeries() throws RmesException {
		String jsonResultat = operationsService.getSeries();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/series/withSims", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithSims", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabelSims.class)))})
	public ResponseEntity<Object> getSeriesWIthSims() throws RmesException {
		String jsonResultat = operationsService.getSeriesWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/series/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesByID", 
	summary = "Series", responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat=XMLUtils.produceXMLResponse(operationsService.getSeriesByID(id));
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		} else {
			try {
				resultat = operationsService.getSeriesJsonByID(id);
			} catch (RmesException e) {
				return returnRmesException(e);
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultat);
	}

	@GetMapping(value = "/series/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesForSearch", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesForSearch() {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getSeriesForSearch();
		} catch (RmesException e) {
			return returnRmesException(e);
			}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
	/**
	 * Get series where stamp is the creator
	 * If only id, label, altlabel are needed, prefere /series/seriesWithStamp/{stamp}
	 * @param stamp
	 * @return 
	 * @throws RmesException
	 */
	@GetMapping(value = "/series/advanced-search/{stamp}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesForSearchWithStamps", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesForSearchWithStamps(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathVariable(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesForSearchWithStamp(stamp);	
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() "
			+ "|| @AuthorizeMethodDecider.isCnis()")
	@PutMapping(value = "/series/{id}",
		consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesById", summary = "Update series")
	public ResponseEntity<Object> setSeriesById(
			@PathVariable(Constants.ID) String id, 
			@Parameter(description = "Series to update", required = true,
			content = @Content(schema = @Schema(implementation = Series.class))) @RequestBody String body) {
		try {
			operationsService.setSeries(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.ok(id);
	}

	@GetMapping(value = "/series/{id}/operationsWithoutReport", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithoutReport", summary = "Operations without metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public ResponseEntity<Object> getOperationsWithoutReport(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getOperationsWithoutReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/series/{id}/operationsWithReport", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithReport", summary = "Operations with metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public ResponseEntity<Object> getOperationsWithReport(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getOperationsWithReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}



	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")
	@PostMapping(value = "/series",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createSeries", summary = "Create series")
	public ResponseEntity<Object> createSeries(
			@Parameter(description = "Series to create", required = true, 
			content = @Content(schema = @Schema(implementation = Series.class))) @RequestBody String body) {
		try {
			String id = operationsService.createSeries(body);
			return ResponseEntity.status(HttpStatus.OK).body(id);
		} catch (RmesException e) {
				return returnRmesException(e)	;	
		}
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PutMapping(value = "/series/validate/{id}",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesValidation", summary = "Series validation")
	public ResponseEntity<Object> setSeriesValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setSeriesValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}



	/**
	 * Get series where stamp is the creator
	 * @param stamp
	 * @return id / label / altLabel
	 * @throws RmesException
	 */
	@GetMapping(value = "/series/seriesWithStamp/{stamp}", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "seriesWithStamp", summary = "Series with given stamp as creator")
	public ResponseEntity<Object> getSeriesWithStamp(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathVariable(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesWithStamp(stamp);	
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}
	
}
