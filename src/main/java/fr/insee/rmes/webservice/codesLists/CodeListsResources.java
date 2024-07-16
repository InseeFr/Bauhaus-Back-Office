package fr.insee.rmes.webservice.codesLists;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.code_list.CodeList;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeListResponse;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.Deserializer;
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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/codeList")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Code lists", description = "Code list API")
@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "204", description = "No Content"), @ApiResponse(responseCode = "400", description = "Bad Request"), @ApiResponse(responseCode = "401", description = "Unauthorized"), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "406", description = "Not Acceptable"), @ApiResponse(responseCode = "500", description = "Internal server error")})
public class CodeListsResources extends GenericResources {

    private final CodeListService codeListService;

    @Autowired
    public CodeListsResources(CodeListService codeListService) {
        this.codeListService = codeListService;
    }

    @PreAuthorize("isAdmin() || isCodesListContributor(#body)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setCodesList", summary = "Create a code list")
    public ResponseEntity<CodeListResponse> setCodesList(@Parameter(description = "Code List", required = true) @RequestBody String body) throws RmesException {
        String id = codeListService.setCodesList(body, false);
        CodeListResponse codeListResponse = new CodeListResponse(id);
        return ResponseEntity.status(HttpStatus.OK).body(codeListResponse);

    }


    @PreAuthorize("isAdmin() || isContributorOfCodesList(#codesListId)")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "setCodesList", summary = "Update a code list")
    public ResponseEntity<CodeListResponse> updateCodesList(@PathVariable(Constants.ID) @P("codesListId") String id, @Parameter(description = "Code list", required = true) @RequestBody String body) throws RmesException {
        id = codeListService.setCodesList(id, body, false);
        CodeListResponse codeListResponse = new CodeListResponse(id);
        return ResponseEntity.status(HttpStatus.OK).body(codeListResponse);
    }


    @PreAuthorize("isAdmin() || isContributorOfCodesList(#codesListId)")
    @DeleteMapping(value = "/{id}")
    @Operation(operationId = "deleteCodeList", summary = "Delete a code list")
    public ResponseEntity<Void> deleteCodeList(@PathVariable(Constants.ID) @P("codesListId") String notation) throws RmesException {
            codeListService.deleteCodeList(notation, false);
            return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getAllCodesLists", summary = "Get all code lists", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeListResponse.class)))})
    public ResponseEntity<List<CodeListResponse>> getAllCodesLists() throws RmesException, JsonProcessingException {
        List<CodeListResponse> listCodeList = codeListService.getAllCodesLists(false);
        return ResponseEntity.status(HttpStatus.OK).body(listCodeList);
    }



    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedCodesListForSearch", summary = "Return all lists for Advanced Search", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListResponse.class)))})
    public ResponseEntity<List<CodeListResponse>> getDetailedCodesLisForSearch() throws RmesException, JsonProcessingException {
        List<CodeListResponse> listCodeList = codeListService.getDetailedCodesListForSearch(false);
        return ResponseEntity.status(HttpStatus.OK).body(listCodeList);

    }


    @GetMapping(value = "/detailed/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getDetailedCodesListByNotation", summary = "Get a code list", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListResponse.class)))})
    public ResponseEntity<CodeListResponse> getDetailedCodesListByNotation(@PathVariable("notation") String notation) throws RmesException {
        CodeListResponse codeListResponse = codeListService.getDetailedCodesList(notation, false);

        return ResponseEntity.status(HttpStatus.OK).body(codeListResponse);
    }


    @GetMapping(value = "/detailed/{notation}/codes")
    @Operation(
            operationId = "getPaginatedCodesForCodeList",
            summary = "List of codes",
            responses = {
                    @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))
            })
    public ResponseEntity<CodeList> getPaginatedCodesForCodeList(
            @PathVariable("notation") String notation,
            @RequestParam(value = "search", required = false) List<String> search,
            @RequestParam("page") int page,
            @RequestParam(value = "per_page", required = false) Integer perPage,
            @RequestParam(value = "sort", required = false) String sort) throws RmesException {
        CodeList codeList = codeListService.getCodesForCodeList(notation, search, page, perPage, sort);
        return ResponseEntity.status(HttpStatus.OK).body(codeList);
    }

    @PreAuthorize("isAdmin()")
    @DeleteMapping(value = "/detailed/{notation}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getPaginatedCodesForCodeList", summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListResponse.class)))})
    public ResponseEntity<Void> deleteCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        codeListService.deleteCodeFromCodeList(notation, code);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PreAuthorize("isAdmin() || isContributorOfCodesList(#notation)")
    @PutMapping(value = "/detailed/{notation}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateCodeForCodeList", summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListItem.class)))})
    public ResponseEntity<CodeListItem> updateCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code, @Parameter(description = "Code", required = true) @RequestBody String body) throws RmesException {
        String id = codeListService.updateCodeFromCodeList(notation, code, body);
        CodeListItem idCodeListItem = new CodeListItem(id);
        return ResponseEntity.status(HttpStatus.OK).body(idCodeListItem);

    }

    @PreAuthorize("isAdmin() || isContributorOfCodesList(#notation)")
    @PostMapping(value = "/detailed/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "addCodeForCodeList", summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListResponse.class)))})
    public ResponseEntity<CodeListItem> addCodeForCodeList(@PathVariable("notation") String notation, @Parameter(description = "Code", required = true) @RequestBody String body) throws RmesException {
        String id = codeListService.addCodeFromCodeList(notation, body);
        CodeListItem idCodeListItem = new CodeListItem(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(idCodeListItem);
    }

    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodeListByNotation", summary = "Get a code list", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListResponse.class)))})
    public ResponseEntity<CodeListResponse> getCodeListByNotation(@PathVariable("notation") String notation) throws RmesException {
            String codeListJson = codeListService.getCodeListJson(notation);
            CodeListResponse codeList=Deserializer.deserializeBody(codeListJson, CodeListResponse.class);
            return ResponseEntity.status(HttpStatus.OK).body(codeList);
    }

    @GetMapping(value = "/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodesForCodeList", summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<CodeList> getCodesForCodeList(@PathVariable("notation") String notation, @RequestParam("page") int page, @RequestParam(value = "per_page", required = false) Integer perPage) throws RmesException {
        String codeListCodesJson = codeListService.getCodesJson(notation, page, perPage);
        CodeList codeListCodes=Deserializer.deserializeBody(codeListCodesJson, CodeList.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeListCodes);

    }

    @GetMapping(value = "/{notation}/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getCodeByNotation", summary = "Code, labels and code list's notation", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
    public ResponseEntity<CodeLabelList> getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        String codeLabelListJson = codeListService.getCode(notation, code);
        CodeLabelList codeLabelList=Deserializer.deserializeBody(codeLabelListJson, CodeLabelList.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeLabelList);
    }


    @PreAuthorize("isAdmin() || isContributorOfCodesList(#id)")
    @PutMapping("/validate/{id}")
    @Operation(operationId = "publishFullCodeList", summary = "Publish a codelist")
    public ResponseEntity<Object> publishFullCodeList(@PathVariable(Constants.ID) String id) throws RmesException {
        codeListService.publishCodeList(id, false);
        CodeListResponse codeListResponseId = new CodeListResponse(id);
        return ResponseEntity.status(HttpStatus.OK).body(codeListResponseId);
    }

}
