package fr.insee.rmes.webservice.codesLists;


import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.GenericResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/codeList")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Codes lists", description = "Codes list API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class CodeListsResources extends GenericResources {

    @Autowired
    CodeListService codeListService;

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setCodesList", summary = "Create a codes list")
    public ResponseEntity<Object> setCodesList(
            @Parameter(description = "Code List", required = true) @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(body, false);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setCodesList", summary = "Create a codes list")
    public ResponseEntity<Object> updateCodesList(
            @PathVariable(Constants.ID) String componentId,
            @Parameter(description = "Code List", required = true) @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(componentId, body, false);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }


    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "deleteCodeList", summary = "Delete a codes list")
    public ResponseEntity<Object> deleteCodeList(@PathVariable(Constants.ID) String notation) {
        try {
            codeListService.deleteCodeList(notation, false);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getAllCodesLists", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
    public ResponseEntity<Object> getAllCodesLists() {
        try {
            String body = codeListService.getAllCodesLists(false);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedCodesListForSearch", summary = "Return all lists for Advanced Search",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getDetailedCodesLisForSearch() {
        try {
            String body = codeListService.getDetailedCodesListForSearch(false);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/detailed/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedCodesListByNotation", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getDetailedCodesListByNotation(@PathVariable("notation") String notation) {
        try {
            String body = codeListService.getDetailedCodesList(notation, false);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/detailed/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getPaginatedCodesForCodeList", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getPaginatedCodesForCodeList(@PathVariable("notation") String notation, @RequestParam("page") int page, @RequestParam(value = "per_page", required = false) Integer perPage) {
        try {
            String body = codeListService.getCodesForCodeList(notation, page, perPage);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @DeleteMapping(value = "/detailed/{notation}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getPaginatedCodesForCodeList", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> deleteCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code) {
        try {
            String body = codeListService.deleteCodeFromCodeList(notation, code);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping(value = "/detailed/{notation}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateCodeForCodeList", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> updateCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code, @Parameter(description = "Code", required = true) @RequestBody String body) {
        try {
            String response = codeListService.updateCodeFromCodeList(notation, code, body);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(value = "/detailed/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addCodeForCodeList", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> addCodeForCodeList(@PathVariable("notation") String notation,  @Parameter(description = "Code", required = true) @RequestBody String body) {
        try {
            String response = codeListService.addCodeFromCodeList(notation, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodeListByNotation", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getCodeListByNotation(@PathVariable("notation") String notation) {
        try {
            String body = codeListService.getCodeListJson(notation);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodesForCodeList", summary = "List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getCodesForCodeList(@PathVariable("notation") String notation, @RequestParam("page") int page, @RequestParam(value = "per_page", required = false) Integer perPage) {
        try {
            String body = codeListService.getCodesJson(notation, page, perPage);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }



    @GetMapping(value = "/{notation}/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
    public ResponseEntity<Object> getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) {
        try {
            String body = codeListService.getCode(notation, code);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping("/validate/{id}")
    @io.swagger.v3.oas.annotations.Operation(operationId = "publishFullCodeList", summary = "Publish a codelist")
    public ResponseEntity<Object> publishFullCodeList(
            @PathVariable(Constants.ID) String id) {
        try {
            codeListService.publishCodeList(id, false);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }
}
