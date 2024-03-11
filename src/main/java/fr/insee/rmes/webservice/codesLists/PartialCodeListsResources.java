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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/codeList/partial")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Partial Codes lists", description = "Partial Codes list API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "204", description = "No Content"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "406", description = "Not Acceptable"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
public class PartialCodeListsResources extends GenericResources {

    @Autowired
    CodeListService codeListService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getAllPartialCodesLists", summary = "Partial List of codes",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
    public ResponseEntity<Object> getAllPartialCodesLists() {
        try {
            String body = codeListService.getAllCodesLists(true);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedPartialCodesListByNotation", summary = "Get a partial list of code",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getDetailedPartialCodesListByNotation(@PathVariable("notation") String notation) {
        try {
            String body = codeListService.getDetailedCodesList(notation, true);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/parent/{parentCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getPartialsByParent", summary = "Get partials by Parent IRI",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
    public ResponseEntity<Object> getPartialsByParent(@PathVariable("parentCode") String parentIri) {
        try {
            String codesLists = codeListService.getPartialCodeListByParent(parentIri);
            return ResponseEntity.status(HttpStatus.OK).body(codesLists);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("@AuthorizeMethodDecider.isAdmin()")
    @PutMapping("/validate/{id}")
    @io.swagger.v3.oas.annotations.Operation(operationId = "publishPartialCodeList", summary = "Publish a partial codelist")
    public ResponseEntity<Object> publishPartialCodeList(
            @PathVariable(Constants.ID) String id) {
        try {
            codeListService.publishCodeList(id, true);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedPartialCodesLisForSearch", summary = "Return all lists for Advanced Search",
            responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Object> getDetailedPartialCodesLisForSearch() {
        try {
            String body = codeListService.getDetailedCodesListForSearch(true);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createPartialCodeList", summary = "Create a codes list")
    public ResponseEntity<Object> createPartialCodeList(
            @Parameter(description = "Code List", required = true) @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(body, true);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setCodesList", summary = "Create a codes list")
    public ResponseEntity<Object> updatePartialCodeList(
            @PathVariable(Constants.ID) String componentId,
            @Parameter(description = "Code List", required = true) @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(componentId, body, true);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @PreAuthorize("hasAnyRole(T(fr.insee.rmes.config.auth.roles.Roles).ADMIN)")
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "deletePartialCodeList", summary = "Delete a partial codes list")
    public ResponseEntity<Object> deletePartialCodeList(@PathVariable(Constants.ID) String notation) {
        try {
            codeListService.deleteCodeList(notation, true);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }
}