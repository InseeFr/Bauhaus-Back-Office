package fr.insee.rmes.modules.codeslists.codeslists.webservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;
import fr.insee.rmes.bauhaus_services.code_list.DetailedCodeList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.Id;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.Page;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.model.PartialCodesList;
import fr.insee.rmes.modules.commons.webservice.GenericResources;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.utils.Deserializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/codeList")
public class CodesListsResources extends GenericResources {

    private final CodeListService codeListService;

    public CodesListsResources(CodeListService codeListService) {
        this.codeListService = codeListService;
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> setCodesList(@RequestBody String body) throws RmesException {
        String id = codeListService.setCodesList(body, false);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(id);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateCodesList(@PathVariable(Constants.ID) String id, @RequestBody String body) throws RmesException {
        codeListService.setCodesList(id, body, false);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteCodeList(@PathVariable(Constants.ID) String id) throws RmesException {
        codeListService.deleteCodeList(id, false);
            return ResponseEntity.status(HttpStatus.OK).build();
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PartialCodesList>> getAllCodesLists() throws RmesException, JsonProcessingException {
        List<PartialCodesList> result = codeListService.getAllCodesLists(false);
        return ResponseEntity.status(HttpStatus.OK)
                .header("Deprecation", "true")
                .header("Sunset", "2025-12-31")
                .header("Link", "</v2/codes-list>; rel=\"successor-version\"")
                .body(result);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CodeList>> getDetailedCodesLisForSearch() throws RmesException, JsonProcessingException {
        List<CodeList> listCodeList = codeListService.getDetailedCodesListForSearch(false);
        return ResponseEntity.status(HttpStatus.OK).body(listCodeList);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/detailed/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeList> getDetailedCodesListByNotation(@PathVariable("notation") String notation) throws RmesException {
        CodeList codeListResponse = codeListService.getDetailedCodesList(notation);
        return ResponseEntity.status(HttpStatus.OK).body(codeListResponse);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/detailed/{notation}/codes")
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
    public ResponseEntity<Void> deleteCodeForCodeList(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        codeListService.deleteCodeFromCodeList(notation, code);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/detailed/{id}/codes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeListItem> updateCodeForCodeList(@PathVariable("id") String id, @PathVariable("code") String code, @RequestBody String body) throws RmesException {
        String response = codeListService.updateCodeFromCodeList(id, code, body);
        CodeListItem idCodeListItem = new CodeListItem(response);
        return ResponseEntity.status(HttpStatus.OK).body(idCodeListItem);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(value = "/detailed/{id}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeListItem> addCodeForCodeList(@PathVariable("id") String id, @RequestBody String body) throws RmesException {
        String response = codeListService.addCodeFromCodeList(id, body);
        CodeListItem idCodeListItem = new CodeListItem(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(idCodeListItem);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeList> getCodeListByNotation(@PathVariable("notation") String notation) throws RmesException {
            String codeListJson = codeListService.getCodeListJson(notation);
            CodeList codeList=Deserializer.deserializeJsonString(codeListJson, CodeList.class);
            return ResponseEntity.status(HttpStatus.OK).body(codeList);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page> getCodesForCodeList(@PathVariable("notation") String notation, @RequestParam("page") int page, @RequestParam(value = "per_page", required = false) Integer perPage) throws RmesException {
        String codeListCodesJson = codeListService.getCodesJson(notation, page, perPage);
        Page codeListCodes=Deserializer.deserializeJsonString(codeListCodesJson, Page.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeListCodes);

    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CodeLabelList> getCodeByNotation(@PathVariable("notation") String notation, @PathVariable("code") String code) throws RmesException {
        String codeLabelListJson = codeListService.getCode(notation, code);
        CodeLabelList codeLabelList=Deserializer.deserializeJsonString(codeLabelListJson, CodeLabelList.class);
        return ResponseEntity.status(HttpStatus.OK).body(codeLabelList);
    }


    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping("/{id}/validate")
    public ResponseEntity<Id> publishFullCodeList(@PathVariable(Constants.ID) Id id) throws RmesException {
        codeListService.publishCodeList(id.identifier(), false);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}
