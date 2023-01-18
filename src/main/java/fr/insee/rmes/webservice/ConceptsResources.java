package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.concepts.*;
import fr.insee.rmes.exceptions.RmesException;
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

import javax.servlet.http.HttpServletResponse;

/**
 * WebService class for resources of Concepts
 * 
 * 
 * @author N. Laval
 *
 */
@RestController
@RequestMapping("/concepts")
@SecurityRequirement(name = "bearerAuth")
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
public class ConceptsResources  extends GenericResources   {
	
	static final Logger logger = LogManager.getLogger(ConceptsResources.class);
	
	@Autowired
	ConceptsService conceptsService;

	@Autowired
	ConceptsCollectionService conceptsCollectionService;

	@GetMapping(value="", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabelAltLabel.class))))})																 
	public ResponseEntity<Object> getConcepts() {
		try {
			String jsonResultat = conceptsService.getConcepts();
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/linkedConcepts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getRelatedConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})																 
	public ResponseEntity<Object> getRelatedConcepts(@PathVariable(Constants.ID) String id) {
		try {
			String resultat = conceptsService.getRelatedConcepts(id);
			return ResponseEntity.status(HttpStatus.OK).body(resultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isCollectionCreator() "
			+ "|| @AuthorizeMethodDecider.isConceptCreator()")
	@DeleteMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "deleteConcept", summary = "deletion")
	public ResponseEntity<Object> deleteConcept(@PathVariable(Constants.ID) String id) {
		try {
			conceptsService.deleteConcept(id);
			return ResponseEntity.status(HttpStatus.OK).body(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@GetMapping(value = "/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsSearch", summary = "Rich list of concepts", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsSearch.class))))})																 
	public ResponseEntity<Object> getConceptsSearch() {
		try {
			String jsonResultat = conceptsService.getConceptsSearch();
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/concept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptByID", summary = "Concept", 
		responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptById.class)))})																 
	public ResponseEntity<Object> getConceptByID(@PathVariable(Constants.ID) String id) {
		try {
			String jsonResultat = conceptsService.getConceptByID(id);
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsToValidate", summary = "List of concepts to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsToValidate.class))))})
	public ResponseEntity<Object> getConceptsToValidate() {
		try {
			String jsonResultat = conceptsService.getConceptsToValidate();
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/concept/{id}/links", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptLinksByID", summary = "List of linked concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptLinks.class))))})
	public ResponseEntity<Object> getConceptLinksByID(@PathVariable(Constants.ID) String id) {
		try {
			String jsonResultat = conceptsService.getConceptLinksByID(id);
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/concept/{id}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptNotesByID", summary = "Last notes of the concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptNotes.class)))})		
	public ResponseEntity<Object> getConceptNotesByID(@PathVariable(Constants.ID) String id, @PathVariable("conceptVersion") int conceptVersion) {
		try {
			String notes = conceptsService.getConceptNotesByID(id, conceptVersion);
			return ResponseEntity.status(HttpStatus.OK).body(notes);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}


	@GetMapping(value = "/collections/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsDashboard", summary = "Rich list of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getCollectionsDashboard() {
		try {
			String jsonResultat = conceptsCollectionService.getCollectionsDashboard();
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/collections/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsToValidate", summary = "List of collections to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionsToValidate.class))))})
	public ResponseEntity<Object> getCollectionsToValidate() {
		try {
			String jsonResultat = conceptsService.getCollectionsToValidate();
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionByID", summary = "Get a collection byt its identifier",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionById.class)))})		
	public ResponseEntity<Object> getCollectionByID(@PathVariable(Constants.ID) String id) {
		try {
			String collection = conceptsCollectionService.getCollectionByID(id);
			return ResponseEntity.status(HttpStatus.OK).body(collection);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/collection/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionMembersByID", summary = "List of collection member concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionMembers.class))))})
	public ResponseEntity<Object> getCollectionMembersByID(@PathVariable(Constants.ID) String id) {
		try {
			String jsonResultat = conceptsCollectionService.getCollectionMembersByID(id);
			return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isConceptsContributor() ")
	@PostMapping(value = "/concept", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConcept", summary = "Create concept" )
	public ResponseEntity<Object> setConcept(
			@Parameter(description = "Concept", required = true) @RequestBody String body) {
		try {
			String id = conceptsService.setConcept(body);
			return ResponseEntity.status(HttpStatus.OK).body(id);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isConceptsContributor() ")
	@PutMapping(value="/concept/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptById", summary = "Update concept")
	public ResponseEntity<Object> setConcept(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Concept", required = true) @RequestBody String body) {
		try {
			conceptsService.setConcept(id, body);
			logger.info("Concept updated : {}" , id);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isConceptCreator() ")
	@PutMapping(value= "/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptsValidation", summary = "Concepts validation")
	public ResponseEntity<Object> setConceptsValidation(
			@Parameter(description = "Id, put '0' if multiple ids", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Concept ids", required = true) @RequestBody String body) throws RmesException {
		try {
			conceptsService.setConceptsValidation(body);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/concept/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip" })
	@Operation(operationId = "exportConcept", summary = "Blob of concept")
	public ResponseEntity<?> exportConcept(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
			return conceptsService.exportConcept(id, accept);
	}

	@GetMapping(value = "/concept/export-zip/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip" })
	@Operation(operationId = "exportConcept", summary = "Blob of concept")
	public void exportZipConcept(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept, HttpServletResponse response) throws RmesException {
		conceptsService.exportZipConcept(id, accept, response);
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isConceptsContributor() ")
	@PostMapping(value = "/collection", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollection", summary = "Create collection")
	public ResponseEntity<Object> setCollection(
			@Parameter(description = "Collection", required = true) @RequestBody String body) {
		try {
			conceptsService.setCollection(body);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isConceptsContributor() "
			+ "|| @AuthorizeMethodDecider.isCollectionCreator()")
	@PutMapping(value = "/collection/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionById", summary = "Update collection")
	public ResponseEntity<Object> setCollection(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Collection", required = true) @RequestBody String body) throws RmesException {
		try {
			conceptsService.setCollection(id, body);
			logger.info("Update collection : {}" , id);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() "
			+ "|| @AuthorizeMethodDecider.isCollectionCreator()")	
	@PutMapping(value= "/collections/validate/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionsValidation", summary = "Collections validation")
	public ResponseEntity<Object> setCollectionsValidation(
			@Parameter(description = "Id, put '0' if multiple ids", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Collection id array to validate", required = true) @RequestBody String body) throws RmesException {
		try {
			conceptsService.setCollectionsValidation(body);
			logger.info("Validated concepts : {}" , body);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GetMapping(value = "/collection/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(operationId = "getCollectionExport", summary = "Blob of collection")
	public ResponseEntity<?> getCollectionExport(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
			return conceptsService.getCollectionExport(id, accept);
	}
}
