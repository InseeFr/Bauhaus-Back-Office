package fr.insee.rmes.modules.ddi.physical_instances.domain.services;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.StudyUnitReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.GroupRepository;
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
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    private GroupServiceImpl groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupServiceImpl(groupRepository);
    }

    @Test
    void createOrUpdate_shouldDelegateToRepository() {
        Group group = group(
                "true", "2026-04-03T12:00:00Z",
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus-test",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
                List.of(new StudyUnitReference("fr.insee", "su-id", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries"
        );

        groupService.createOrUpdate(group);

        verify(groupRepository).createOrUpdate(group);
    }

    @Test
    void getAll_shouldDelegateToRepository() {
        List<PartialGroup> expected = List.of(
                new PartialGroup("g1", "Group 1", new Date(), "fr.insee", List.of())
        );
        when(groupRepository.getAll()).thenReturn(expected);

        List<PartialGroup> result = groupService.getAll();

        assertThat(result).isEqualTo(expected);
        verify(groupRepository).getAll();
    }

    @Test
    void deprecateAll_shouldDelegateToRepository() {
        groupService.deprecateAll();

        verify(groupRepository).deprecateAll();
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
