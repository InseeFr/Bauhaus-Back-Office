package fr.insee.rmes.webservice;

import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.AuthType;
import fr.insee.rmes.config.auth.roles.Constants;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.LabelUrl;
import fr.insee.rmes.config.swagger.model.application.Init;
import fr.insee.rmes.config.swagger.model.application.Roles;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.dissemination_status.DisseminationStatus;
import fr.insee.rmes.persistance.stamps.StampsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 * 
 *         schemes: - http
 * 
 *         consumes: - application/json
 * 
 *         produces: - application/json
 *
 */
@Component
@Path("/")
@Tag(name="Application", description="Application API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class PublicResources {

	final static Logger logger = LogManager.getLogger(PublicResources.class);

	@Autowired
	UserRolesManagerService userRolesManagerService;
	
	@Autowired
	StampsService stampsService;

	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getInit", summary = "Initial properties", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Init.class)))})
	public Response getProperties() {
		JSONObject props = new JSONObject();
		try {
			props.put("appHost", Config.APP_HOST);
			props.put("defaultContributor", Config.DEFAULT_CONTRIBUTOR);
			props.put("defaultMailSender", Config.DEFAULT_MAIL_SENDER);
			props.put("maxLengthScopeNote", Config.MAX_LENGTH_SCOPE_NOTE);
			props.put("lg1", Config.LG1);
			props.put("lg2", Config.LG2);
			props.put("authType", AuthType.getAuthType());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return Response.status(HttpStatus.SC_OK).entity(props.toString()).build();
	}

	@GET
	@Path("/stamps")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getStamps", summary = "List of stamps", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
	public Response getStamps() {
			String entity = null;
			try {
				entity = stampsService.getStamps();
			} catch (RmesException e) {
				return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
			}
			return Response.status(HttpStatus.SC_OK).entity(entity).build();
	}

	@GET
	@Path("/disseminationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDisseminationStatus", summary = "List of dissemination status", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=LabelUrl.class))))})
	public Response getDisseminationStatus() {
		TreeSet<String> dsList = new TreeSet<String>();
		for (DisseminationStatus ds : DisseminationStatus.values()) {
			try {
				dsList.add(new ObjectMapper().writeValueAsString(ds));
			} catch (JsonProcessingException e) {
				return Response.status(500).entity(e.getMessage()).build();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(dsList.toString()).build();
	}

	@GET
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getRoles", summary = "List of roles", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Roles.class))))})
	public Response getRoles() {
		String entity = null;
		try {
			entity = userRolesManagerService.getRoles();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(entity).build();
	}

	@GET
	@Path("/agents")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getAgents", summary = "List of agents", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getAgents() {
		String entity = null;
		try {
			entity = userRolesManagerService.getAgents();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type("text/plain").build();
		}
		return Response.status(HttpStatus.SC_OK).entity(entity).build();
	}

	@Secured({ Constants.SPRING_ADMIN })
	@POST
	@Path("/private/role/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setAddRole", summary = "Add roles")
	public Response setAddRole(@Parameter(required=true)@RequestBody(description = "Roles and users to add") String body) {
		userRolesManagerService.setAddRole(body);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN })
	@POST
	@Path("/private/role/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDeleteRole", summary = "Delete role")
	public Response setDeleteRole(@Parameter(required=true)@RequestBody(description = "Role and user to delete") String body) {
		userRolesManagerService.setDeleteRole(body);
		return Response.status(Status.NO_CONTENT).build();
	}
}