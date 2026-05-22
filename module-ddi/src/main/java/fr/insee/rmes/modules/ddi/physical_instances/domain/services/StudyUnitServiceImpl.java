package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Domain service for DDI StudyUnit items.
 * <p>
 * Inherits common {@code createOrUpdate} logic from {@link AbstractDdiItemService}.
 */
public class StudyUnitServiceImpl extends AbstractDdiItemService<StudyUnit> implements StudyUnitService {

    private static final Logger logger = LoggerFactory.getLogger(StudyUnitServiceImpl.class);

    private final StudyUnitRepository studyUnitRepository;

    public StudyUnitServiceImpl(StudyUnitRepository studyUnitRepository) {
        super(studyUnitRepository);
        this.studyUnitRepository = studyUnitRepository;
    }

    @Override
    public List<PartialStudyUnit> getAll() {
        logger.info("Getting all study units");
        return studyUnitRepository.getAll();
    }

    @Override
    public void addPhysicalInstance(StudyUnit studyUnit, DDIReference physicalInstanceReference) {
        logger.info("Adding physical instance to study unit: id={}, piId={}", studyUnit.getID(), physicalInstanceReference.id());
        studyUnitRepository.addPhysicalInstance(studyUnit, physicalInstanceReference);
    }

    @Override
    protected String itemTypeName() {
        return "study unit";
    }

    @Override
    protected String itemId(StudyUnit studyUnit) {
        return studyUnit.getID();
    }
}
