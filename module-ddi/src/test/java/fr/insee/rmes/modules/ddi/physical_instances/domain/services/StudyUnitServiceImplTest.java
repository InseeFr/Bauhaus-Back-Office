package fr.insee.rmes.modules.ddi.physical_instances.domain.services;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.StudyUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyUnitServiceImplTest {

    @Mock
    private StudyUnitRepository studyUnitRepository;

    private StudyUnitServiceImpl studyUnitService;

    @BeforeEach
    void setUp() {
        studyUnitService = new StudyUnitServiceImpl(studyUnitRepository);
    }

    @Test
    void createOrUpdate_shouldDelegateToRepository() {
        StudyUnit studyUnit = studyUnit(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(LangStrings.of("fr-FR", "Test StudyUnit")),
                "http://id.insee.fr/operations/operation/op1",
                null
        );

        studyUnitService.createOrUpdate(studyUnit);

        verify(studyUnitRepository).createOrUpdate(studyUnit);
    }

    @Test
    void getAll_shouldDelegateToRepository() {
        List<PartialStudyUnit> expected = List.of(
                new PartialStudyUnit("su-1", "StudyUnit 1", new Date(), "fr.insee")
        );
        when(studyUnitRepository.getAll()).thenReturn(expected);

        List<PartialStudyUnit> result = studyUnitService.getAll();

        assertThat(result).isEqualTo(expected);
        verify(studyUnitRepository).getAll();
    }
    private static Group group(String isUniversallyUnique, String versionDate, String urn, String agency,
                               String id, String version, String versionResponsibility, Citation citation,
                               List<StudyUnitReference> studyUnitReference, List<String> seriesIris, String typeOfGroup) {
        Group group = new Group();
        group.setURN(urn);
        group.setAgency(agency);
        group.setID(id);
        group.setVersion(version);
        group.setVersionResponsibility(versionResponsibility);
        group.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        group.putAdditionalProperty("@versionDate", versionDate);
        if (citation != null) group.putAdditionalProperty("Citation", citation);
        if (studyUnitReference != null) group.putAdditionalProperty("StudyUnitReference", studyUnitReference);
        if (seriesIris != null) group.putAdditionalProperty("seriesIris", seriesIris);
        if (typeOfGroup != null) group.putAdditionalProperty("typeOfGroup", typeOfGroup);
        return group;
    }

    private static StudyUnit studyUnit(String isUniversallyUnique, String versionDate, String urn, String agency,
                                       String id, String version, Citation citation, String operationIri,
                                       List<DDIReference> physicalInstanceReferences) {
        StudyUnit studyUnit = new StudyUnit();
        studyUnit.setURN(urn);
        studyUnit.setAgency(agency);
        studyUnit.setID(id);
        studyUnit.setVersion(version);
        studyUnit.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        studyUnit.putAdditionalProperty("@versionDate", versionDate);
        if (citation != null) studyUnit.putAdditionalProperty("Citation", citation);
        if (operationIri != null) studyUnit.putAdditionalProperty("operationIri", operationIri);
        if (physicalInstanceReferences != null) studyUnit.putAdditionalProperty("physicalInstanceReferences", physicalInstanceReferences);
        return studyUnit;
    }
}
