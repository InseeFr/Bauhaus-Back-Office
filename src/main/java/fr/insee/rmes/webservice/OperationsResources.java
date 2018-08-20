package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import fr.insee.rmes.config.swagger.model.operations.documentation.Attribute;
import fr.insee.rmes.config.swagger.model.operations.documentation.MSD;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.families.Family;
import fr.insee.rmes.persistance.service.sesame.operations.indicators.Indicator;
import fr.insee.rmes.persistance.service.sesame.operations.operations.Operation;
import fr.insee.rmes.persistance.service.sesame.operations.series.Series;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Component
@Path("/operations")
@Tag(name="Operations", description="Operation API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
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
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilies", summary = "List of families", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getFamilies() throws Exception {
		String jsonResultat = operationsService.getFamilies();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilyByID", summary = "Get a family", 
		responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Family.class)))}
	)
	public Response getFamilyByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getFamilyByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@Secured({ Constants.SPRING_ADMIN })
	@PUT
	@Path("/family/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyById", summary = "Update family" )
	public Response setFamilyById(
			@PathParam("id") String id, 
			@RequestBody(description = "Family to update", required = true,
            content = @Content(schema = @Schema(implementation = Family.class))) String body) {
		operationsService.setFamily(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}

	/***************************************************************************************************
	 * SERIES
	 ******************************************************************************************************/
	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeries", summary = "List of series", responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getSeries() throws Exception {
		String jsonResultat = operationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSeriesByID", summary = "Series", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Series.class)))})
	public Response getSeriesByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getSeriesByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN })
	@PUT
	@Path("/series/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setSeriesById", summary = "Update series")
	public Response setSeriesById(
			@QueryParam("id") String id, 
			@RequestBody(description = "Series to update", required = true,
            content = @Content(schema = @Schema(implementation = Series.class)))String body) {
		operationsService.setSeries(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}

	/***************************************************************************************************
	 * OPERATIONS
	 ******************************************************************************************************/
	@GET
	@Path("/operations")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperations", summary = "List of operations", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getOperations() throws Exception {
		String jsonResultat = operationsService.getOperations();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/operation/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationByID", summary = "Operation", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Operation.class)))})
	public Response getOperationByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getOperationByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


	@GET
	@Path("/operation/{id}/variableBook")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getVarBook", summary = "Produce a book with all variables of an operation")
	public Response getVarBookExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader)
			throws Exception {
		try {
			return operationsService.getVarBookExport(id, acceptHeader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	@Secured({ Constants.SPRING_ADMIN })
	@PUT
	@Path("/operation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationById", summary = "Update operation")
	public Response setOperationById(
			@QueryParam("id") String id, 
			@RequestBody(description = "Operation to update", required = true,
            content = @Content(schema = @Schema(implementation = Operation.class))) String body) {
		operationsService.setOperation(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}

	/***************************************************************************************************
	 * INDICATORS
	 ******************************************************************************************************/
	@GET
	@Path("/indicators")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicators", summary = "List of indicators", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getIndicators() throws Exception {
		String jsonResultat = operationsService.getIndicators();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/indicator/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorByID", summary = "Indicator", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Indicator.class)))})
	public Response getIndicatorByID(@PathParam("id") String id) {
		String jsonResultat = operationsService.getIndicatorByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN })
	@PUT
	@Path("/indicator/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorById", summary = "Update indicator")
	public Response setIndicatorById(
			@QueryParam("id") String id, 
			@RequestBody(description = "Indicator to update", required = true,
            content = @Content(schema = @Schema(implementation = Indicator.class))) String body) {
		operationsService.setIndicator(id, body);
		return Response.status(Status.NO_CONTENT).build();
	}
	
	@Secured({ Constants.SPRING_ADMIN })
	@POST
	@Path("/indicator")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicator", summary = "Create indicator",
	responses = { @ApiResponse(content = @Content(mediaType = "text/plain"))})
	public Response setIndicator(@RequestBody(description = "Indicator to create", required = true,
            content = @Content(schema = @Schema(implementation = Indicator.class))) String body) {
		logger.info("POST indicator");
		String id = operationsService.setIndicator(body);
		if (id == null) {return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(id).build();}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	/***************************************************************************************************
	 * DOCUMENTATION
	 ******************************************************************************************************/
	@GET
	@Path("/metadataStructureDefinition")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMsd", summary = "Metadata structure definition", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = MSD.class)))})
	public Response getMSD() {
		String jsonResultat = operationsService.getMSD();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/metadataAttribute/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMA", summary = "Metadata attribute specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Attribute.class)))})
	public Response getMetadataAttribute(@PathParam("id") String id) {
		String jsonResultat = operationsService.getMetadataAttribute(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/metadataReport/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Metadata report for an id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json"//TODO , schema = @Schema(implementation = .class)
	))})
	public Response getMetadataReport(@PathParam("id") String id) {
		String jsonResultat = operationsService.getMetadataReport(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
