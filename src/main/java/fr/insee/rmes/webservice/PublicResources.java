package fr.insee.rmes.webservice;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/")
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

	@GetMapping("/init")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getInit", summary = "Initial properties", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Init.class)))})
	public ResponseEntity<Object> getProperties() throws RmesException {
		JSONObject props = new JSONObject();
		try {
			props.put("appHost", Config.getAppHost());
			props.put("defaultContributor", Config.getDefaultContributor());
			props.put("defaultMailSender", Config.getDefaultMailSender());
			props.put("maxLengthScopeNote", Config.getMaxLengthScopeNote());
			props.put("lg1", Config.getLg1());
			props.put("lg2", Config.getLg2());
			props.put("authType", AuthType.getAuthType());
			props.put("modules", getActiveModules());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR,e.getMessage(),e.getClass().getSimpleName());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(props.toString());
	}

	private List<String> getActiveModules() {
        String dirPath = Config.getDocumentsStorageGestion() + "/BauhausActiveModules.txt";
        File file = new File(dirPath);
        try {
			return FileUtils.readLines(file, StandardCharsets.UTF_8);//Read lines in a list
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<>();
		} 
	}

	@GetMapping("/stamps")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getStamps", summary = "List of stamps", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
	public ResponseEntity<Object> getStamps() {
			String entity = null;
			try {
				entity = stampsService.getStamps();
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
			return ResponseEntity.status(HttpStatus.SC_OK).body(entity);
	}

	@GetMapping("/disseminationStatus")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDisseminationStatus", summary = "List of dissemination status", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=LabelUrl.class))))})
	public ResponseEntity<Object> getDisseminationStatus() {
		TreeSet<String> dsList = new TreeSet<>();
		for (DisseminationStatus ds : DisseminationStatus.values()) {
			try {
				dsList.add(new ObjectMapper().writeValueAsString(ds));
			} catch (JsonProcessingException e) {
				return ResponseEntity.status(500).body(e.getMessage());
			}
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(dsList.toString());
	}

	@GetMapping("/roles")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getRoles", summary = "List of roles", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Roles.class))))})
	public ResponseEntity<Object> getRoles() {
		String entity = null;
		try {
			entity = userRolesManagerService.getRoles();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(entity);
	}

	@GetMapping("/agents")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getAgents", summary = "List of agents", responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object>  getAgents() {
		String entity = null;
		try {
			entity = userRolesManagerService.getAgents();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(entity);
	}

}