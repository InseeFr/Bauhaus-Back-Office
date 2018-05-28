package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.roles.Constants;
import fr.insee.rmes.persistance.service.ConceptsService;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 *
 */
@Component
@Path("/concepts")
public class ConceptsResources {

	final static Logger logger = LogManager.getLogger(ConceptsResources.class);

	@Autowired
	ConceptsService conceptsService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConcepts() {
		String jsonResultat = conceptsService.getConcepts();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptsSearch() {
		String jsonResultat = conceptsService.getConceptsSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptsToValidate() {
		String jsonResultat = conceptsService.getConceptsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/links")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptLinksByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptLinksByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConceptNotesByID(@PathParam("id") String id, @PathParam("conceptVersion") int conceptVersion) {
		String jsonResultat = conceptsService.getConceptNotesByID(id, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollections() {
		String jsonResultat = conceptsService.getCollections();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionsDashboard() {
		String jsonResultat = conceptsService.getCollectionsDashboard();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionsToValidate() {
		String jsonResultat = conceptsService.getCollectionsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}/members")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionMembersByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionMembersByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/concept")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConcept(String body) {
		String id = conceptsService.setConcept(body);
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/concept/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConcept(@PathParam("id") String id, String body) {
		conceptsService.setConcept(id, body);
		logger.info("Update concept : " + id);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CREATOR })
	@PUT
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setConceptsValidation(String body) throws Exception {
		try {
			conceptsService.setConceptsValidation(body);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GET
	@Path("/concept/export/{id}")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	public Response getConceptExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getConceptExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_CONCEPTS_CREATOR })
	@POST
	@Path("/concept/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response setConceptSend(@PathParam("id") String id, String body) throws Exception {
		try {
			Boolean isSent = conceptsService.setConceptSend(id, body);
			logger.info("Send concept : " + id);
			return Response.status(Status.OK).entity(isSent).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/collection")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setCollection(String body) {
		conceptsService.setCollection(body);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@PUT
	@Path("/collection/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setCollection(@PathParam("id") String id, String body) throws Exception {
		try {
			conceptsService.setCollection(id, body);
			logger.info("Update collection : " + id);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_COLLECTIONS_CREATOR })
	@PUT
	@Path("/collections/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setCollectionsValidation(String body) throws Exception {
		try {
			conceptsService.setCollectionsValidation(body);
			logger.info("Validated concepts : " + body);
			return Response.status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GET
	@Path("/collection/export/{id}")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text" })
	public Response getCollectionExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getCollectionExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@POST
	@Path("/collection/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response setCollectionSend(@PathParam("id") String id, String body) throws Exception {
		try {
			Boolean isSent = conceptsService.setCollectionSend(id, body);
			logger.info("Send concept : " + id);
			return Response.status(Status.OK).entity(isSent).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}
