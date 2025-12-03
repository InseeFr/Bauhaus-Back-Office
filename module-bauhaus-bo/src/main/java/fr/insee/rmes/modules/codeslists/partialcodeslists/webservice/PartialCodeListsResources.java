package fr.insee.rmes.modules.codeslists.partialcodeslists.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.Constants;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeLabelList;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeList;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.model.PartialCodesList;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.commons.webservice.GenericResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/codeList/partial")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Partial Code lists", description = "Partial Code list API")
public class PartialCodeListsResources extends GenericResources {

    @Autowired
    CodeListService codeListService;

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Partial List of codes")
    public List<PartialCodesList> getAllPartialCodesLists() throws JsonProcessingException, RmesException {
        return codeListService.getAllCodesLists(true);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a partial list of code")
    public ResponseEntity<Object> getDetailedPartialCodesListByNotation (@PathVariable("notation") String notation) throws RmesException {
        String body = codeListService.getDetailedPartialCodesList(notation);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/parent/{parentCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get partials by Parent IRI")
    public ResponseEntity<Object> getPartialsByParent(@PathVariable("parentCode") String parentIri) {
        try {
            String codesLists = codeListService.getPartialCodeListByParent(parentIri);
            return ResponseEntity.status(HttpStatus.OK).body(codesLists);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.PUBLISH)
    @PutMapping("/{id}/validate")
    @io.swagger.v3.oas.annotations.Operation(summary = "Publish a partial codelist")
    public ResponseEntity<Object> publishPartialCodeList(
            @PathVariable(Constants.ID) String id) {
        try {
            codeListService.publishCodeList(id, true);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return all lists for Advanced Search")
    public ResponseEntity<Object> getDetailedPartialCodesLisForSearch() throws JsonProcessingException {
        try {
            List<CodeList> body = codeListService.getDetailedCodesListForSearch(true);
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a code list")
    public ResponseEntity<Object> createPartialCodeList(
            @Parameter(description = "Code List", required = true) @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(body, true);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a code list")
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

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a partial code list")
    public ResponseEntity<Object> deletePartialCodeList(@PathVariable(Constants.ID) String notation) {
        try {
            codeListService.deleteCodeList(notation, true);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }
}