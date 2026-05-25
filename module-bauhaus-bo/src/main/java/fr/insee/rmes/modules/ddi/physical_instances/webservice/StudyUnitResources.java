package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for DDI StudyUnit operations.
 */
@RestController
@RequestMapping(
        value = "/ddi/study-units",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnModule("ddi")
public class StudyUnitResources {

    private static final Logger logger = LoggerFactory.getLogger(StudyUnitResources.class);

    private final StudyUnitService studyUnitService;

    public StudyUnitResources(StudyUnitService studyUnitService) {
        this.studyUnitService = studyUnitService;
    }

    @GetMapping
    public ResponseEntity<List<PartialStudyUnit>> getStudyUnits() {
        logger.info("GET /ddi/study-units - Getting all study units");
        try {
            List<PartialStudyUnit> studyUnits = studyUnitService.getAll();
            return ResponseEntity.ok(studyUnits);
        } catch (Exception e) {
            logger.error("Failed to get study units", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrUpdateStudyUnit(@RequestBody Ddi4StudyUnit studyUnit) {
        logger.info("POST /ddi/study-units - Creating/updating study unit: id={}", studyUnit.id());
        try {
            studyUnitService.createOrUpdate(studyUnit);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            logger.error("Failed to create/update study unit: id={}", studyUnit.id(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
