package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeriesPublicationTest {
    @InjectMocks
    SeriesPublication seriesPublication;

    @Mock
    ParentUtils ownerUtils;

    @Test
    void shouldThrowExceptionIfParentFamilyIsUnpublished() throws RmesException {
        JSONObject series = new JSONObject();
        series.put(Constants.ID, "1");
        JSONObject family = new JSONObject();
        family.put("id", "2");
        series.put("family", family);

        when(ownerUtils.getValidationStatus("2")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
        assertThrows(
                RmesBadRequestException.class,
                () -> seriesPublication.publishSeries("1", series)
        );
    }
}