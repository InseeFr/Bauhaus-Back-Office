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

import fr.insee.rmes.config.auth.roles.Constants;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 *
 */
@Component
@Path("/concepts")
@Tag(name="Concepts", description="Concept API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class ConceptsResources   {
	
	final static Logger logger = LogManager.getLogger(ConceptsResources.class);
	
	@Autowired
	ConceptsService conceptsService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabelAltLabel.class))))})																 
	public Response getConcepts() {
		String jsonResultat = conceptsService.getConcepts();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/advanced-search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConceptsSearch", summary = "Rich list of concepts", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsSearch.class))))})																 
	public Response getConceptsSearch() {
		String jsonResultat = conceptsService.getConceptsSearch();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConceptByID", summary = "Concept", 
		responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptById.class)))})																 
	public Response getConceptByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConceptsToValidate", summary = "List of concepts to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsToValidate.class))))})
	public Response getConceptsToValidate() {
		String jsonResultat = conceptsService.getConceptsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/links")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConceptLinksByID", summary = "List of linked concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptLinks.class))))})
	public Response getConceptLinksByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getConceptLinksByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/concept/{id}/notes/{conceptVersion}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getConceptNotesByID", summary = "Last notes of the concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptNotes.class)))})		
	public Response getConceptNotesByID(@PathParam("id") String id, @PathParam("conceptVersion") int conceptVersion) {
		String jsonResultat = conceptsService.getConceptNotesByID(id, conceptVersion);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCollections", summary = "List of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getCollections() {
		String jsonResultat = conceptsService.getCollections();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/dashboard")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCollectionsDashboard", summary = "Rich list of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public Response getCollectionsDashboard() {
		String jsonResultat = conceptsService.getCollectionsDashboard();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collections/toValidate")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCollectionsToValidate", summary = "List of collections to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionsToValidate.class))))})
	public Response getCollectionsToValidate() {
		String jsonResultat = conceptsService.getCollectionsToValidate();
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCollectionByID", summary = "Collection", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionById.class)))})		
	public Response getCollectionByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/collection/{id}/members")
	@Operation(operationId = "getCollectionMembersByID", summary = "List of collection member concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionMembers.class))))})
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCollectionMembersByID(@PathParam("id") String id) {
		String jsonResultat = conceptsService.getCollectionMembersByID(id);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/concept")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setConcept", summary = "Create concept" )
	public Response setConcept(@RequestBody(description = "Concept", required = true) String body) {
		String id = conceptsService.setConcept(body);
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/concept/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setConceptById", summary = "Update concept")
	public Response setConcept(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Concept", required = true) String body) {
		conceptsService.setConcept(id, body);
		logger.info("Update concept : " + id);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CREATOR })
	@PUT
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setConceptsValidation", summary = "Concepts validation")
	public Response setConceptsValidation(
			@RequestBody(description = "Concept id array to validate", required = true) String body) throws Exception {
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
	@Operation(operationId = "getConceptExport", summary = "Blob of concept")
	public Response getConceptExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getConceptExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_CONCEPTS_CREATOR })
	@POST
	@Path("/concept/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(operationId = "setConceptSend", summary = "Send concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class)))})	
	public Response setConceptSend(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Mail informations", required = true) String body) throws Exception {
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
	@Operation(operationId = "setCollection", summary = "Create collection")
	public Response setCollection(@RequestBody(description = "Collection", required = true) String body) {
		conceptsService.setCollection(body);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@PUT
	@Path("/collection/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCollectionById", summary = "Update collection")
	public Response setCollection(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Collection", required = true) String body) throws Exception {
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
	@Operation(operationId = "setCollectionsValidation", summary = "Collections validation")
	public Response setCollectionsValidation(
			@RequestBody(description = "Collection id array to validate", required = true) String body) throws Exception {
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
	@Operation(operationId = "getCollectionExport", summary = "Blob of collection")
	public Response getCollectionExport(@PathParam("id") String id, @HeaderParam("Accept") String acceptHeader) {
		return conceptsService.getCollectionExport(id, acceptHeader);
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@POST
	@Path("/collection/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(operationId = "setCollectionSend", summary = "Send collection", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class)))})	
	public Response setCollectionSend(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Mail informations", required = true) String body) throws Exception {
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
