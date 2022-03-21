package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.roles.UserRolesManagerService;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.authentication.stamps.StampsService;
import io.swagger.v3.oas.annotations.Operation;
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
@RestController
@RequestMapping("/users")
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
public class UserResources {

	static final Logger logger = LogManager.getLogger(UserResources.class);

	@Autowired
	UserRolesManagerService userRolesManagerService;
	
	@Autowired
	StampsService stampsService;

	@Autowired
	StampsRestrictionsService stampsRestrictionService;

	@GetMapping("/stamp")
	@Produces(MediaType.APPLICATION_JSON)
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

	@PostMapping("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "login", summary = "Fake Login", responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))})
	public ResponseEntity<Object> login(@RequestBody(description = "Component", required = true) String user) throws JsonProcessingException {
		stampsRestrictionService.setFakeUser(user);
		return ResponseEntity.status(HttpStatus.SC_OK).build();
	}
	
	
	@Secured({ Roles.SPRING_ADMIN })
	@PostMapping("/private/add/role/{role}/user/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setAddRole", summary = "Add role")
	public ResponseEntity<Object> setAddRole(@PathVariable("role") String role, @PathVariable("user") String user) {
		userRolesManagerService.setAddRole(role, user);
		return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).build();
	}

	@Secured({ Roles.SPRING_ADMIN })
	@PostMapping("/private/delete/role/{role}/user/{user}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDeleteRole", summary = "Delete role")
	public ResponseEntity<Object> setDeleteRole(@PathVariable("role") String role, @PathVariable("user") String user) {
		userRolesManagerService.setDeleteRole(role, user);
		return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).build();
	}
}