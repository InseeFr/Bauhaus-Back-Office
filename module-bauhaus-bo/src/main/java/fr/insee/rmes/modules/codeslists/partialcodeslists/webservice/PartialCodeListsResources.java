package fr.insee.rmes.modules.codeslists.partialcodeslists.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.model.PartialCodesList;
import fr.insee.rmes.modules.commons.webservice.GenericResources;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/codeList/partial")
public class PartialCodeListsResources extends GenericResources {

    private final CodeListService codeListService;

    public PartialCodeListsResources(CodeListService codeListService) {
        this.codeListService = codeListService;
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PartialCodesList> getAllPartialCodesLists() throws JsonProcessingException, RmesException {
        return codeListService.getAllCodesLists(CodeListKind.PARTIAL);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/{notation}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDetailedPartialCodesListByNotation(@PathVariable("notation") String notation)
            throws RmesException {
        String body = codeListService.getDetailedPartialCodesList(notation);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/parent/{parentCode}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public ResponseEntity<Object> publishPartialCodeList(@PathVariable(Constants.ID) String id) throws RmesException {
        // Handled by RmesExceptionHandler, like the full codes list endpoint : returnRmesException
        // would answer with an empty body, losing the error code the front needs to translate it.
        codeListService.publishCodeList(id, CodeListKind.PARTIAL);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDetailedPartialCodesLisForSearch() throws JsonProcessingException {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(codeListService.getDetailedCodesListForSearch(CodeListKind.PARTIAL));
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.CREATE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createPartialCodeList(@RequestBody String body) {
        try {
            String id = codeListService.setCodesList(body, CodeListKind.PARTIAL);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.UPDATE)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updatePartialCodeList(
            @PathVariable(Constants.ID) String componentId, @RequestBody String body) {
        try {
            String id = codeListService.setCodesList(componentId, body, CodeListKind.PARTIAL);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }

    @HasAccess(module = RBAC.Module.CODESLIST_PARTIALCODESLIST, privilege = RBAC.Privilege.DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deletePartialCodeList(@PathVariable(Constants.ID) String notation) {
        try {
            codeListService.deleteCodeList(notation, CodeListKind.PARTIAL);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RmesException e) {
            return returnRmesException(e);
        }
    }
}
