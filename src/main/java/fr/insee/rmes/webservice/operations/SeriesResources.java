package fr.insee.rmes.webservice.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;


@Qualifier("Series")
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/operations")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('operations')")
public class SeriesResources  {

	protected final OperationsService operationsService;

	protected final OperationsDocumentationsService documentationsService;

	public SeriesResources(OperationsService operationsService, OperationsDocumentationsService documentationsService) {
		this.operationsService = operationsService;
		this.documentationsService = documentationsService;
	}


	@GetMapping(value = "/series", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeries", summary = "List of series", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public ResponseEntity<Object> getSeries() throws RmesException {
		String series = operationsService.getSeries();
		return ResponseEntity.status(HttpStatus.OK).body(series);
	}

	@GetMapping(value = "/series/withSims", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithSims", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabelSims.class)))})
	public ResponseEntity<Object> getSeriesWIthSims() throws RmesException {
		String series = operationsService.getSeriesWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(series);
	}

	@GetMapping(value = "/series/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesByID", 
	summary = "Get a series", responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) throws RmesException {
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			return ResponseEntity.status(HttpStatus.OK).body(XMLUtils.produceXMLResponse(operationsService.getSeriesByID(id)));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(operationsService.getSeriesJsonByID(id));
		}
	}

	@GetMapping(value = "/series/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesForSearch", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesForSearch() throws RmesException {
		String series = operationsService.getSeriesForSearch();
		return ResponseEntity.status(HttpStatus.OK).body(series);
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
		String series = operationsService.getSeriesForSearchWithStamp(stamp);
		return ResponseEntity.status(HttpStatus.OK).body(series);
	}

	@PreAuthorize("isAdmin() || isContributorOfSerie(#seriesId)")
	@PutMapping(value = "/series/{id}",
		consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesById", summary = "Update a series")
	public ResponseEntity<Object> setSeriesById(
			@PathVariable(Constants.ID) @P("seriesId") String id,
			@Parameter(description = "Series to update", required = true,
			content = @Content(schema = @Schema(implementation = Series.class))) @RequestBody String body) throws RmesException {
		operationsService.setSeries(id, body);
		return ResponseEntity.ok(id);
	}

	@GetMapping(value = "/series/{id}/operationsWithoutReport", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithoutReport", summary = "Operations without metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public ResponseEntity<Object> getOperationsWithoutReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String operations  = operationsService.getOperationsWithoutReport(id);
		return ResponseEntity.status(HttpStatus.OK).body(operations);
	}

	@GetMapping(value = "/series/{id}/operationsWithReport", produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithReport", summary = "Operations with metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public ResponseEntity<Object> getOperationsWithReport(@PathVariable(Constants.ID) String id) throws RmesException {
		String operations  = operationsService.getOperationsWithReport(id);
		return ResponseEntity.status(HttpStatus.OK).body(operations);
	}


	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
	@PostMapping(value = "/series",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createSeries", summary = "Create series")
	public ResponseEntity<Object> createSeries(
			@Parameter(description = "Series to create", required = true, 
			content = @Content(schema = @Schema(implementation = Series.class))) @RequestBody String body) throws RmesException {
		String id = operationsService.createSeries(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).SERIES_CONTRIBUTOR)")
	@PutMapping(value = "/series/validate/{id}",
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesValidation", summary = "Series validation")
	public ResponseEntity<Object> setSeriesValidation(
			@PathVariable(Constants.ID) String id) throws RmesException {
		operationsService.setSeriesValidation(id);
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
		String series = operationsService.getSeriesWithStamp(stamp);
		return ResponseEntity.status(HttpStatus.OK).body(series);
	}
	
}
