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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Component
@Path("/codeList")
@Api(value = "Code list API", tags = { "Code list" })
@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 204, message = "No Content"),
		@ApiResponse(code = 400, message = "Bad Request"), @ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"), @ApiResponse(code = 404, message = "Not found"),
		@ApiResponse(code = 406, message = "Not Acceptable"),
		@ApiResponse(code = 500, message = "Internal server error") })
public class CodeListsResources {

	final static Logger logger = LogManager.getLogger(CodeListsResources.class);

	@Autowired
	CodeListService codeListService;


	@GET
	@Path("/{notation}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCodeListByNotation", value = "List of codes", response = CodeList.class)
	public Response getCodeListByNotation(@PathParam("notation") String notation) {
		String jsonResultat = codeListService.getCodeList(notation);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}

	@GET
	@Path("/{notation}/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "getCodeByNotation", value = "Code, labels and code list's notation", response = CodeLabelList.class)
	public Response getCodeByNotation(@PathParam("notation") String notation, @PathParam("code") String code) {
		String jsonResultat = codeListService.getCode(notation, code);
		return Response.status(HttpStatus.SC_OK).entity(jsonResultat).build();
	}
}
