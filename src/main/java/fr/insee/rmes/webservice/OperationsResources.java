package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.auth.roles.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.operations.IndicatorById;
import fr.insee.rmes.config.swagger.model.operations.OperationById;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.families.Family;
import fr.insee.rmes.persistance.service.sesame.operations.series.Series;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path("/operations")
@Api(value = "Operations API", tags = { "Operations" })
@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 204, message = "No Content"),
		@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not found"),
		@ApiResponse(code = 406, message = "Not Acceptable"),
		@ApiResponse(code = 500, message = "Internal server error") })
public class OperationsResources {

	final static Logger logger = LogManager.getLogger(OperationsResources.class);

	@Autowired
	OperationsService operationsService;

	/***************************************************************************************************
	 * FAMILY
	 ******************************************************************************************************/
	@GET
	@Path("/families")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getFamilies", value = "List of families", response = IdLabel.class,
	responseContainer = "List")
	public Response getFamilies() throws Exception {
		String jsonResultat = operationsService.getFamilies();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getFamilyByID", value = "Family", response = Family.class)
	public Response getFamilyByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getFamilyByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@Secured({ Constants.SPRING_ADMIN })
	@POST
	@Path("/family/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setFamilyById", value = "Update family")
	public Response setFamilyById(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id, 
			@ApiParam(value = "Family", required = true) String body) {
		operationsService.setFamily(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}

	/***************************************************************************************************
	 * SERIES
	 ******************************************************************************************************/
	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getSeries", value = "List of series", response = IdLabelAltLabel.class,
	responseContainer = "List")
	public Response getSeries() throws Exception {
		String jsonResultat = operationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getSeriesByID", value = "Series", response = Series.class)
	public Response getSeriesByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getSeriesByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN })
	@POST
	@Path("/series/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setSeriesById", value = "Update series")
	public Response setSeriesById(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id, 
			@ApiParam(value = "Series", required = true) String body) {
		operationsService.setSeries(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}

	/***************************************************************************************************
	 * OPERATIONS
	 ******************************************************************************************************/
	@GET
	@Path("/operations")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getOperations", value = "List of operations", response = IdLabelAltLabel.class,
	responseContainer = "List")
	public Response getOperations() throws Exception {
		String jsonResultat = operationsService.getOperations();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/operation/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getOperationByID", value = "Operation", response = OperationById.class)
	public Response getOperationByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getOperationByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


	@GET
	@Path("/operation/{id}/variableBook")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@ApiOperation(nickname = "getVarBook", value = "Produce a book with all variables of an operation")
	public Response getVarBookExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader)
			throws Exception {
		try {
			return operationsService.getVarBookExport(id, acceptHeader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/***************************************************************************************************
	 * INDICATORS
	 ******************************************************************************************************/
	@GET
	@Path("/indicators")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getIndicators", value = "List of indicators", response = IdLabelAltLabel.class,
	responseContainer = "List")
	public Response getIndicators() throws Exception {
		String jsonResultat = operationsService.getIndicators();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/indicator/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getIndicatorByID", value = "Indicator", response = IndicatorById.class)
	public Response getIndicatorByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getIndicatorByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}




}
