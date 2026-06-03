package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for CodeList usage queries.
 */
@RestController
@RequestMapping(
        value = "/ddi",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnModule("ddi")
public class CodesListResources {

    private static final Logger logger = LoggerFactory.getLogger(CodesListResources.class);

    private final DDIService ddiService;

    public CodesListResources(DDIService ddiService) {
        this.ddiService = ddiService;
    }

    /**
     * Returns every Variable (with its PhysicalInstance) that uses the code list {@code agencyId/id}.
     */
    @GetMapping("/codes-list/{agencyId}/{id}/users")
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<CodeListVariableUsage>> getCodeListUsers(
            @PathVariable String agencyId,
            @PathVariable String id) {
        logger.info("GET /ddi/codes-list/{}/{}/users - Getting variables using code list", agencyId, id);
        try {
            List<CodeListVariableUsage> usages = ddiService.getVariablesUsingCodeList(agencyId, id);
            return ResponseEntity.ok(usages);
        } catch (Exception e) {
            logger.error("Failed to get variables using code list: agencyId={}, id={}", agencyId, id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
