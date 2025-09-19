package fr.insee.rmes.onion.infrastructure.webservice.codes_lists;


import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;
import fr.insee.rmes.bauhaus_services.code_list.DetailedCodeList;
import fr.insee.rmes.config.swagger.model.Id;
import fr.insee.rmes.config.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.config.swagger.model.code_list.CodeList;
import fr.insee.rmes.config.swagger.model.code_list.Page;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.codeslists.PartialCodesList;
import fr.insee.rmes.onion.infrastructure.webservice.GenericResources;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.utils.Deserializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/codeList")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Code lists", description = "Code list API")
@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "204", description = "No Content"), @ApiResponse(responseCode = "400", description = "Bad Request"), @ApiResponse(responseCode = "401", description = "Unauthorized"), @ApiResponse(responseCode = "403", description = "Forbidden"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "406", description = "Not Acceptable"), @ApiResponse(responseCode = "500", description = "Internal server error")})
public class CodeListsResources extends GenericResources {

    private final CodeListService codeListService;

    public CodeListsResources(CodeListService codeListService) {
        this.codeListService = codeListService;
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a code list")
    public ResponseEntity<String> setCodesList(@Parameter(description = "Code List", required = true) @RequestBody String body) throws RmesException {
        String id = codeListService.setCodesList(body, false);
        return ResponseEntity.status(HttpStatus.OK).body(id);

    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a code list")
    public ResponseEntity<String> updateCodesList(@PathVariable(Constants.ID) String id, @Parameter(description = "Code list", required = true) @RequestBody String body) throws RmesException {
        codeListService.setCodesList(id, body, false);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a code list")
    public ResponseEntity<Void> deleteCodeList(@PathVariable(Constants.ID) String id) throws RmesException {
        codeListService.deleteCodeList(id, false);
            return ResponseEntity.status(HttpStatus.OK).build();
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all code lists", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = CodeList.class)))})
    public List<PartialCodesList> getAllCodesLists() throws RmesException, JsonProcessingException {
        return codeListService.getAllCodesLists(false);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return all lists for Advanced Search", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<List<CodeList>> getDetailedCodesLisForSearch() throws RmesException, JsonProcessingException {
        List<CodeList> listCodeList = codeListService.getDetailedCodesListForSearch(false);
        return ResponseEntity.status(HttpStatus.OK).body(listCodeList);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/detailed/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a code list", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<CodeList> getDetailedCodesListByNotation(@PathVariable("notation") String notation) throws RmesException {
        CodeList codeListResponse = codeListService.getDetailedCodesList(notation);
        return ResponseEntity.status(HttpStatus.OK).body(codeListResponse);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/detailed/{notation}/codes")
    @Operation(
            summary = "List of codes",
            responses = {
                    @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailedCodeList.class)))
            })
    public ResponseEntity<Page> getPaginatedCodesForCodeList(
            @PathVariable("notation") String notation,
            @RequestParam(value = "search", required = false) List<String> search,
            @RequestParam("page") int page,
            @RequestParam(value = "per_page", required = false) Integer perPage,
            @RequestParam(value = "sort", required = false) String sort) throws RmesException {
        Page codeList = codeListService.getCodesForCodeList(notation, search, page, perPage, sort);
        return ResponseEntity.status(HttpStatus.OK).body(codeList);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/detailed/{notation}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<Void> deleteCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        codeListService.deleteCodeFromCodeList(notation, code);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/detailed/{id}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeListItem.class)))})
    public ResponseEntity<CodeListItem> updateCodeForCodeList(@PathVariable("id") String id, @PathVariable("code") String code, @Parameter(description = "Code", required = true) @RequestBody String body) throws RmesException {
        String response = codeListService.updateCodeFromCodeList(id, code, body);
        CodeListItem idCodeListItem = new CodeListItem(response);
        return ResponseEntity.status(HttpStatus.OK).body(idCodeListItem);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(value = "/detailed/{id}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<CodeListItem> addCodeForCodeList(@PathVariable("id") String id, @Parameter(description = "Code", required = true) @RequestBody String body) throws RmesException {
        String response = codeListService.addCodeFromCodeList(id, body);
        CodeListItem idCodeListItem = new CodeListItem(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(idCodeListItem);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a code list", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeList.class)))})
    public ResponseEntity<CodeList> getCodeListByNotation(@PathVariable("notation") String notation) throws RmesException {
            String codeListJson = codeListService.getCodeListJson(notation);
            CodeList codeList=Deserializer.deserializeJsonString(codeListJson, CodeList.class);
            return ResponseEntity.status(HttpStatus.OK).body(codeList);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List of codes", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailedCodeList.class)))})
    public ResponseEntity<Page> getCodesForCodeList(@PathVariable("notation") String notation, @RequestParam("page") int page, @RequestParam(value = "per_page", required = false) Integer perPage) throws RmesException {
        String codeListCodesJson = codeListService.getCodesJson(notation, page, perPage);
        Page codeListCodes=Deserializer.deserializeJsonString(codeListCodesJson, Page.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeListCodes);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Code, labels and code list's notation", responses = {@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = CodeLabelList.class)))})
    public ResponseEntity<CodeLabelList> getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        String codeLabelListJson = codeListService.getCode(notation, code);
        CodeLabelList codeLabelList=Deserializer.deserializeJsonString(codeLabelListJson, CodeLabelList.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeLabelList);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping("/{id}/validate")
    @Operation(summary = "Publish a codelist")
    public ResponseEntity<Id> publishFullCodeList(@PathVariable(Constants.ID) Id id) throws RmesException {
        codeListService.publishCodeList(id.identifier(), false);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
