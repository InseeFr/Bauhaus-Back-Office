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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.manager.SecurityManagerService;
import fr.insee.rmes.config.auth.security.manager.User;
import fr.insee.rmes.config.roles.UserRolesManagerService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.LabelUrl;
import fr.insee.rmes.config.swagger.model.application.Init;
import fr.insee.rmes.config.swagger.model.application.Roles;
import fr.insee.rmes.persistance.disseminationStatus.DisseminationStatus;
import fr.insee.rmes.persistance.stamps.StampsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
@Api(value = "Concept API", tags = { "Application" })
@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 204, message = "No Content"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not found"),
		@ApiResponse(code = 406, message = "Not Acceptable"),
		@ApiResponse(code = 500, message = "Internal server error") })
public class PublicResources {

	final static Logger logger = LogManager.getLogger(PublicResources.class);

	@Autowired
	SecurityManagerService securityManagerService;

	@Autowired
	UserRolesManagerService userRolesManagerService;
	
	@Autowired
	StampsService stampsService;

	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getInit", value = "Initial properties", response = Init.class)
	public Response getProperties() {
		JSONObject props = new JSONObject();
		try {
			props.put("appHost", Config.APP_HOST);
			props.put("defaultContributor", Config.DEFAULT_CONTRIBUTOR);
			props.put("defaultMailSender", Config.DEFAULT_MAIL_SENDER);
			props.put("maxLengthScopeNote", Config.MAX_LENGTH_SCOPE_NOTE);
			props.put("lg1", Config.LG1);
			props.put("lg2", Config.LG2);
			props.put("authType", securityManagerService.getAuthType());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return Response.status(HttpStatus.SC_OK).entity(props.toString()).build();
	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "postAuth", value = "Returns user informations", response = User.class)
	public Response postAuth(String body) {
		return Response.status(HttpStatus.SC_OK).entity(securityManagerService.postAuth(body)).build();
	}

	@GET
	@Path("/stamps")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getStamps", value = "List of stamps", response = String.class)
	public Response getStamps() {
		return Response.status(HttpStatus.SC_OK).entity(stampsService.getStamps()).build();
	}

	@GET
	@Path("/disseminationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getDisseminationStatus", value = "List of dissemination status", response = LabelUrl.class, responseContainer = "List")
	public Response getDisseminationStatus() {
		TreeSet<String> dsList = new TreeSet<String>();
		for (DisseminationStatus ds : DisseminationStatus.values()) {
			try {
				dsList.add(new ObjectMapper().writeValueAsString(ds));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return Response.status(HttpStatus.SC_OK).entity(dsList.toString()).build();
	}

	@GET
	@Path("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getRoles", value = "List of roles", response = Roles.class, responseContainer = "List")
	public Response getRoles() {
		return Response.status(HttpStatus.SC_OK).entity(userRolesManagerService.getRoles()).build();
	}

	@GET
	@Path("/agents")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getAgents", value = "List of agents", response = IdLabel.class, responseContainer = "List")
	public Response getAgents() {
		return Response.status(HttpStatus.SC_OK).entity(userRolesManagerService.getAgents()).build();
	}

	@POST
	@Path("/private/role/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setAddRole", value = "Add roles")
	public Response setAddRole(@ApiParam(value = "Roles and users to add", required = true) String body) {
		userRolesManagerService.setAddRole(body);
		return Response.status(Status.NO_CONTENT).build();
	}

	@POST
	@Path("/private/role/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setDeleteRole", value = "Delete role")
	public Response setDeleteRole(@ApiParam(value = "Role and user to delete", required = true) String body) {
		userRolesManagerService.setDeleteRole(body);
		return Response.status(Status.NO_CONTENT).build();
	}
}