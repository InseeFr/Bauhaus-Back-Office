package fr.insee.rmes.webservice;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.organizations.Organization;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/organizations")
@Tag(name="Organizations", description="Organization API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class OrganizationsResources {

	static final Logger logger = LogManager.getLogger(OrganizationsResources.class);

	@Autowired
	OrganizationsService organizationsService;

	@GetMapping("/organization/{identifier}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(operationId = "getOrganizationByIdentifier", summary = "Organization" , responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Organization.class)))})
	public ResponseEntity<Object> getOrganizationByIdentifier(@PathVariable("identifier") String identifier,
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				resultat=XMLUtils.produceXMLResponse(organizationsService.getOrganization(identifier));
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
		}
		else try {
			resultat = organizationsService.getOrganizationJsonString(identifier);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
	}

	@GetMapping("")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Operation(operationId = "getOrganizations", summary = "List of organizations" , responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getOrganizations(
			@Parameter(hidden = true) @HeaderParam(HttpHeaders.ACCEPT) String header) {
		String resultat;
		if (header != null && header.equals(MediaType.APPLICATION_XML)) {
			try {
				resultat=XMLUtils.produceXMLResponse(organizationsService.getOrganizations());
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
		}
		else try {
			resultat = organizationsService.getOrganizationsJson();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
	}

}
