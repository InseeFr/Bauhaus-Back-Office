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
	@Path("/family/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFamily(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getFamily(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/family/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFamilyMembers(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getFamilyMembers(id);
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
	@Path("/series/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneSeries(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getOneSeries(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/series/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSeriesMembers(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getSeriesMembers(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassifications() throws Exception {
		String jsonResultat = classificationsService.getClassifications();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassification(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassification(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/items")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItems(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassificationItems(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/levels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevels(@PathParam("id") String id) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevels(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{id}/level/{depth}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevel(@PathParam("id") String id, @PathParam("depth") String depth) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevel(id, depth);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/level/{levelId}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationLevelMembers(@PathParam("classificationId") String classificationId, @PathParam("levelId") String levelId) throws Exception {
		String jsonResultat = classificationsService.getClassificationLevelMembers(classificationId, levelId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItem(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId) throws Exception {
		String jsonResultat = classificationsService.getClassificationItem(classificationId, itemId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNotes(@PathParam("classificationId") String classificationId,
			@PathParam("itemId") String itemId, @PathParam("conceptVersion") int conceptVersion) throws Exception {
		String jsonResultat = classificationsService.getClassificationItemNotes(classificationId, itemId, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/classification/{classificationId}/item/{itemId}/narrowers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClassificationItemNarrowers(@PathParam("classificationId") String classificationId, @PathParam("itemId") String itemId) throws Exception {
		String jsonResultat = classificationsService.getClassificationItemNarrowers(classificationId, itemId);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/correspondences")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCorrespondences() throws Exception {
		String jsonResultat = classificationsService.getCorrespondences();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

}
