package fr.insee.rmes.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import fr.insee.rmes.persistance.service.OperationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path("/operations")
@Api(value = "Operations API", tags = { "Opérations" })
@ApiResponses(value = { @ApiResponse(code = 400, message = "La syntaxe de la requête est incorrecte"),
		@ApiResponse(code = 401, message = "Une authentification est nécessaire pour accéder à la ressource"),
		@ApiResponse(code = 404, message = "Ressource non trouvée"),
		@ApiResponse(code = 406, message = "L'en-tête HTTP 'Accept' contient une valeur non acceptée"),
		@ApiResponse(code = 500, message = "Erreur interne du serveur") })
public class OperationsResources {

	final static Logger logger = LogManager.getLogger(OperationsResources.class);

	@Autowired
	OperationsService operationsService;

	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getSeries", value = "List of series")
	public Response getSeries() throws Exception {
		String jsonResultat = operationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/operations")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getOperations", value = "List of operations")
	public Response getOperations() throws Exception {
		try {
			String operations = operationsService.getOperations();
			return Response.ok().entity(operations).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GET
	@Path("/operation/{id}/variableBook")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	@ApiOperation(nickname = "getVarBook", value = "Produce a book with all variables of an operation")
	public Response getVarBookExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader)
			throws Exception {
		try {
			return operationsService.getVarBookExport(id, acceptHeader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		// return null;
	}

}
