package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Colectica adapter for StudyUnit persistence.
 * <p>
 * Transforms {@link Ddi4StudyUnit} to DDI3 XML via {@link Ddi3XmlWriter},
 * then delegates the REST call to the parent class.
 */
public class ColecticaStudyUnitRepository extends AbstractColecticaItemRepository implements StudyUnitRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaStudyUnitRepository.class);
    private static final String STUDY_UNIT_ITEM_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";

    private final DDIRepository ddiRepository;

    public ColecticaStudyUnitRepository(
            RestTemplate restTemplate,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter,
            DDIRepository ddiRepository
    ) {
        super(restTemplate, instanceConfiguration, authenticator, ddi3XmlWriter);
        this.ddiRepository = ddiRepository;
    }

    @Override
    public List<PartialStudyUnit> getAll() {
        logger.info("Getting all study units from Colectica");
        return ddiRepository.getStudyUnits();
    }

    @Override
    public void createOrUpdate(Ddi4StudyUnit studyUnit) {
        logger.info("Creating/updating study unit in Colectica: id={}, agency={}, urn={}", studyUnit.id(), studyUnit.agency(), studyUnit.urn());
        try {
            String ddi3Xml = ddi3XmlWriter.buildStudyUnitXml(studyUnit);
            logger.info("Generated DDI3 XML for study unit id={}: {}", studyUnit.id(), ddi3Xml);
            createOrUpdateItem(STUDY_UNIT_ITEM_TYPE, studyUnit, ddi3Xml);
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
