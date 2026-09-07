package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
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
import java.util.Set;

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
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
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

    @Test
    void getAll_shouldBeSortedByLabelDescending() {
        when(studyUnitRepository.getAll()).thenReturn(List.of(
                new PartialStudyUnit("su-a", "alpha", new Date(), "fr.insee"),
                new PartialStudyUnit("su-c", "Charlie", new Date(), "fr.insee"),
                new PartialStudyUnit("su-b", "Bravo", new Date(), "fr.insee")
        ));

        List<PartialStudyUnit> result = studyUnitService.getAll();

        assertThat(result).extracting(PartialStudyUnit::label)
                .containsExactly("Charlie", "Bravo", "alpha");
    }

    @Test
    void deprecate_shouldDelegateToRepository() {
        Set<String> ids = Set.of("su-id-1", "su-id-2");

        studyUnitService.deprecate(ids);

        verify(studyUnitRepository).deprecate(ids);
    }
}
