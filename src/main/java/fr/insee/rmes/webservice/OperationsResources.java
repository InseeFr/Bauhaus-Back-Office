package fr.insee.rmes.webservice;

import java.util.List;

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
import fr.insee.rmes.persistance.service.sesame.operations.SimpleObjectForList;

@Component
@Path("/operations")
public class OperationsResources {

	final static Logger logger = LogManager.getLogger(OperationsResources.class);

	@Autowired
	OperationsService operationsService;

	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSeries() throws Exception {
		String jsonResultat = operationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/operations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOperations() throws Exception {
		try {
			List<SimpleObjectForList> operations = operationsService.getOperations();
			return Response.ok().entity(operations).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GET
	@Path("/operation/{id}/variableBook")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
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
