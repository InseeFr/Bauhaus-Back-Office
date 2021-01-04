package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabelSims;
import fr.insee.rmes.config.swagger.model.operations.documentation.Attribute;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Parameter;
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

	static final Logger logger = LogManager.getLogger(OperationsResources.class);

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
	public Response getFamilies() throws RmesException {
		String jsonResultat = operationsService.getFamilies();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/families/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamiliesForSearch", summary = "List of families for search",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Family.class))))})
	public Response getFamiliesForSearch() throws RmesException {
		String jsonResultat = operationsService.getFamiliesForSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFamilyByID", summary = "Get a family", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Family.class)))}
			)
	public Response getFamilyByID(@PathParam(Constants.ID) String id) throws RmesException {
		String jsonResultat = operationsService.getFamilyByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	/**
	 * UPDATE
	 * @param id, body
	 * @return response
	 */

	@Secured({ Roles.SPRING_ADMIN })
	@PUT
	@Path("/family/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyById", summary = "Update family" )
	public Response setFamilyById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Family to update", required = true,
			content = @Content(schema = @Schema(implementation = Family.class))) String body) {
		try {
			operationsService.setFamily(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}


	/**
	 * CREATE
	 * @param body
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN })
	@POST
	@Path("/family")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createFamily", summary = "Create family")
	public Response createFamily(
			@RequestBody(description = "Family to create", required = true, 
			content = @Content(schema = @Schema(implementation = Family.class))) String body) {
		String id = null;
		try {
			id = operationsService.createFamily(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Roles.SPRING_ADMIN })
	@PUT
	@Path("/family/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setFamilyValidation", summary = "Family validation")
	public Response setFamilyValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setFamilyValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}




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


	/***************************************************************************************************
	 * OPERATIONS
	 ******************************************************************************************************/



	@GET
	@Path("/operations")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperations", summary = "List of operations", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getOperations() throws RmesException {
		String jsonResultat = operationsService.getOperations();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}


	@GET
	@Path("/operation/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getOperationByID", summary = "Operation", 
	responses = { @ApiResponse(content = @Content(/*mediaType = "application/json",*/ schema = @Schema(implementation = Operation.class)))})
	public Response getOperationByID(@PathParam(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				resultat=XMLUtils.produceXMLResponse(operationsService.getOperationById(id));
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		} else {
			try {
				resultat = operationsService.getOperationJsonByID(id);
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(resultat).build();
	}


	@POST
	@Path("/operation/codebook")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getCodeBook", summary = "Produce a codebook from a DDI")
	public Response getCodeBook( @HeaderParam("Accept") String acceptHeader, 
			@Parameter(schema = @Schema(type = "string", format = "binary", description = "file in DDI"))
	@FormDataParam("file") InputStream isDDI,
	@Parameter(schema = @Schema(type = "string", format = "binary", description = "file 2"))
	@FormDataParam(value = "dicoVar") InputStream isCodeBook) throws IOException, RmesException {
		String ddi = IOUtils.toString(isDDI, StandardCharsets.UTF_8); 
		File codeBookFile = fr.insee.rmes.utils.FileUtils.streamToFile(isCodeBook, "dicoVar",".odt");
		return operationsService.getCodeBookExport(ddi,codeBookFile, acceptHeader);	
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_CNIS })
	@PUT
	@Path("/operation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationById", summary = "Update operation")
	public Response setOperationById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Operation to update", required = true, 
			content = @Content(schema = @Schema(implementation = Operation.class))) String body) {
		try {
			operationsService.setOperation(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR })
	@POST
	@Path("/operation")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "createOperation", summary = "Create operation")
	public Response createOperation(
			@RequestBody(description = "Operation to create", required = true, 
			content = @Content(schema = @Schema(implementation = Operation.class))) String body) {
		String id = null;
		try {
			id = operationsService.createOperation(body);
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
	@Path("/operation/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setOperationValidation", summary = "Operation validation")
	public Response setOperationValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setOperationValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}



	/***************************************************************************************************
	 * INDICATORS
	 ******************************************************************************************************/
	@GET
	@Path("/indicators")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicators", summary = "List of indicators", 
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=IdLabelAltLabel.class)))})
	public Response getIndicators() throws RmesException {
		String jsonResultat = operationsService.getIndicators();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/indicators/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorsForSearch", summary = "List of indicators for search",
	responses = {@ApiResponse(content=@Content(schema=@Schema(type="array",implementation=Indicator.class)))})
	public Response getIndicatorsForSearch() throws RmesException {
		String jsonResultat = operationsService.getIndicatorsForSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();

	}

	@GET
	@Path("/indicator/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getIndicatorByID", summary = "Indicator", 
	responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Indicator.class)))})
	public Response getIndicatorByID(@PathParam(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				resultat=XMLUtils.produceXMLResponse(operationsService.getIndicatorById(id));
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		} else {
			try {
				resultat = operationsService.getIndicatorJsonByID(id);
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(resultat).build();
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@PUT
	@Path("/indicator/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorById", summary = "Update indicator")
	public Response setIndicatorById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Indicator to update", required = true,
			content = @Content(schema = @Schema(implementation = Indicator.class))) String body) {
		try {
			operationsService.setIndicator(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@PUT
	@Path("/indicator/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicatorValidation", summary = "Indicator validation")
	public Response setIndicatorValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.setIndicatorValidation(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@POST
	@Path("/indicator")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setIndicator", summary = "Create indicator",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN))})
	public Response setIndicator(@RequestBody(description = "Indicator to create", required = true,
	content = @Content(schema = @Schema(implementation = Indicator.class))) String body) {
		logger.info("POST indicator");
		String id = null;
		try {
			id = operationsService.setIndicator(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		if (id == null) {return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(id).build();}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}




	/***************************************************************************************************
	 * DOCUMENTATION
	 ******************************************************************************************************/

	@GET
	@Path("/metadataStructureDefinition")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMsd", summary = "Metadata structure definition", 
	responses = { @ApiResponse(content = @Content(/*mediaType = "application/json",*/ schema = @Schema(implementation = MAS.class)))})
	public Response getMSD(
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header
			) {
		MSD msd ;
		String jsonResultat = null ;
		
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				msd = operationsService.getMSD();
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.ok(XMLUtils.produceResponse(msd, header)).build();
		}
		
		else {
			try {
				jsonResultat = operationsService.getMSDJson();
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
		}
	}
	
	@GET
	@Path("/metadataAttribute/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMA", summary = "Metadata attribute specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Attribute.class)))})
	public Response getMetadataAttribute(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataAttribute(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/metadataAttributes")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMAs", summary = "Metadata attributes specification and property", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type="array",implementation = Attribute.class)))})
	public Response getMetadataAttributes() {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataAttributes();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


	@GET
	@Path("/metadataReport/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Metadata report for an id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public Response getMetadataReport(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataReport(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/metadataReport/default")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReportDefaultValue", summary = "Get default value for metadata report",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
			))})
	public Response getMetadataReportDefaultValue() throws IOException {
		return Response.status(HttpStatus.SC_OK).entity(operationsService.getMetadataReportDefaultValue()).build();
	}

	@GET
	@Path("/metadataReport/fullSims/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@io.swagger.v3.oas.annotations.Operation(operationId = "getFullSims", summary = "Full sims for an id", 
	responses = { @ApiResponse(content = @Content(/*mediaType = "application/json" ,*/ schema = @Schema(implementation = Documentation.class)
			))})
	public Response getFullSims(
			@Parameter(
					description = "Identifiant de la documentation (format : [0-9]{4})",
					required = true,
					schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathParam(Constants.ID) String id,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header
			) {
		Documentation fullsims;
		try {
			fullsims = operationsService.getFullSims(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}

		return Response.ok(XMLUtils.produceResponse(fullsims, header)).build();
	}

	/**
	 * GET
	 * @param id
	 * @return
	 */

	@GET
	@Path("/metadataReport/Owner/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "getMetadataReport", summary = "Owner stamp for a Metadata report's id", 
	responses = { @ApiResponse(content = @Content(mediaType = "application/json" , schema = @Schema(implementation = Documentation.class)
	))})
	public Response getMetadataReportOwner(@PathParam(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = operationsService.getMetadataReportOwner(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@POST
	@Path("/metadataReport")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReport", summary = "Create metadata report",
	responses = { @ApiResponse(content = @Content(mediaType = MediaType.TEXT_PLAIN))})
	public Response setMetadataReport(@RequestBody(description = "Metadata report to create", required = true,
	content = @Content(schema = @Schema(implementation = Documentation.class))) String body) {
		logger.info("POST Metadata report");
		String id = null;
		try {
			id = operationsService.createMetadataReport(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		if (id == null) {return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(id).build();}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR, Roles.SPRING_CNIS })
	@PUT
	@Path("/metadataReport/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportById", summary = "Update metadata report")
	public Response setMetadataReportById(
			@PathParam(Constants.ID) String id, 
			@RequestBody(description = "Report to update", required = true,
			content = @Content(schema = @Schema(implementation = Documentation.class))) String body) {
		try {
			operationsService.setMetadataReport(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * DELETE
	 * @param id
	 * @return
	 */
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR, Roles.SPRING_CNIS })
	@DELETE
	@Path("/metadataReport/delete/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "deleteMetadataReportById", summary = "Delete metadata report")
	public Response deleteMetadataReportById(
			@PathParam(Constants.ID) String id) {
		Status result=Status.NO_CONTENT;
		try {
			 result = operationsService.deleteMetadataReport(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(result).build();
	}

	
	
	/**
	 * PUBLISH
	 * @param id
	 * @return response
	 */	
	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_SERIES_CONTRIBUTOR, Roles.SPRING_INDICATOR_CONTRIBUTOR })
	@PUT
	@Path("/metadataReport/validate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@io.swagger.v3.oas.annotations.Operation(operationId = "setMetadataReportValidation", summary = "Sims validation")
	public Response setSimsValidation(
			@PathParam(Constants.ID) String id) throws RmesException {
		try {
			operationsService.publishMetadataReport(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@GET
	@Path("/metadataReport/export/{id}")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExport", summary = "Produce a document with a metadata report")
	public Response getSimsExport(@Parameter(
			description = "Identifiant de la documentation (format : [0-9]{4})",
			required = true,
			schema = @Schema(pattern = "[0-9]{4}", type = "string")) @PathParam(Constants.ID) String id
			) throws RmesException {
		return operationsService.exportMetadataReport(id);	
	}

	@GET
	@Path("/metadataReport/testExport")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@io.swagger.v3.oas.annotations.Operation(operationId = "getSimsExport", summary = "Produce a document with a metadata report")
	public Response getTestSimsExport() throws RmesException {
		return operationsService.exportTestMetadataReport();	
	}

	
	
	private Response returnRmesException(RmesException e) {
		logger.error(e.getMessage(), e);
		return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
	}

}
