package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.IdLabel;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/concepts-collections")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="ConceptsCollections", description="Concept Collections API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class ConceptsCollectionsResources extends GenericResources   {
	
	static final Logger logger = LogManager.getLogger(ConceptsCollectionsResources.class);

	public enum Language {
		lg1, lg2;
	}

	@Autowired
	ConceptsService conceptsService;

	@Autowired
	ConceptsCollectionService conceptsCollectionService;

	@GetMapping(value = "/collections", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCollections", summary = "List of collections",
			responses = {@ApiResponse(content=@Content(array=@ArraySchema(schema=@Schema(implementation= IdLabel.class))))})
	public ResponseEntity<Object> getCollections() {
		try {
			String collections = conceptsCollectionService.getCollections();
			return ResponseEntity.status(HttpStatus.OK).body(collections);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value = "/export/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(operationId = "getCollectionExport", summary = "Blob of collection")
	public ResponseEntity<?> getCollectionExport(@PathVariable(Constants.ID) String id, @RequestHeader(required=false) String accept) throws RmesException {
			return conceptsService.getCollectionExport(id, accept);
	}


	@GetMapping(value = "/export-zip/{id}/{type}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/zip" })
	@Operation(operationId = "exportZipCollectionODT", summary = "Blob of concept")
	public void exportZipCollectionODT(
			@PathVariable(Constants.ID) String id,
			@PathVariable("type") String type,
			@RequestParam("langue") Language lg,
			@RequestHeader(required=false) String accept,
			@RequestParam("withConcepts") boolean withConcepts,
			HttpServletResponse response) throws RmesException {
		conceptsCollectionService.exportZipCollection(id, accept, response, lg, type, withConcepts);
	}

	@GetMapping(value = "/export/{id}/{type}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/vnd.oasis.opendocument.text" })
	@Operation(operationId = "getCollectionExport", summary = "Blob of collection")
	public ResponseEntity<?> getCollectionExport(
			@PathVariable(Constants.ID) String id,
			@PathVariable("type") String type,
			@RequestParam("langue") Language lg,
			@RequestParam("withConcepts") boolean withConcepts,
			@RequestHeader(required=false) String accept,
			HttpServletResponse response)
			throws RmesException {

		if("ods".equalsIgnoreCase(type)){
			return conceptsCollectionService.getCollectionExportODS(id, accept, withConcepts, response);
		}
		return conceptsCollectionService.getCollectionExportODT(id, accept, lg, withConcepts, response);

	}
}
