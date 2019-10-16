package fr.insee.rmes.webservice;

import java.io.IOException;
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
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
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
 * WebService class for resources of Documents and Links
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
public class DocumentsResources {

	private static final String TEXT_PLAIN = "text/plain";

	static final Logger logger = LogManager.getLogger(DocumentsResources.class);

	@Autowired
	DocumentsService documentsService;

	/*
	 * DOCUMENTS AND LINKS
	 */
	
	/*
	 * Get the list of all documents and links
	 */
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDocuments", summary = "List of documents and links",
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

	/*
	 * Get one document or link
	 */
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDocument", summary = "Document or link",
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
	
	@GET
	@Path("/document/{id}")
	@Produces("*/*")
	@Operation(operationId = "downloadDocument", summary = "Download Document")																 
	public Response downloadDocument(@PathParam("id") String id) {
		try {
			return documentsService.downloadDocument(id);
		} catch (RmesException e) {
			logger.error(e.getMessageAndDetails());
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		} catch (IOException e) {
			logger.error("IOException" + e.getMessage());
			return Response.status(HttpStatus.SC_NOT_FOUND).entity(e.getMessage()).type(TEXT_PLAIN).build();
		}
	}


	/*
	 * Update informations about a document (or link), but not the file
	 */

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/document/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setDocumentById", summary = "Update document or link")
	public Response setDocument(
			@Parameter(description = "Id", required = true) @PathParam("id") String id,
			@RequestBody(description = "Document", required = true)
			@Parameter(schema = @Schema(implementation=Document.class)) String body) {
		try {
			documentsService.setDocument(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		logger.info("Update document : " + id);
		return Response.status(Status.OK).build();
	}
	
	/*
	 * Delete a document or link
	 */
	
	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@DELETE
	@Path("/{id}")
	@Operation(operationId = "deleteDocument", summary = "Delete a document or link")
	public Response deleteDocument(@PathParam("id") String id) {
		Status status = null;
		try {
			status = documentsService.deleteDocument(id);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(status).entity(id).build();
	}

	
	
	/*
	 * DOCUMENTS
	 */
	
	
	/*
	 * Create a new document
	 */
	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/document")
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text",MediaType.APPLICATION_JSON })
	@Operation(operationId = "setDocument", summary = "Create document" )
	public Response setDocument(
			@Parameter(description = "Document", required = true, schema = @Schema(implementation=Document.class))
			@FormDataParam(value="body") String body,
			@Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file" ))
			@FormDataParam(value = "file") InputStream documentFile,
			@Parameter(hidden=true) @FormDataParam(value = "file") FormDataContentDisposition fileDisposition
			) throws Exception {
		String id = null;
		String documentName = fileDisposition.getFileName();
		try {
			id = documentsService.setDocument(body, documentFile, documentName);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}


	/*
	 * Change the file of a document
	 */

	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@PUT
	@Path("/{id}")
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text",MediaType.APPLICATION_JSON })
	@Operation(operationId = "changeDocument", summary = "Change document" )
	public Response changeDocument(
			@Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file"))
			@FormDataParam(value = "file") InputStream documentFile,
			@Parameter(hidden=true) @FormDataParam(value = "file") FormDataContentDisposition fileDisposition,
			@Parameter(description = "Id", required = true) @PathParam("id") String id
			) throws Exception {
		String url = null;
		String documentName = fileDisposition.getFileName();
		try {
			url = documentsService.changeDocument(id, documentFile, documentName);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(url).build();
	}

	/*
	 * LINKS
	 */
	
	/*
	 * Create a new link
	 */
	@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR })
	@POST
	@Path("/link")
	@Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM, "application/vnd.oasis.opendocument.text",MediaType.APPLICATION_JSON })
	@Operation(operationId = "setDocument", summary = "Create link" )
	public Response setLink(
			@Parameter(description = "Link", required = true, schema = @Schema(implementation=Document.class))
			@FormDataParam(value="body") String body
			) throws Exception {
		String id = null;
		try {
			id = documentsService.setLink(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getMessageAndDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	
	
}
