package fr.insee.rmes.modules.organisations.webservice;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabel;
import fr.insee.rmes.modules.commons.configuration.swagger.model.organizations.Organization;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.utils.XMLUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@SecurityRequirement(name = "bearerAuth")
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
public class OrganisationsResources {


	static final Logger logger = LoggerFactory.getLogger(OrganisationsResources.class);

	final OrganizationsService organizationsService;

	public OrganisationsResources(OrganizationsService organizationsService) {
		this.organizationsService = organizationsService;
	}

	@GetMapping(value = "/organization/{identifier}", 
			produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@Operation(summary = "Organization" , responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Organization.class)))})
	public ResponseEntity<Object> getOrganizationByIdentifier(@PathVariable("identifier") String identifier,
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
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

	@GetMapping(value = "", 
			produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	@Operation(summary = "List of organizations" , responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getOrganizations(
			@Parameter(hidden = true) @RequestHeader(required=false) String accept) {
		String resultat;
		if (accept != null && accept.equals(MediaType.APPLICATION_XML_VALUE)) {
			try {
				resultat=XMLUtils.produceXMLResponse(organizationsService.getOrganizations());
			} catch (RmesException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getDetails());
			}
		}
		else try {
			logger.info("[OrganizationsResources] Starting fetching organizations");
			resultat = organizationsService.getOrganizationsJson();
			logger.info("[OrganizationsResources] fetching organizations is now done");
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.SC_OK).body(resultat);
	}

}
