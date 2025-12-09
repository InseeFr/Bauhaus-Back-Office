package fr.insee.rmes.modules.commons.webservice.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.domain.codeslist.CodesListService;
import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.codeslists.CodesList;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/v2/codes-list")
public class V2CodeListsResources {

    private final CodesListService codesListService;

    public V2CodeListsResources(CodesListService codesListService) {
        this.codesListService = codesListService;
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CodesListDomain> getAllCodesLists(
        @RequestParam(value = "partial", required = false, defaultValue = "false") boolean partial
    ) throws RmesException, JsonProcessingException {
        return codesListService.getAllCodesLists(partial);
    }
}