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
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.concepts.CollectionById;
import fr.insee.rmes.config.swagger.model.concepts.CollectionMembers;
import fr.insee.rmes.config.swagger.model.concepts.CollectionsToValidate;
import fr.insee.rmes.config.swagger.model.concepts.ConceptById;
import fr.insee.rmes.config.swagger.model.concepts.ConceptLinks;
import fr.insee.rmes.config.swagger.model.concepts.ConceptNotes;
import fr.insee.rmes.config.swagger.model.concepts.ConceptsSearch;
import fr.insee.rmes.config.swagger.model.concepts.ConceptsToValidate;
import fr.insee.rmes.persistance.service.ConceptsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 *
 */
@Component
@Path("/concepts")
@Api(value = "Concept API", tags = { "Concepts" })
@ApiResponses(value = { 
		@ApiResponse(code = 200, message = "Success"),
		@ApiResponse(code = 204, message = "No Content"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not found"),
		@ApiResponse(code = 406, message = "Not Acceptable"),
		@ApiResponse(code = 500, message = "Internal server error") })
public class ConceptsResources {

	final static Logger logger = LogManager.getLogger(ConceptsResources.class);

	@Autowired
	ConceptsService conceptsService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConcepts", value = "List of concepts", response = IdLabelAltLabel.class , responseContainer = "List")																 
	public Response getConcepts() {
		String jsonResultat = conceptsService.getConcepts();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConceptsSearch", value = "Rich list of concepts", response = ConceptsSearch.class , responseContainer = "List")																 
	public Response getConceptsSearch() {
		String jsonResultat = conceptsService.getConceptsSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConceptByID", value = "Concept", response = ConceptById.class)																 
	public Response getConceptByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConceptsToValidate", value = "List of concepts to validate", response = ConceptsToValidate.class , responseContainer = "List")
	public Response getConceptsToValidate() {
		String jsonResultat = conceptsService.getConceptsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/links")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConceptLinksByID", value = "List of linked concepts", response = ConceptLinks.class , responseContainer = "List")
	public Response getConceptLinksByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptLinksByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getConceptNotesByID", value = "Last notes of the concept", response = ConceptNotes.class)
	public Response getConceptNotesByID(@PathParam("id") String id, @PathParam("conceptVersion") int conceptVersion) {
		String jsonResultat = conceptsService.getConceptNotesByID(id, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCollections", value = "List of collections", response = IdLabel.class , responseContainer = "List")
	public Response getCollections() {
		String jsonResultat = conceptsService.getCollections();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCollectionsDashboard", value = "Rich list of collections", response = IdLabel.class , responseContainer = "List")
	public Response getCollectionsDashboard() {
		String jsonResultat = conceptsService.getCollectionsDashboard();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCollectionsToValidate", value = "List of collections to validate", response = CollectionsToValidate.class , responseContainer = "List")
	public Response getCollectionsToValidate() {
		String jsonResultat = conceptsService.getCollectionsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCollectionByID", value = "Collection", response = CollectionById.class)
	public Response getCollectionByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}/members")
	@ApiOperation(nickname = "getCollectionMembersByID", value = "List of collection member concepts", response = CollectionMembers.class , responseContainer = "List")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionMembersByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionMembersByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/concept")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setConcept", value = "Create concept")
	public Response setConcept(@ApiParam(value = "Concept", required = true) String body) {
		String id = conceptsService.setConcept(body);
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/concept/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setConceptById", value = "Update concept")
	public Response setConcept(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id,
			@ApiParam(value = "Concept", required = true) String body) {
		conceptsService.setConcept(id, body);
		logger.info("Update concept : " + id);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CREATOR })
	@PUT
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setConceptsValidation", value = "Concepts validation")
	public Response setConceptsValidation(
			@ApiParam(value = "Concept id array to validate", required = true) String body) throws Exception {
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
	@ApiOperation(nickname = "getConceptExport", value = "Blob of concept")
	public Response getConceptExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getConceptExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_CONCEPTS_CREATOR })
	@POST
	@Path("/concept/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(nickname = "setConceptSend", value = "Send concept", response = Boolean.class)
	public Response setConceptSend(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id,
			@ApiParam(value = "Mail informations", required = true) String body) throws Exception {
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
	@ApiOperation(nickname = "setCollection", value = "Create collection")
	public Response setCollection(@ApiParam(value = "Collection", required = true) String body) {
		conceptsService.setCollection(body);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@PUT
	@Path("/collection/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "setCollectionById", value = "Update collection")
	public Response setCollection(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id,
			@ApiParam(value = "Collection", required = true) String body) throws Exception {
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
	@ApiOperation(nickname = "setCollectionsValidation", value = "Collections validation")
	public Response setCollectionsValidation(
			@ApiParam(value = "Collection id array to validate", required = true) String body) throws Exception {
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
	@ApiOperation(nickname = "getCollectionExport", value = "Blob of collection")
	public Response getCollectionExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getCollectionExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@POST
	@Path("/collection/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(nickname = "setCollectionSend", value = "Send collection", response = Boolean.class)
	public Response setCollectionSend(
			@ApiParam(value = "Id", required = true) @PathParam("id") String id,
			@ApiParam(value = "Mail informations", required = true) String body) throws Exception {
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
