package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.AuthType;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.LabelUrl;
import fr.insee.rmes.config.swagger.model.application.Init;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class for resources 
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

	static final Logger logger = LogManager.getLogger(PublicResources.class);

	@Autowired
	UserRolesManagerService userRolesManagerService;
	
	@Autowired
	StampsService stampsService;

	@GET
	@Path("/init")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getInit", summary = "Initial properties", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Init.class)))})
	public Response getProperties() throws RmesException {
		JSONObject props = new JSONObject();
		try {
			props.put("appHost", Config.APP_HOST);
			props.put("defaultContributor", Config.DEFAULT_CONTRIBUTOR);
			props.put("defaultMailSender", Config.DEFAULT_MAIL_SENDER);
			props.put("maxLengthScopeNote", Config.MAX_LENGTH_SCOPE_NOTE);
			props.put("lg1", Config.LG1);
			props.put("lg2", Config.LG2);
			props.put("authType", AuthType.getAuthType());
			props.put("modules", getActiveModules());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR,e.getMessage(),e.getClass().getSimpleName());
		}
		return Response.status(HttpStatus.SC_OK).entity(props.toString()).build();
	}

	private List<String> getActiveModules() {
        String dirPath = Config.DOCUMENTS_STORAGE_GESTION + "\\BauhausActiveModules.txt";
        File file = new File(dirPath);
        try {
			return FileUtils.readLines(file, StandardCharsets.UTF_8);//Read lines in a list
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<>();
		} 
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
				return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
			}
			return Response.status(HttpStatus.SC_OK).entity(entity).build();
	}

	
	@GET
	@Path("/disseminationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDisseminationStatus", summary = "List of dissemination status", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=LabelUrl.class))))})
	public Response getDisseminationStatus() {
		TreeSet<String> dsList = new TreeSet<>();
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
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
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
			return Response.status(e.getStatus()).entity(e.getDetails()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(entity).build();
	}

}