package fr.insee.rmes.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.organizations.Organization;
import fr.insee.rmes.persistance.service.OrganizationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("/organizations")
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

	final static Logger logger = LogManager.getLogger(OrganizationsResources.class);

	@Autowired
	OrganizationsService organizationsService;


	@GET
	@Path("/organization/{identifier}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getOrganizationByIdentifier", summary = "Organization" , responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Organization.class)))})
	public Response getOrganizationByIdentifier(@PathParam("identifier") String identifier) {
		String jsonResultat = organizationsService.getOrganization(identifier);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getOrganizations", summary = "List of organizations" , responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getOrganizations() {
		String jsonResultat = organizationsService.getOrganizations();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
