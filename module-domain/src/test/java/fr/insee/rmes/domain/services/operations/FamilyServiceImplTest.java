package fr.insee.rmes.domain.services.operations;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FamilyServiceImplTest {

    @Mock
    private OperationFamilyRepository operationFamilyRepository;

    private FamilyServiceImpl familyService;

    @BeforeEach
    void setUp() {
        familyService = new FamilyServiceImpl(operationFamilyRepository);
    }

    @Test
    void getFamilies_ShouldReturnListOfPartialOperationFamilies() throws RmesException {
        PartialOperationFamily family1 = mock(PartialOperationFamily.class);
        PartialOperationFamily family2 = mock(PartialOperationFamily.class);
        List<PartialOperationFamily> expectedFamilies = Arrays.asList(family1, family2);

        when(operationFamilyRepository.getFamilies()).thenReturn(expectedFamilies);

        List<PartialOperationFamily> result = familyService.getFamilies();

        assertEquals(expectedFamilies, result);
        verify(operationFamilyRepository).getFamilies();
    }

    @Test
    void getFamilies_ShouldThrowRmesExceptionWhenRepositoryFails() throws RmesException {
        RmesException expectedException = new RmesException(500, "Repository error", "Details");
        when(operationFamilyRepository.getFamilies()).thenThrow(expectedException);

        RmesException thrownException = assertThrows(RmesException.class, () -> familyService.getFamilies());

        assertEquals(expectedException, thrownException);
        verify(operationFamilyRepository).getFamilies();
    }

    @Test
    void getFamily_ShouldReturnOperationFamily() throws RmesException {
        String familyId = "123";
        OperationFamily expectedFamily = mock(OperationFamily.class);

        when(operationFamilyRepository.getFullFamily(familyId)).thenReturn(expectedFamily);

        OperationFamily result = familyService.getFamily(familyId);

        assertEquals(expectedFamily, result);
        verify(operationFamilyRepository).getFullFamily(familyId);
    }

    @Test
    void getFamily_ShouldThrowRmesExceptionWhenRepositoryFails() throws RmesException {
        String familyId = "123";
        RmesException expectedException = new RmesException(500, "Repository error", "Details");
        when(operationFamilyRepository.getFullFamily(familyId)).thenThrow(expectedException);

        RmesException thrownException = assertThrows(RmesException.class, () -> familyService.getFamily(familyId));

        assertEquals(expectedException, thrownException);
        verify(operationFamilyRepository).getFullFamily(familyId);
    }
}