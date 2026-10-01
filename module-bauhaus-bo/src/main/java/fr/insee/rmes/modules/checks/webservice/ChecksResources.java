package fr.insee.rmes.modules.checks.webservice;

import fr.insee.rmes.modules.checks.domain.model.CheckResult;
import fr.insee.rmes.modules.checks.domain.port.clientside.CheckerService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/checks",
        produces = {MediaType.APPLICATION_JSON_VALUE})
public class ChecksResources {

    private static final Logger logger = LoggerFactory.getLogger(ChecksResources.class);

    private final CheckerService checkerService;

    public ChecksResources(CheckerService checkerService) {
        this.checkerService = checkerService;
    }

    @GetMapping()
    @HasAccess(module = RBAC.Module.CONCEPT_CONCEPT, privilege = RBAC.Privilege.ADMINISTRATION)
    public ResponseEntity<List<CheckResult>> runAllChecks() {
        logger.info("Starting all data checks");
        List<CheckResult> results = checkerService.checks();
        logger.info("Completed {} checks", results.size());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(results);
    }
}
