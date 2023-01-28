package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * WebService class for resources of Documents and Links
 * 
 *
 */
@RestController
@RequestMapping("/documents")
@SecurityRequirement(name = "bearerAuth")
@Tag(name=Constants.DOCUMENT, description="Document API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class DocumentsResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(DocumentsResources.class);

	@Autowired
	DocumentsService documentsService;

	/*
	 * DOCUMENTS AND LINKS
	 */
	
	/**
	 * Get the list of all documents and links
	 * @return
	 */
	@GetMapping
	@Operation(operationId = "getDocuments", summary = "List of documents and links",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=Document.class))))})																 
	public ResponseEntity<Object> getDocuments() {
		String jsonResultat;
		try {
			jsonResultat = documentsService.getDocuments();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}


	
	/*
	 * DOCUMENTS
	 */
	/**
	 * Get One Document
	 * @param id
	 * @return
	 */
	@GetMapping("/document/{id}")
	@Operation(operationId = "getDocument", summary = "Get a Document",
		responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=Document.class)))})																 
	public ResponseEntity<Object> getDocument(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = documentsService.getDocument(id).toString();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResultat);
	}
	
	@GetMapping(value = "/document/{id}/file", produces = "*/*")
	@Operation(operationId = "downloadDocument", summary = "Download the Document file")																 
	public ResponseEntity<Object> downloadDocument(@PathVariable(Constants.ID) String id) {
		try {
			return documentsService.downloadDocument(id);
		} catch (RmesException e) {
			logger.error(e.getDetails());
			return returnRmesException(e);
		} catch (IOException e) {
			logger.error("IOException {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
		}
	}
	
	
	
	/**
	 * Create a new document
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@Operation(operationId = "setDocument", summary = "Create document" )
	@PostMapping(value = "/document", 
	consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
			MediaType.APPLICATION_OCTET_STREAM_VALUE,
			"application/vnd.oasis.opendocument.text",
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Object> setDocument(
			@Parameter(description = Constants.DOCUMENT, required = true, schema = @Schema(implementation=Document.class))
			@RequestParam(value="body") String body,
			@Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file" ))
			@RequestParam(value = "file") MultipartFile  documentFile
			) {
		String id = null;
		String documentName = documentFile.getOriginalFilename();
		try {
			id = documentsService.createDocument(body, documentFile.getInputStream(), documentName);
		} catch (RmesException e) {
			return returnRmesException(e);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().body("IOException"+e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	/**
	 * Update informations about a document
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PutMapping("/document/{id}")
	@Operation(operationId = "setDocumentById", summary = "Update document ")
	public ResponseEntity<Object> setDocument(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = Constants.DOCUMENT, required = true, schema = @Schema(implementation=Document.class))@RequestBody String body) {
		try {
			documentsService.setDocument(id, body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		logger.info("Update document : {}", id);
		return ResponseEntity.ok(id);
	}
	


	/**
	 * Change the file of a document
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")	@Operation(operationId = "changeDocument", summary = "Change document file" )
	@PutMapping(value = "/document/{id}/file", 
	consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
			MediaType.APPLICATION_OCTET_STREAM_VALUE,
			"application/vnd.oasis.opendocument.text",
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Object> changeDocument(
			@Parameter(description = "Fichier", required = true, schema = @Schema(type = "string", format = "binary", description = "file"))
			@RequestParam(value = "file") MultipartFile documentFile,
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id
			) throws RmesException {
		String url = null;
		String documentName = documentFile.getOriginalFilename();
		try {
			url = documentsService.changeDocument(id, documentFile.getInputStream(), documentName);
		} catch (RmesException e) {
			return returnRmesException(e);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().body("IOException"+e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.OK).body(url);
	}
	
	/**
	 * Delete a document
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")	@DeleteMapping("/document/{id}")
	@Operation(operationId = "deleteDocument", summary = "Delete a document")
	public ResponseEntity<Object> deleteDocument(@PathVariable(Constants.ID) String id) {
		HttpStatus status = null;
		try {
			status = documentsService.deleteDocument(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(status).body(id);
	}

	/*
	 * LINKS
	 */
	

	/**
	 * Get One Link
	 * @param id
	 * @return
	 */
	@GetMapping("/link/{id}")
	@Operation(operationId = "getLink", summary = "Get a Link",
	responses = {@ApiResponse(content=@Content(schema=@Schema(implementation=Document.class)))})																 
	public ResponseEntity<Object> getLink(@PathVariable(Constants.ID) String id) {
		try {
			String link = documentsService.getLink(id).toString();
			return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(link);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	/**
	 * Create a new link
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PostMapping(value = "/link",
		consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
				MediaType.APPLICATION_OCTET_STREAM_VALUE,
				"application/vnd.oasis.opendocument.text",
				MediaType.APPLICATION_JSON_VALUE })
	@Operation(operationId = "setDocument", summary = "Create link" )
	public ResponseEntity<Object> setLink(
			@Parameter(description = "Link", required = true, schema = @Schema(implementation=Document.class))
			@RequestParam(value="body") String body
			) {
		String id = null;
		try {
			id = documentsService.setLink(body);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	/**
	 * Update informations about a link
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")
	@PutMapping("/link/{id}")
	@Operation(operationId = "setLinkById", summary = "Update link")
	public ResponseEntity<Object> setLink(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Link", required = true, schema = @Schema(implementation=Document.class)) @RequestBody String body) {
		try {
			documentsService.setLink(id, body);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		logger.info("Update link : {}", id);
		return ResponseEntity.ok(id);
	}
	
	/**
	 * Delete a link
	 */
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isIndicatorContributor() "
			+ "|| @AuthorizeMethodDecider.isSeriesContributor() ")	@DeleteMapping("/link/{id}")
	@Operation(operationId = "deleteLink", summary = "Delete a link")
	public ResponseEntity<Object> deleteLink(@PathVariable(Constants.ID) String id) {
		HttpStatus status = null;
		try {
			status = documentsService.deleteLink(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getDetails());
		}
		return ResponseEntity.status(status).body(id);
	}
	
	
}
