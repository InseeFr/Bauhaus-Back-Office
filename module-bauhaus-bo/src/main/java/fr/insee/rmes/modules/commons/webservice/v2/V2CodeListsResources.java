package fr.insee.rmes.modules.commons.webservice.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.domain.codeslist.CodesListService;
import fr.insee.rmes.domain.codeslist.model.CodesListDomain;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.codeslists.CodesList;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/v2/codes-list")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Code lists v2", description = "Code list API v2")
public class V2CodeListsResources {

    private final CodesListService codesListService;

    public V2CodeListsResources(CodesListService codesListService) {
        this.codesListService = codesListService;
    }

    @HasAccess(module = RBAC.Module.CODESLIST_CODESLIST, privilege = RBAC.Privilege.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get all code lists"
    )
    public List<CodesListDomain> getAllCodesLists(
        @Parameter(description = "Filter by partial code lists (skos:Collection) vs complete code lists (skos:ConceptScheme)")
        @RequestParam(value = "partial", required = false, defaultValue = "false") boolean partial
    ) throws RmesException, JsonProcessingException {
        return codesListService.getAllCodesLists(partial);
    }
}