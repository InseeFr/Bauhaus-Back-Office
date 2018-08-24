package fr.insee.rmes.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.swagger.model.codeList.CodeLabelList;
import fr.insee.rmes.config.swagger.model.codeList.CodeList;
import fr.insee.rmes.persistance.service.CodeListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("/codeList")
@Tag(name="Codes lists", description="Codes list API")
@ApiResponses(value = { 
		@ApiResponse(responseCode = "200", description = "Success"), 
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request"), 
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden"), 
		@ApiResponse(responseCode = "404", description = "Not found"),
		@ApiResponse(responseCode = "406", description = "Not Acceptable"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class CodeListsResources {

	final static Logger logger = LogManager.getLogger(CodeListsResources.class);

	@Autowired
	CodeListService codeListService;


	@GET
	@Path("/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeListByNotation", summary = "List of codes", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getCodeListByNotation(@PathParam("notation") String notation) {
		String jsonResultat = codeListService.getCodeList(notation);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/{notation}/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
	public Response getCodeByNotation(@PathParam("notation") String notation, @PathParam("code") String code) {
		String jsonResultat = codeListService.getCode(notation, code);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
}
