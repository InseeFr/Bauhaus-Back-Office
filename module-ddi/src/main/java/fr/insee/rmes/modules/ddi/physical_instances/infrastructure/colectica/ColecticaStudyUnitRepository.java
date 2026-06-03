package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import fr.insee.rmes.colectica.client.ColecticaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ColecticaStudyUnitRepository extends AbstractColecticaItemRepository implements StudyUnitRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaStudyUnitRepository.class);
    private static final String STUDY_UNIT_ITEM_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";

    private final DDIRepository ddiRepository;

    public ColecticaStudyUnitRepository(
            ColecticaClient colecticaClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            Ddi4ToLifecycle33 ddi4ToLifecycle33,
            DDIRepository ddiRepository
    ) {
        super(colecticaClient, instanceConfiguration, ddi4ToLifecycle33);
        this.ddiRepository = ddiRepository;
    }

    @Override
    public List<PartialStudyUnit> getAll() {
        logger.info("Getting all study units from Colectica");
        return ddiRepository.getStudyUnits();
    }

    @Override
    public void addPhysicalInstance(Ddi4StudyUnit studyUnit, Reference physicalInstanceReference) {
        logger.info("Linking physical instance piId={} to study unit id={}", physicalInstanceReference.id(), studyUnit.id());
        List<Reference> refs = new ArrayList<>(
                studyUnit.physicalInstanceReferences() != null ? studyUnit.physicalInstanceReferences() : List.of()
        );
        refs.add(physicalInstanceReference);
        Ddi4StudyUnit updated = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                studyUnit.versionDate(),
                studyUnit.urn(),
                studyUnit.agency(),
                studyUnit.id(),
                studyUnit.version(),
                studyUnit.citation(),
                studyUnit.operationIri(),
                refs
        );
        createOrUpdate(updated);
    }

    @Override
    public void createOrUpdate(Ddi4StudyUnit studyUnit) {
        logger.info("Creating/updating study unit in Colectica: id={}, agency={}, urn={}", studyUnit.id(), studyUnit.agency(), studyUnit.urn());
        try {
            createOrUpdateItem(STUDY_UNIT_ITEM_TYPE, studyUnit);
            logger.info("Study unit successfully sent to Colectica: id={}", studyUnit.id());
        } catch (RuntimeException e) {
            logger.error("Unexpected error creating study unit in Colectica: id={}", studyUnit.id(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating study unit in Colectica: id={}", studyUnit.id(), e);
            throw new RuntimeException("Failed to create study unit: " + studyUnit.id(), e);
        }
    }
}
