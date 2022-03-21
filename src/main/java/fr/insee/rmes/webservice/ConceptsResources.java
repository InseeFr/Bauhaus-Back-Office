package fr.insee.rmes.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.auth.roles.Roles;
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
import fr.insee.rmes.exceptions.RmesException;
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
@RestController
@RequestMapping("/concepts")
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
	
	static final Logger logger = LogManager.getLogger(ConceptsResources.class);
	
	@Autowired
	ConceptsService conceptsService;

	@GetMapping(value="", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabelAltLabel.class))))})																 
	public ResponseEntity<Object> getConcepts() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConcepts();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/linkedConcepts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getRelatedConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})																 
	public ResponseEntity<Object> getRelatedConcepts(@PathVariable(Constants.ID) String id) {
		String resultat;
		try {
			resultat = conceptsService.getRelatedConcepts(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultat);
	}
	
	
	//@Secured({ Constants.SPRING_ADMIN, Constants.SPRING_CONCEPTS_CONTRIBUTOR, Constants.SPRING_COLLECTIONS_CREATOR })
	@DeleteMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "deleteConcept", summary = "deletion")
	public ResponseEntity<Object> deleteConcept(@PathVariable(Constants.ID) String id) {
		try {
			conceptsService.deleteConcept(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(value = "/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsSearch", summary = "Rich list of concepts", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsSearch.class))))})																 
	public ResponseEntity<Object> getConceptsSearch() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConceptsSearch();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/concept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptByID", summary = "Concept", 
		responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptById.class)))})																 
	public ResponseEntity<Object> getConceptByID(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConceptByID(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsToValidate", summary = "List of concepts to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsToValidate.class))))})
	public ResponseEntity<Object> getConceptsToValidate() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConceptsToValidate();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/concept/{id}/links", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptLinksByID", summary = "List of linked concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptLinks.class))))})
	public ResponseEntity<Object> getConceptLinksByID(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConceptLinksByID(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/concept/{id}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptNotesByID", summary = "Last notes of the concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptNotes.class)))})		
	public ResponseEntity<Object> getConceptNotesByID(@PathVariable(Constants.ID) String id, @PathVariable("conceptVersion") int conceptVersion) {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getConceptNotesByID(id, conceptVersion);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/collections", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollections", summary = "List of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getCollections() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getCollections();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/collections/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsDashboard", summary = "Rich list of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getCollectionsDashboard() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getCollectionsDashboard();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/collections/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsToValidate", summary = "List of collections to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionsToValidate.class))))})
	public ResponseEntity<Object> getCollectionsToValidate() {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getCollectionsToValidate();
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionByID", summary = "Collection", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionById.class)))})		
	public ResponseEntity<Object> getCollectionByID(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getCollectionByID(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value = "/collection/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionMembersByID", summary = "List of collection member concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionMembers.class))))})
	public ResponseEntity<Object> getCollectionMembersByID(@PathVariable(Constants.ID) String id) {
		String jsonResultat;
		try {
			jsonResultat = conceptsService.getCollectionMembersByID(id);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR })
	@PostMapping("/concept")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConcept", summary = "Create concept" )
	public ResponseEntity<Object> setConcept(@RequestBody(description = "Concept", required = true) String body) {
		String id = null;
		try {
			id = conceptsService.setConcept(body);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR })
	@PutMapping("/concept/{id}")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptById", summary = "Update concept")
	public ResponseEntity<Object> setConcept(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@RequestBody(description = "Concept", required = true) String body) {
		try {
			conceptsService.setConcept(id, body);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		logger.info("Update concept : {}" , id);
		return ResponseEntity.noContent().build();
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPT_CREATOR })
	@PutMapping("/validate")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptsValidation", summary = "Concepts validation")
	public ResponseEntity<Object> setConceptsValidation(
			@RequestBody(description = "Concept id array to validate", required = true) String body) throws RmesException {
		try {
			conceptsService.setConceptsValidation(body);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@GetMapping(value = "/concept/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(operationId = "exportConcept", summary = "Blob of concept")
	public ResponseEntity<Object> exportConcept(@PathVariable(Constants.ID) String id, @HeaderParam("Accept") String acceptHeader) throws RmesException {
			return conceptsService.exportConcept(id, acceptHeader);
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR, Roles.SPRING_CONCEPT_CREATOR })
	@PostMapping("/concept/send/{id}")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Produces(MediaType.TEXT_PLAIN_VALUE)
	@Operation(operationId = "setConceptSend", summary = "Send concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class)))})	
	public ResponseEntity<Object> setConceptSend(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@RequestBody(description = "Mail informations", required = true) String body) throws RmesException {
		try {
			Boolean isSent = conceptsService.setConceptSend(id, body);
			logger.info("Send concept : {}" , id);
			return ResponseEntity.status(HttpStatus.OK).body(isSent);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR })
	@PostMapping("/collection")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollection", summary = "Create collection")
	public ResponseEntity<Object> setCollection(@RequestBody(description = "Collection", required = true) String body) {
		try {
			conceptsService.setCollection(body);
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
		}
		return ResponseEntity.noContent().build();
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR, Roles.SPRING_COLLECTION_CREATOR })
	@PutMapping("/collection/{id}")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionById", summary = "Update collection")
	public ResponseEntity<Object> setCollection(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@RequestBody(description = "Collection", required = true) String body) throws RmesException {
		try {
			conceptsService.setCollection(id, body);
			logger.info("Update collection : {}" , id);
			return ResponseEntity.noContent().build();
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_COLLECTION_CREATOR })
	@PutMapping("/collections/validate")
	@Consumes(MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionsValidation", summary = "Collections validation")
	public ResponseEntity<Object> setCollectionsValidation(
			@RequestBody(description = "Collection id array to validate", required = true) String body) throws RmesException {
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
	public ResponseEntity<Object> getCollectionExport(@PathVariable(Constants.ID) String id, @HeaderParam("Accept") String acceptHeader) throws RmesException {
			return conceptsService.getCollectionExport(id, acceptHeader);
	}

	@Secured({ Roles.SPRING_ADMIN, Roles.SPRING_CONCEPTS_CONTRIBUTOR, Roles.SPRING_COLLECTION_CREATOR })
	@PostMapping(value="/collection/send/{id}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	@Operation(operationId = "setCollectionSend", summary = "Send collection", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Boolean.class)))})	
	public ResponseEntity<Object> setCollectionSend(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@RequestBody(description = "Mail informations", required = true) String body) throws RmesException {
		try {
			Boolean isSent = conceptsService.setCollectionSend(id, body);
			logger.info("Send concept : {}" , id);
			return ResponseEntity.status(HttpStatus.OK).body(isSent);
		} catch (RmesException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

}
