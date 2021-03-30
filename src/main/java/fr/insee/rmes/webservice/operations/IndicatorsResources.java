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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.utils.XMLUtils;
import fr.insee.rmes.webservice.OperationsCommonResources;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Component
@Qualifier("Indicator")
@Path("/operations")
public class IndicatorsResources extends OperationsCommonResources {

	
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


}
