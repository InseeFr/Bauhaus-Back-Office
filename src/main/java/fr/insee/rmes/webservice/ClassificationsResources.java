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

import fr.insee.rmes.persistance.service.ClassificationsService;

@Component
@Path("/classifications")
public class ClassificationsResources {

	final static Logger logger = LogManager.getLogger(ClassificationsResources.class);

	@Autowired
	ClassificationsService classificationsService;
	
	@GET
	@Path("/families")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFamilies() throws Exception {
		String jsonResultat = classificationsService.getFamilies();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	@GET
	@Path("/series")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSeries() throws Exception {
		String jsonResultat = classificationsService.getSeries();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassifications() throws Exception {
		String jsonResultat = classificationsService.getClassifications();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
