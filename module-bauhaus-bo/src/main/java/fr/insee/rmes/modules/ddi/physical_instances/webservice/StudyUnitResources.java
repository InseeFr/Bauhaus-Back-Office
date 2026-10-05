package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.domain.logging.LogSanitizer.forLog;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.webservice.HasAccess;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for DDI StudyUnit operations.
 */
@RestController
@RequestMapping(value = "/ddi/study-units", produces = MediaType.APPLICATION_JSON_VALUE)
@ConditionalOnModule("ddi")
public class StudyUnitResources {

    private static final Logger logger = LoggerFactory.getLogger(StudyUnitResources.class);

    private final StudyUnitService studyUnitService;
    private final DDIService ddiService;

    public StudyUnitResources(StudyUnitService studyUnitService, DDIService ddiService) {
        this.studyUnitService = studyUnitService;
        this.ddiService = ddiService;
    }

    @GetMapping
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<PartialStudyUnit>> getStudyUnits() {
        logger.info("GET /ddi/study-units - Getting all study units");
        List<PartialStudyUnit> studyUnits = studyUnitService.getAll();
        return ResponseEntity.ok(studyUnits);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.CREATE)
    public ResponseEntity<Void> createOrUpdateStudyUnit(@RequestBody Ddi4StudyUnit studyUnit) {
        logger.info("POST /ddi/study-units - Creating/updating study unit: id={}", studyUnit.id());
        studyUnitService.createOrUpdate(studyUnit);
        return ResponseEntity.status(201).build();
    }

    /**
     * Les variables du VariableScheme de la StudyUnit, dans l'enveloppe DDI4 : le vivier dans lequel
     * une PhysicalInstance de l'étude réutilise une variable (#1387).
     */
    @GetMapping("/{agencyId}/{id}/variables")
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<Ddi4Response> getStudyUnitVariables(@PathVariable String agencyId, @PathVariable String id) {
        logger.info("GET /ddi/study-units/{}/{}/variables - Getting reusable variables", forLog(agencyId), forLog(id));
        return ResponseEntity.ok(ddiService.getStudyUnitVariables(agencyId, id));
    }

    /**
     * Une ligne par variable utilisée par chaque PhysicalInstance de la StudyUnit : signale qu'une
     * variable réutilisée est partagée avec d'autres fichiers (#1387).
     */
    @GetMapping("/{agencyId}/{id}/variable-usages")
    @HasAccess(module = RBAC.Module.DDI_PHYSICALINSTANCE, privilege = RBAC.Privilege.READ)
    public ResponseEntity<List<CodeListVariableUsage>> getStudyUnitVariableUsages(
            @PathVariable String agencyId, @PathVariable String id) {
        logger.info(
                "GET /ddi/study-units/{}/{}/variable-usages - Getting variable usages", forLog(agencyId), forLog(id));
        return ResponseEntity.ok(ddiService.getStudyUnitVariableUsages(agencyId, id));
    }
}
