package fr.insee.rmes.webservice;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.exceptions.RmesException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value="/codeList")
@SecurityRequirement(name = "bearerAuth")
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
public class CodeListsResources extends GenericResources  {

	static final Logger logger = LogManager.getLogger(CodeListsResources.class);

	@Autowired
	CodeListService codeListService;

	@PostMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public ResponseEntity<Object> setCodesList(
			@Parameter(description = "Code List", required = true) @RequestBody String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(body, false);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PutMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public ResponseEntity<Object> updateCodesList(
			@PathVariable(Constants.ID) String componentId, 
			@Parameter(description = "Code List", required = true) @RequestBody String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(id, body, false);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "deleteCodeList", summary = "Delete a codes list")
	public ResponseEntity<Object> deleteCodeList(@PathVariable(Constants.ID) String notation) {
		try {
			codeListService.deleteCodeList(notation, false);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@PostMapping(value="/partial", consumes=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "createPartialCodeList", summary = "Create a codes list")
	public ResponseEntity<Object> createPartialCodeList(
			@Parameter(description = "Code List", required = true) @RequestBody String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(body, true);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}

	@PutMapping(value="/partial/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "setCodesList", summary = "Create a codes list")
	public ResponseEntity<Object> updatePartialCodeList(
			@PathVariable(Constants.ID) String componentId, 
			@Parameter(description = "Code List", required = true) @RequestBody String body) {
		String id = null;
		try {
			id = codeListService.setCodesList(id, body, true);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}


	@DeleteMapping(value = "/partial/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "deletePartialCodeList", summary = "Delete a partial codes list")
	public ResponseEntity<Object> deletePartialCodeList(@PathVariable(Constants.ID) String notation) {
		try {
			codeListService.deleteCodeList(notation, true);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (RmesException e) {
			return returnRmesException(e);		}
	}

	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getAllCodesLists", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
	public ResponseEntity<Object> getallCodesLists() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getAllCodesLists(false);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/partial", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getallPartialCodesLists", summary = "Partial List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
	public ResponseEntity<Object> getallPartialCodesLists() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getAllCodesLists(true);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/search", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getDetailedCodesListForSearch", summary = "Return all lists for Advanced Search",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public ResponseEntity<Object> getDetailedCodesLisForSearch() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesListForSearch(false);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/partial/search", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getDetailedPartialCodesLisForSearch", summary = "Return all lists for Advanced Search",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public ResponseEntity<Object> getDetailedPartialCodesLisForSearch() {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesListForSearch(true);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/detailed/{notation}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getDetailedCodesListByNotation", summary = "List of codes",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public ResponseEntity<Object> getDetailedCodesListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesList(notation, false);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/partial/{notation}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getDetailedPartialCodesListByNotation", summary = "Get a partial list of code",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public ResponseEntity<Object> getDetailedPartialCodesListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getDetailedCodesList(notation, true);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/{notation}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCodeListByNotation", summary = "List of codes", 
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
	public ResponseEntity<Object> getCodeListByNotation(@PathVariable("notation") String notation) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCodeListJson(notation);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}

	@GetMapping(value="/partials/{parentCode}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getPartialsByParent", summary = "Get partials by Parent IRI",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
	public ResponseEntity<Object> getPartialsByParent(@PathVariable("parentCode") String parentIri) {
		try {
			String codesLists = codeListService.getPartialCodeListByParent(parentIri);
			return ResponseEntity.status(HttpStatus.OK).body(codesLists);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
	}

	@GetMapping(value="/{notation}/code/{code}", produces=MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation",
			responses = { @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
	public ResponseEntity<Object> getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) {
		String jsonResultat;
		try {
			jsonResultat = codeListService.getCode(notation, code);
		} catch (RmesException e) {
			return returnRmesException(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body(jsonResultat);
	}


}
