package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;

import java.util.ArrayList;
import java.util.List;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi3XmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Colectica adapter for StudyUnit persistence.
 * <p>
 * Transforms {@link StudyUnit} to DDI3 XML via {@link Ddi3XmlWriter},
 * then delegates the REST call to the parent class.
 */
public class ColecticaStudyUnitRepository extends AbstractColecticaItemRepository implements StudyUnitRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaStudyUnitRepository.class);
    private static final String STUDY_UNIT_ITEM_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";

    private final DDIRepository ddiRepository;

    public ColecticaStudyUnitRepository(
            RestClient restClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaAuthenticator authenticator,
            Ddi3XmlWriter ddi3XmlWriter,
            DDIRepository ddiRepository
    ) {
        super(restClient, instanceConfiguration, authenticator, ddi3XmlWriter);
        this.ddiRepository = ddiRepository;
    }

    @Override
    public List<PartialStudyUnit> getAll() {
        logger.info("Getting all study units from Colectica");
        return ddiRepository.getStudyUnits();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPhysicalInstance(StudyUnit studyUnit, DDIReference physicalInstanceReference) {
        logger.info("Linking physical instance piId={} to study unit id={}", physicalInstanceReference.id(), studyUnit.getID());
        List<DDIReference> existing =
                (List<DDIReference>) studyUnit.getAdditionalProperties().get("physicalInstanceReferences");
        List<DDIReference> refs = new ArrayList<>(existing != null ? existing : List.of());
        refs.add(physicalInstanceReference);

        // Recopie l'item (forme historique en additionalProperties) en remplaçant les références PI.
        StudyUnit updated = new StudyUnit();
        updated.setURN(studyUnit.getURN());
        updated.setAgency(studyUnit.getAgency());
        updated.setID(studyUnit.getID());
        updated.setVersion(studyUnit.getVersion());
        studyUnit.getAdditionalProperties().forEach(updated::putAdditionalProperty);
        updated.putAdditionalProperty("physicalInstanceReferences", refs);
        createOrUpdate(updated);
    }

    @Override
    public void createOrUpdate(StudyUnit studyUnit) {
        logger.info("Creating/updating study unit in Colectica: id={}, agency={}, urn={}", studyUnit.getID(), studyUnit.getAgency(), studyUnit.getURN());
        try {
            String ddi3Xml = ddi3XmlWriter.buildStudyUnitXml(studyUnit);
            logger.info("Generated DDI3 XML for study unit id={}: {}", studyUnit.getID(), ddi3Xml);
            createOrUpdateItem(STUDY_UNIT_ITEM_TYPE, studyUnit.getAgency(), studyUnit.getID(), studyUnit.getVersion(),
                    (String) studyUnit.getAdditionalProperties().get("@versionDate"), ddi3Xml);
            logger.info("Study unit successfully sent to Colectica: id={}", studyUnit.getID());
        } catch (RuntimeException e) {
            logger.error("Unexpected error creating study unit in Colectica: id={}", studyUnit.getID(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error creating study unit in Colectica: id={}", studyUnit.getID(), e);
            throw new RuntimeException("Failed to create study unit: " + studyUnit.getID(), e);
        }
    }
}
