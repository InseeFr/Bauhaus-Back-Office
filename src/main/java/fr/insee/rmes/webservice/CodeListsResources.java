package fr.insee.rmes.webservice;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/codeList")
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

	static final Logger logger = LogManager.getLogger(CodeListsResources.class);

	@Autowired
	CodeListService codeListService;

	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public Response setCodesList(@RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(body, false);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@PutMapping("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public Response updateCodesList(@PathVariable(Constants.ID) String componentId, @RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(id, body, false);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@PostMapping("/partial")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "createPartialCodeList", summary = "Create a codes list")
	public Response createPartialCodeList(@RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(body, true);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@PutMapping("/partial/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public Response updatePartialCodeList(@PathVariable(Constants.ID) String componentId, @RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(id, body, true);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@GetMapping("/codeslists")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getAllCodesLists", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
	public Response getallCodesLists() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getAllCodesLists(false);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/partial")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getallPartialCodesLists", summary = "Partial List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
	public Response getallPartialCodesLists() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getAllCodesLists(true);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedCodesListForSearch", summary = "Return all lists for Advanced Search",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedCodesLisForSearch() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesListForSearch(false);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/partial/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedPartialCodesLisForSearch", summary = "Return all lists for Advanced Search",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedPartialCodesLisForSearch() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesListForSearch(true);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/detailed/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedCodesListByNotation", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedCodesListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesList(notation, false);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/partial/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedPartialCodesListByNotation", summary = "Get a partial list of code",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedPartialCodesListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesList(notation, true);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeListByNotation", summary = "List of codes", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getCodeListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCodeListJson(notation);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GetMapping("/{notation}/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
	public Response getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCode(notation, code);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


}
