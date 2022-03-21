package fr.insee.rmes.webservice.operations;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Qualifier("Series")
@RestController
@RequestMapping("/operations")
public class SeriesResources extends OperationsCommonResources {

	
	/***************************************************************************************************
	 * SERIES
	 ******************************************************************************************************/
	@GetMapping("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeries", summary = "List of series", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public ResponseEntity<Object> getSeries() throws RmesException {
		String jsonResultat = operationsService.getSeries();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping("/series/withSims")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithSims", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabelSims.class)))})
	public ResponseEntity<Object> getSeriesWIthSims() throws RmesException {
		String jsonResultat = operationsService.getSeriesWithSims();
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping("/series/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesByID", 
	summary = "Series", responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesByID(@PathVariable(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
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

	@GetMapping("/series/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
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
	@GetMapping("/series/advanced-search/{stamp}")	
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesForSearchWithStamps", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public ResponseEntity<Object> getSeriesForSearchWithStamps(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathVariable(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesForSearchWithStamp(stamp);	
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_CNIS })
	@PutMapping("/series/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesById", summary = "Update series")
	public ResponseEntity<Object> setSeriesById(
			@PathVariable(Constants.ID) String id, 
			@RequestBody(description = "Series to update", required = true,
			content = @Content(schema = @Schema(implementation = Series.class)))String body) {
		try {
			operationsService.setSeries(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.ok(id);
	}

	@GetMapping("/series/{id}/operationsWithoutReport")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithoutReport", summary = "Operations without metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public ResponseEntity<Object> getOperationsWithoutReport(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getOperationsWithoutReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping("/series/{id}/operationsWithReport")
	@Produces(MediaType.APPLICATION_JSON)
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
	@Secured({ Roles.SPRING_ADMIN })
	@PostMapping("/series")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createSeries", summary = "Create series")
	public ResponseEntity<Object> createSeries(
			@RequestBody(description = "Series to create", required = true, 
			content = @Content(schema = @Schema(implementation = Series.class))) String body) {
		String id = null;
		try {
			id = operationsService.createSeries(body);
		} catch (RmesException e) {
				return returnRmesException(e)	;	
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR })
	@PutMapping("/series/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
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
	@GetMapping("/series/seriesWithStamp/{stamp}")
	@Produces(MediaType.APPLICATION_JSON)
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
