package fr.insee.rmes.webservice;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.auth.roles.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.DocumentsService;
import fr.insee.rmes.persistance.service.sesame.operations.documentations.documents.Document;
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
 * WebService class for resources of Documents
 * 
 *
 */
@Component
@Path("/documents")
@Tag(name="Document", description="Document API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class DocumentsRessources {

	private static final String TEXT_PLAIN = "text/plain";

	static final Logger logger = LogManager.getLogger(ConceptsResources.class);
	
	@Autowired
	DocumentsService documentsService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDocuments", summary = "List of documents",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Document.class))))})																 
	public Response getDocuments() {
		String jsonResultat;
		try {
			jsonResultat = documentsService.getDocuments();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDocument", summary = "Document",
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=Document.class)))})																 
	public Response getDocument(@PathParam("id") String id) {
		String jsonResultat;
		try {
			jsonResultat = documentsService.getDocument(id).toString();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
	
	
	@DELETE
	@Path("/{id}")
	@Operation(operationId = "deleteDocument", summary = "deletion")
	public Response deleteConcept(@PathParam("id") String id) {
		String jsonResultat = null;
		try {
			jsonResultat = documentsService.deleteDocument(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}
	
	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/document")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDocument", summary = "Create document" )
	public Response setDocument(
			@Parameter(description = "Document", required = true, schema = @Schema(implementation=Document.class)) String body,
			@Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file 2"))
			@FormDataParam(value = "file") InputStream documentFile
			) throws Exception {
		String id = null;
		try {
			id = documentsService.setDocument(body, documentFile);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/document/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDocumentById", summary = "Update document")
	public Response setDocument(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Document", required = true)
			@Parameter(schema = @Schema(implementation=Document.class)) String body) {
		try {
			documentsService.setDocument(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		logger.info("Update concept : " + id);
		return Response.status(Status.NO_CONTENT).build();
	}
	
}
