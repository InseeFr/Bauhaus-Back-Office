package fr.insee.rmes.webservice;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="User", description="User Management")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class UserResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(UserResources.class);

	@Autowired
	UserRolesManagerService userRolesManagerService;
	
	@Autowired
	StampsService stampsService;

	@Autowired
	StampsRestrictionsService stampsRestrictionService;

	@GetMapping(value = "/stamp",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getStamp", summary = "User's stamp", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))})
	public ResponseEntity<Object> getStamp() {
			String stamp = null;
			try {
				stamp = stampsService.getStamp();
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
			return ResponseEntity.status(HttpStatus.SC_OK).body(stamp);
	}

	@PostMapping(value = "/login",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "login", summary = "Fake Login", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))})
	public ResponseEntity<Object> login(
			@Parameter(description = "Component", required = true) @RequestBody String user) 
			throws JsonProcessingException {
		stampsRestrictionService.setFakeUser(user);
		return ResponseEntity.status(HttpStatus.SC_OK).build();
	}
	

}