package fr.insee.rmes.modules.operations.families.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyAlreadyPublishedException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyNotFoundException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyPrefLabelAlreadyUsedException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.commands.CreateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.model.commands.UpdateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainFamilyServiceTest {

    @Mock
    private OperationFamilyRepository operationFamilyRepository;

    private DomainFamilyService familyService;

    /** 2026-08-27T10:15:30 en Europe/Paris, pour que les dates écrites soient prévisibles. */
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-08-27T08:15:30Z"), ZoneId.of("Europe/Paris"));
    private static final String NOW = "2026-08-27T10:15:30";

    @BeforeEach
    void setUp() {
        familyService = new DomainFamilyService(operationFamilyRepository, FIXED_CLOCK);
    }

    @Test
    void get_families_should_return_list_of_partial_operation_families() throws RmesException {
        PartialOperationFamily family1 = mock(PartialOperationFamily.class);
        PartialOperationFamily family2 = mock(PartialOperationFamily.class);
        List<PartialOperationFamily> expectedFamilies = Arrays.asList(family1, family2);

        when(operationFamilyRepository.getFamilies()).thenReturn(expectedFamilies);

        List<PartialOperationFamily> result = familyService.getFamilies();

        assertEquals(expectedFamilies, result);
        verify(operationFamilyRepository).getFamilies();
    }

    @Test
    void get_families_should_throw_rmes_exception_when_repository_fails() throws RmesException {
        RmesException expectedException = new RmesException(500, "Repository error", "Details");
        when(operationFamilyRepository.getFamilies()).thenThrow(expectedException);

        RmesException thrownException = assertThrows(RmesException.class, () -> familyService.getFamilies());

        assertEquals(expectedException, thrownException);
        verify(operationFamilyRepository).getFamilies();
    }

    @Test
    void get_family_should_return_operation_family() throws RmesException {
        String familyId = "123";
        OperationFamily expectedFamily = mock(OperationFamily.class);

        when(operationFamilyRepository.getFullFamily(familyId)).thenReturn(expectedFamily);

        OperationFamily result = familyService.getFamily(familyId);

        assertEquals(expectedFamily, result);
        verify(operationFamilyRepository).getFullFamily(familyId);
    }

    @Test
    void get_family_should_throw_rmes_exception_when_repository_fails() throws RmesException {
        String familyId = "123";
        RmesException expectedException = new RmesException(500, "Repository error", "Details");
        when(operationFamilyRepository.getFullFamily(familyId)).thenThrow(expectedException);

        RmesException thrownException = assertThrows(RmesException.class, () -> familyService.getFamily(familyId));

        assertEquals(expectedException, thrownException);
        verify(operationFamilyRepository).getFullFamily(familyId);
    }

    @Test
    void create_family_should_save_the_family_unpublished_under_a_generated_id() throws Exception {
        when(operationFamilyRepository.generateId()).thenReturn("s1001");

        String id = familyService.createFamily(
                new CreateFamilyCommand("Famille", "Family", "Résumé", "Abstract"));

        assertEquals("s1001", id);
        ArgumentCaptor<OperationFamily> captor = ArgumentCaptor.forClass(OperationFamily.class);
        verify(operationFamilyRepository).save(captor.capture());
        OperationFamily saved = captor.getValue();
        assertEquals("s1001", saved.id());
        assertEquals("Famille", saved.prefLabelLg1());
        assertEquals("Family", saved.prefLabelLg2());
        assertEquals("Résumé", saved.abstractLg1());
        assertEquals("Abstract", saved.abstractLg2());
        assertEquals(ValidationStatus.UNPUBLISHED.getValue(), saved.validationState());
        assertEquals(NOW, saved.created());
        assertEquals(NOW, saved.modified());
    }

    @Test
    void create_family_should_reject_a_pref_label_lg1_already_used_by_another_family() throws Exception {
        when(operationFamilyRepository.generateId()).thenReturn("s1001");
        when(operationFamilyRepository.isPrefLabelAlreadyUsed("s1001", "Famille", Language.lg1)).thenReturn(true);

        FamilyPrefLabelAlreadyUsedException exception = assertThrows(FamilyPrefLabelAlreadyUsedException.class,
                () -> familyService.createFamily(new CreateFamilyCommand("Famille", "Family", null, null)));

        assertEquals(Language.lg1, exception.language());
        verify(operationFamilyRepository, never()).save(any());
    }

    @Test
    void create_family_should_reject_a_pref_label_lg2_already_used_by_another_family() throws Exception {
        when(operationFamilyRepository.generateId()).thenReturn("s1001");
        when(operationFamilyRepository.isPrefLabelAlreadyUsed("s1001", "Famille", Language.lg1)).thenReturn(false);
        when(operationFamilyRepository.isPrefLabelAlreadyUsed("s1001", "Family", Language.lg2)).thenReturn(true);

        FamilyPrefLabelAlreadyUsedException exception = assertThrows(FamilyPrefLabelAlreadyUsedException.class,
                () -> familyService.createFamily(new CreateFamilyCommand("Famille", "Family", null, null)));

        assertEquals(Language.lg2, exception.language());
        verify(operationFamilyRepository, never()).save(any());
    }

    @Test
    void update_family_should_reject_an_unknown_id() throws Exception {
        when(operationFamilyRepository.exists("s1001")).thenReturn(false);

        assertThrows(FamilyNotFoundException.class, () -> familyService.updateFamily(
                new UpdateFamilyCommand("s1001", "Famille", "Family", null, null, null)));

        verify(operationFamilyRepository, never()).save(any());
    }

    @Test
    void update_family_should_keep_the_family_unpublished_when_it_never_was_published() throws Exception {
        when(operationFamilyRepository.exists("s1001")).thenReturn(true);
        when(operationFamilyRepository.getValidationStatus("s1001")).thenReturn(ValidationStatus.UNPUBLISHED);

        familyService.updateFamily(new UpdateFamilyCommand("s1001", "Famille", "Family", "Résumé", "Abstract", "2026-01-01T00:00:00"));

        ArgumentCaptor<OperationFamily> captor = ArgumentCaptor.forClass(OperationFamily.class);
        verify(operationFamilyRepository).save(captor.capture());
        OperationFamily saved = captor.getValue();
        assertEquals(ValidationStatus.UNPUBLISHED.getValue(), saved.validationState());
        assertEquals("2026-01-01T00:00:00", saved.created());
        assertEquals(NOW, saved.modified());
    }

    @Test
    void update_family_should_flag_a_published_family_as_modified() throws Exception {
        when(operationFamilyRepository.exists("s1001")).thenReturn(true);
        when(operationFamilyRepository.getValidationStatus("s1001")).thenReturn(ValidationStatus.VALIDATED);

        familyService.updateFamily(new UpdateFamilyCommand("s1001", "Famille", "Family", null, null, null));

        ArgumentCaptor<OperationFamily> captor = ArgumentCaptor.forClass(OperationFamily.class);
        verify(operationFamilyRepository).save(captor.capture());
        assertEquals(ValidationStatus.MODIFIED.getValue(), captor.getValue().validationState());
    }

    @Test
    void validate_family_should_publish_the_family() throws Exception {
        when(operationFamilyRepository.getValidationStatus("s1001")).thenReturn(ValidationStatus.MODIFIED);

        familyService.validateFamily("s1001");

        verify(operationFamilyRepository).publish("s1001");
    }

    @Test
    void validate_family_should_reject_an_already_published_family() throws Exception {
        when(operationFamilyRepository.getValidationStatus("s1001")).thenReturn(ValidationStatus.VALIDATED);

        assertThrows(FamilyAlreadyPublishedException.class, () -> familyService.validateFamily("s1001"));

        verify(operationFamilyRepository, never()).publish(any());
    }
}
