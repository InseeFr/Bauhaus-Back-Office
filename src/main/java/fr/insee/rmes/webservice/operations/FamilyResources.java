package fr.insee.rmes.webservice.operations;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Family;
import fr.insee.rmes.webservice.OperationsAbstResources;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/***************************************************************************************************
 * FAMILY
 ******************************************************************************************************/
@Component
@Path("/operations")
public class FamilyResources extends OperationsAbstResources {

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


}
