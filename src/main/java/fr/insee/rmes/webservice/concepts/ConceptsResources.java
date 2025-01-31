package fr.insee.rmes.webservice.concepts;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.config.swagger.model.IdLabelAltLabel;
import fr.insee.rmes.config.swagger.model.concepts.*;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.PartialConcept;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/concepts")
@SecurityRequirement(name = "bearerAuth")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('concepts')")
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


public class ConceptsResources  {
	
	static final Logger logger = LoggerFactory.getLogger(ConceptsResources.class);
	
	final ConceptsService conceptsService;

	final ConceptsCollectionService conceptsCollectionService;

	public ConceptsResources(ConceptsService conceptsService, ConceptsCollectionService conceptsCollectionService) {
		this.conceptsService = conceptsService;
		this.conceptsCollectionService = conceptsCollectionService;
	}

	@GetMapping(value="", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabelAltLabel.class))))})																 
	public Collection<PartialConcept> getConcepts() throws RmesException {
		return conceptsService.getConcepts();
	}

	@GetMapping(value = "/linkedConcepts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getRelatedConcepts", summary = "List of concepts",
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})																 
	public ResponseEntity<Object> getRelatedConcepts(@PathVariable(Constants.ID) String id) throws RmesException {
		String concepts = conceptsService.getRelatedConcepts(id);
		return ResponseEntity.status(HttpStatus.OK).body(concepts);
	}
	
	
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).COLLECTION_CREATOR "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CREATOR)")
	@DeleteMapping(value="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "deleteConcept", summary = "Delete a concept")
	public ResponseEntity<Object> deleteConcept(@PathVariable(Constants.ID) String id) throws RmesException {
		conceptsService.deleteConcept(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(value = "/advanced-search", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsSearch", summary = "Rich list of concepts", 
	responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsSearch.class))))})																 
	public ResponseEntity<Object> getConceptsSearch() throws RmesException {
		String concepts = conceptsService.getConceptsSearch();
		return ResponseEntity.status(HttpStatus.OK).body(concepts);
	}

	@GetMapping(value = "/concept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptByID", summary = "Get a concept",
		responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptById.class)))})																 
	public ResponseEntity<Object> getConceptByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String concept = conceptsService.getConceptByID(id);
		return ResponseEntity.status(HttpStatus.OK).body(concept);
	}

	@GetMapping(value = "/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptsToValidate", summary = "List of concepts to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptsToValidate.class))))})
	public ResponseEntity<Object> getConceptsToValidate() throws RmesException {
		String concepts = conceptsService.getConceptsToValidate();
		return ResponseEntity.status(HttpStatus.OK).body(concepts);
	}

	@GetMapping(value = "/concept/{id}/links", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptLinksByID", summary = "List of linked concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=ConceptLinks.class))))})
	public ResponseEntity<Object> getConceptLinksByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String conceptLinks = conceptsService.getConceptLinksByID(id);
		return ResponseEntity.status(HttpStatus.OK).body(conceptLinks);
	}

	@GetMapping(value = "/concept/{id}/notes/{conceptVersion}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getConceptNotesByID", summary = "Last notes of the concept", 
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ConceptNotes.class)))})		
	public ResponseEntity<Object> getConceptNotesByID(@PathVariable(Constants.ID) String id, @PathVariable("conceptVersion") int conceptVersion) throws RmesException {
		String notes = conceptsService.getConceptNotesByID(id, conceptVersion);
		return ResponseEntity.status(HttpStatus.OK).body(notes);
	}


	@GetMapping(value = "/collections/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsDashboard", summary = "Rich list of collections", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=IdLabel.class))))})
	public ResponseEntity<Object> getCollectionsDashboard() throws RmesException {
		String collections = conceptsCollectionService.getCollectionsDashboard();
		return ResponseEntity.status(HttpStatus.OK).body(collections);
	}

	@GetMapping(value = "/collections/toValidate", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionsToValidate", summary = "List of collections to validate", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionsToValidate.class))))})
	public ResponseEntity<Object> getCollectionsToValidate() throws RmesException {
		String collections = conceptsService.getCollectionsToValidate();
		return ResponseEntity.status(HttpStatus.OK).body(collections);
	}

	@GetMapping(value = "/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionByID", summary = "Get a collection by its identifier",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = CollectionById.class)))})		
	public ResponseEntity<Object> getCollectionByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String collection = conceptsCollectionService.getCollectionByID(id);
		return ResponseEntity.status(HttpStatus.OK).body(collection);
	}

	@GetMapping(value = "/collection/{id}/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollectionMembersByID", summary = "List of collection member concepts", 
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation=CollectionMembers.class))))})
	public ResponseEntity<Object> getCollectionMembersByID(@PathVariable(Constants.ID) String id) throws RmesException {
		String collectionMembers = conceptsCollectionService.getCollectionMembersByID(id);
		return ResponseEntity.status(HttpStatus.OK).body(collectionMembers);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CONTRIBUTOR)")
	@PostMapping(value = "/concept", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConcept", summary = "Create concept" )
	public ResponseEntity<Object> setConcept(
			@Parameter(description = "Concept", required = true) @RequestBody String body) throws RmesException {
		String id = conceptsService.setConcept(body);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	//TODO Test with Roles.ADMIN, Roles.CONCEPT_CONTRIBUTOR (user stamp is contributor and user stamp is not contributor) : StampRestrictionsServiceImpl.isConceptManager
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CONTRIBUTOR)")
	@PutMapping(value="/concept/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptById", summary = "Update a concept")
	public ResponseEntity<Object> setConcept(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Concept", required = true) @RequestBody String body) throws RmesException {
		conceptsService.setConcept(id, body);
		logger.info("Concept updated : {}" , id);
		return ResponseEntity.noContent().build();
	}

	//TODO Test with admin and with concept_creator (user stamp is creator of all concepts, user stamp is not creator of one concept)
	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CREATOR)")
	@PutMapping(value= "/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setConceptsValidation", summary = "Concepts validation")
	public ResponseEntity<Object> setConceptsValidation(
			@Parameter(description = "Id, put '0' if multiple ids", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Concept ids", required = true) @RequestBody String body) throws RmesException {
		conceptsService.setConceptsValidation(body);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/collection/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(operationId = "getCollectionExport", summary = "Blob of collection")
	public ResponseEntity<?> getCollectionExport(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
		return conceptsService.getCollectionExport(id, accept);
	}

	@GetMapping(value = "/concept/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip" })
	@Operation(operationId = "exportConcept", summary = "Blob of concept")
	public ResponseEntity<?> exportConcept(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
		return conceptsService.exportConcept(id, accept);
	}

	@GetMapping(value = "/concept/export-zip/{id}/{type}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip" })
	@Operation(operationId = "exportConcept", summary = "Blob of concept")
	public void exportZipConcept(
			@PathVariable(Constants.ID) String id,
			@PathVariable("type") String type,
			@RequestParam("langue") ConceptsCollectionsResources.Language lg,
			@RequestHeader(required=false) String accept,
			@RequestParam("withConcepts") boolean withConcepts,
			HttpServletResponse response) throws RmesException {
		conceptsService.exportZipConcept(id, accept, response, lg, type, withConcepts);
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CONTRIBUTOR)")
	@PostMapping(value = "/collection", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollection", summary = "Create collection")
	public ResponseEntity<Object> setCollection(
			@Parameter(description = "Collection", required = true) @RequestBody String body) throws RmesException {
		conceptsService.setCollection(body);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).CONCEPT_CONTRIBUTOR "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).COLLECTION_CREATOR)")
	@PutMapping(value = "/collection/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionById", summary = "Update a collection")
	public ResponseEntity<Object> setCollection(
			@Parameter(description = "Id", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Collection", required = true) @RequestBody String body) throws RmesException {
		conceptsService.setCollection(id, body);
		logger.info("Update collection : {}" , id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN "
			+ ", T(fr.insee.rmes.config.auth.roles.Roles).COLLECTION_CREATOR)")	
	@PutMapping(value= "/collections/{id}/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCollectionsValidation", summary = "Collections validation")
	public ResponseEntity<Object> setCollectionsValidation(
			@Parameter(description = "Id, put '0' if multiple ids", required = true) @PathVariable(Constants.ID) String id,
			@Parameter(description = "Collection id array to validate", required = true) @RequestBody String body) throws RmesException {
		conceptsService.setCollectionsValidation(body);
		logger.info("Validated concepts : {}" , body);
		return ResponseEntity.noContent().build();
	}
}
