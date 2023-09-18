package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
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
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

	private static final String DIFFUSION = "diffusion";


	private static final String GESTION = "gestion";


	static final Logger logger = LoggerFactory.getLogger(LoaderResources.class);


	@Autowired
	protected RepositoryGestion repoGestion;

	@Autowired
	protected RepositoryPublication repositoryPublication;
	
	
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")	
	@Operation(operationId = "uploadFile", summary = "Upload a ttl or trig file in database"  )
	@PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@RequestBody(content = @Content(encoding = @Encoding(name = "database", contentType = "text/plain")))
	public ResponseEntity<Object> UploadRdf(
			@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {GESTION,DIFFUSION},type = "string")) 
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


	
	@GetMapping(value = "/download/graph", produces = "*/*")
	@Operation(operationId = "downloadGraph", summary = "Download the Graph")																 
	public ResponseEntity<Object> downloadDocument(
			@RequestBody String urlGraph,
			@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {GESTION,DIFFUSION},type = "string")) 
				@RequestBody String database) {
		try {
			checkDatabase(database);
			return downloadFile(urlGraph, database);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	@PreAuthorize("@AuthorizeMethodDecider.isAdmin() ")	
	@GetMapping(value = "/download/graphs", produces = "*/*")
	@Operation(operationId = "downloadGraphs", summary = "Download all graphs in a zip file")																 
	public ResponseEntity<Object> downloadDocument(
			@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {GESTION,DIFFUSION},type = "string")) 
				@RequestBody String database) {
		try {
			checkDatabase(database);
			return downloadFile(null, database);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}
	
	
	@GetMapping(value = "/graphs", produces = "*/*")
	@Operation(operationId = "getAllGraphs", summary = "Get the list of all graphs in database")																 
	public ResponseEntity<String> getAllGraphs(
			@Parameter(description = "Database", schema = @Schema(nullable = true, allowableValues = {GESTION,DIFFUSION},type = "string")) 
				@RequestBody String database) {
		String[] graphs;
		try {
			checkDatabase(database);
			if (database.equals(GESTION)) {
				graphs = repoGestion.getAllGraphs();
			}else {
				graphs = repositoryPublication.getAllGraphs();
			}
			return ResponseEntity.ok(String.join("\n", graphs));
		} catch (RmesException e) {
			return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());		}
	}
	
	
	private void checkDatabase(String database) throws RmesException {
		if (database == null)  throw new RmesException(HttpStatus.BAD_REQUEST,"Database is missing", "Database is null");
		if (!database.equals(GESTION)&&!database.equals(DIFFUSION)) throw new RmesException(HttpStatus.BAD_REQUEST,"Database is unknown : "+ database, "Database is "+database);
	}

	private void uploadFile(MultipartFile trigFile, String graph, String database) throws RmesException {
		String documentName = trigFile.getName();
		try (InputStream content = trigFile.getInputStream()){
			RDFFormat format = Rio.getParserFormatForFileName(trigFile.getOriginalFilename()).orElse(RDFFormat.TRIG);
			if (database.equals(GESTION)) {
				repoGestion.persistFile(content, format, graph);
			}else {
				repositoryPublication.persistFile(content, format,graph);
			}
		}  catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR,"IOException : file " + documentName, e.getMessage());
		}	
	}

	
	private ResponseEntity<Object> downloadFile(String graph, String database) throws RmesException{
		File outFile ;
		if (database.equals(GESTION)) {
			outFile = repoGestion.getGraphAsFile(graph);
		}else {
			outFile = repositoryPublication.getGraphAsFile(graph);
		}

		//Build Headers
		String fileName = graph == null ? database.concat(".zip") : graph.replace(RdfUtils.getBaseGraph(),"").replace("/","_").concat(".trig");
		ContentDisposition content = ContentDisposition.builder("attachement").filename(fileName).build();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentDisposition(content);
		
		//Get document as resource
		Resource resource = new FileSystemResource(outFile);
		
		//return the response with document
		try {
			return ResponseEntity.ok()
						.headers(responseHeaders)
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.body(resource);			
		 } catch ( Exception e ) { 
         	logger.error(e.getMessage());
         	throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "Error downloading file"); 
         }
		
	}
	
}