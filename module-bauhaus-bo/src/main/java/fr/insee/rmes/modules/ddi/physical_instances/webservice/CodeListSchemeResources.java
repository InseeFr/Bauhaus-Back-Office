package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialCodeListSchemeResponse;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(
        value = "/ddi",
        produces = {
                "application/hal+json",
                MediaType.APPLICATION_JSON_VALUE
        }
)
@ConditionalOnModule("ddi")
public class CodeListSchemeResources {

    private final DDIService ddiService;

    public CodeListSchemeResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    @GetMapping("/code-list-scheme")
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<PartialCodeListSchemeResponse>> getCodeListSchemes() {
        List<PartialCodeListScheme> codeListSchemes = ddiService.getCodeListSchemes();

        List<PartialCodeListSchemeResponse> responses = codeListSchemes.stream()
                .map(codeListScheme -> {
                    var response = PartialCodeListSchemeResponse.fromDomain(codeListScheme);
                    response.add(linkTo(CodeListSchemeResources.class)
                            .slash("code-list-scheme")
                            .slash(codeListScheme.agency())
                            .slash(codeListScheme.id())
                            .withSelfRel());
                    return response;
                })
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaTypes.HAL_JSON)
                .body(responses);
    }
}
