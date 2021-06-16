package fr.insee.rmes.webservice;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.insee.rmes.bauhaus_services.Constants;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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

	static final Logger logger = LogManager.getLogger(CodeListsResources.class);

	@Autowired
	CodeListService codeListService;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public Response setCodesList(@RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public Response updateCodesList(@PathParam(Constants.ID) String componentId, @RequestBody(description = "Code List", required = true) String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(id, body);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(id).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getAllCodesLists", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
	public Response getallCodesLists() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getAllCodesLists();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedCodesListForSearch", summary = "Return all lists for Advanced Search",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedCodesLisForSearch() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesListForSearch();
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/detailed/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDetailedCodesListByNotation", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getDetailedCodesListByNotation(@PathParam("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesList(notation);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeListByNotation", summary = "List of codes", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public Response getCodeListByNotation(@PathParam("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCodeListJson(notation);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}



	@GET
	@Path("/{notation}/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
	public Response getCodeByNotation(@PathParam("notation") String notation, @PathParam("code") String code) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCode(notation, code);
		} catch (RmesException e) {
			return Response.status(e.getStatus()).entity(e.getDetails()).type(TEXT_PLAIN).build();
		}
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}


}
