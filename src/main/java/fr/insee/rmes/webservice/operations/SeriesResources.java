package fr.insee.rmes.webservice.operations;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsAbstResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Component
@Path("/operations")
public class SeriesResources extends OperationsAbstResources {

	
	/***************************************************************************************************
	 * SERIES
	 ******************************************************************************************************/
	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeries", summary = "List of series", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getSeries() throws RmesException {
		String jsonResultat = operationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series/withSims")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesWithSims", summary = "List of series with related sims", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabelSims.class)))})
	public Response getSeriesWIthSims() throws RmesException {
		String jsonResultat = operationsService.getSeriesWithSims();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesByID", 
	summary = "Series", responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Series.class)))})
	public Response getSeriesByID(@PathParam(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				resultat=XMLUtils.produceXMLResponse(operationsService.getSeriesByID(id));
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		} else {
			try {
				resultat = operationsService.getSeriesJsonByID(id);
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(resultat).build();
	}

	@GET
	@Path("/series/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesForSearch", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public Response getSeriesForSearch() {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getSeriesForSearch();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_CNIS })
	@PUT
	@Path("/series/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesById", summary = "Update series")
	public Response setSeriesById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Series to update", required = true,
			content = @Content(schema = @Schema(implementation = Series.class)))String body) {
		try {
			operationsService.setSeries(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	@GET
	@Path("/series/{id}/operationsWithoutReport")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationsWithoutReport", summary = "Operations without metadataReport",  responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Operation.class)))})
	public Response getOperationsWithoutReport(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getOperationsWithoutReport(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@POST
	@Path("/series")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createSeries", summary = "Create series")
	public Response createSeries(
			@RequestBody(description = "Series to create", required = true, 
			content = @Content(schema = @Schema(implementation = Series.class))) String body) {
		String id = null;
		try {
			id = operationsService.createSeries(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR })
	@PUT
	@Path("/series/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesValidation", summary = "Series validation")
	public Response setSeriesValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setSeriesValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}


	@GET
	@Path("/series/seriesForStamp/{stamp}")	
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "seriesForStamp", summary = "Series with given stamp")
	public Response getSeriesForStamp(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathParam(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesForStamp(stamp);	
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series/seriesIdsForStamp/{stamp}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "seriesIdsForStamp", summary = "Ids of Series with given stamp")
	public Response getSeriesIdsForStamp(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathParam(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesIdsForStamp(stamp);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/seriesWithStamp/{stamp}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "seriesIdsForStamp", summary = "Series with given stamp as creator")
	public Response getSeriesWithStamp(@Parameter(
			description = "Timbre d'un utilisateur (format : ([A-Za-z0-9_-]+))",
			required = true,
			schema = @Schema(pattern = "([A-Za-z0-9_-]+)", type = "string")) @PathParam(Constants.STAMP) String stamp
			) throws RmesException {
		String jsonResultat = operationsService.getSeriesWithStamp(stamp);	
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
}
