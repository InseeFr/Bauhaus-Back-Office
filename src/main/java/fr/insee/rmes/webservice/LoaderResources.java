package fr.insee.rmes.webservice;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * WebService class to download/upload resources in databases
 * 
 *         schemes: - http
 * 
 *         consumes: - application/json
 * 
 *         produces: - application/json
 *
 */
@RestController
@RequestMapping("/loader")
@SecurityRequirement(name = "bearerAuth")
@Tag(name="Loader", description="User Management")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class LoaderResources  extends GenericResources {

	static final Logger logger = LogManager.getLogger(LoaderResources.class);


	@Autowired
	protected RepositoryGestion repoGestion;
	
	
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")	
	@Operation(operationId = "uploadFile", summary = "Upload a ttl or trig file in database"  )
	@PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@RequestBody(content = @Content(encoding = @Encoding(name = "database", contentType = "text/plain")))
	public ResponseEntity<Object> saveDocument(
			@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {"gestion","diffusion"},type = "string")) 
				@RequestPart(value = "database") final String database,
			@RequestPart(value = "graph", required = false)  final String graph,
			@RequestPart(value = "file")  final MultipartFile file) {

		try {
			checkDatabase(database);
			uploadFile(file, graph, database);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	private void checkDatabase(String database) throws RmesException {
		if (database == null)  throw new RmesException(HttpStatus.BAD_REQUEST,"Database is missing", "Database is null");
		if (!database.equals("gestion")&&!database.equals("diffusion")) throw new RmesException(HttpStatus.BAD_REQUEST,"Database is unknown : "+ database, "Database is "+database);
	}

	public void uploadFile(MultipartFile trigFile, String graph, String database) throws RmesException {
		String documentName = trigFile.getName();
		try (InputStream content = trigFile.getInputStream()){
			RDFFormat format = Rio.getParserFormatForFileName(trigFile.getOriginalFilename()).orElse(RDFFormat.TRIG);
			if (database.equals("gestion")) {
				repoGestion.persistFile(content, format, graph);
			}else {
				RepositoryPublication.persistFile(content, format,graph);
			}
		}  catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR,"IOException : file " + documentName, e.getMessage());
		}	
	}
	
}