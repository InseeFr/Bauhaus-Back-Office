package fr.insee.rmes.bauhaus_services.operations.operations;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationPublicationTest {

    @InjectMocks
    OperationPublication operationPublication;

    @Mock
    OperationsParentRepository operationsParentRepository;

    @Test
    void shouldThrowExceptionIfOperationIsAlreadyPublished() throws RmesException {
        JSONObject operation = new JSONObject();
        operation.put(Constants.ID, "1");

        when(operationsParentRepository.getFamOpSerValidationStatus("1"))
                .thenReturn(ValidationStatus.VALIDATED.getValue());

        var exception = assertThrows(
                RmesBadRequestException.class, () -> operationPublication.publishOperation("1", operation));
        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("Operation: 1");
    }

    @Test
    void shouldThrowExceptionIfParentSeriesIsUnpublished() throws RmesException {
        JSONObject operation = new JSONObject();
        operation.put(Constants.ID, "1");
        JSONObject series = new JSONObject();
        series.put("id", "2");
        operation.put("series", series);

        when(operationsParentRepository.getValidationStatus("2")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
        var exception = assertThrows(
                RmesBadRequestException.class, () -> operationPublication.publishOperation("1", operation));
        assertThat(exception.getDetails())
                .contains("This operation cannot be published before its series is published");
    }

    @Test
    void shouldReturnOperationErrorCodeWhenParentSeriesIsUnpublished() throws RmesException {
        JSONObject operation = new JSONObject();
        operation.put(Constants.ID, "1");
        JSONObject series = new JSONObject();
        series.put("id", "2");
        operation.put("series", series);

        when(operationsParentRepository.getValidationStatus("2")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
        var exception = assertThrows(
                RmesBadRequestException.class, () -> operationPublication.publishOperation("1", operation));
        assertThat(exception.getDetails()).contains("\"code\":704");
    }
}
