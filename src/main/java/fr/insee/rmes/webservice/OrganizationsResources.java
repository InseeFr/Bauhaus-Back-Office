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

import fr.insee.rmes.config.swagger.model.organizations.Organization;
import fr.insee.rmes.persistance.service.OrganizationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path("/organization")
@Api(value = "Organization API", tags = { "Organization" })
@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 204, message = "No Content"),
		@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not found"),
		@ApiResponse(code = 406, message = "Not Acceptable"),
		@ApiResponse(code = 500, message = "Internal server error") })
public class OrganizationsResources {

	final static Logger logger = LogManager.getLogger(OrganizationsResources.class);

	@Autowired
	OrganizationsService organizationsService;


	@GET
	@Path("/{identifier}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getOrganizationByIdentifier", value = "Organization" , response = Organization.class)
	public Response getOrganizationByIdentifier(@PathParam("identifier") String identifier) {
		String jsonResultat = organizationsService.getOrganization(identifier);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


}
