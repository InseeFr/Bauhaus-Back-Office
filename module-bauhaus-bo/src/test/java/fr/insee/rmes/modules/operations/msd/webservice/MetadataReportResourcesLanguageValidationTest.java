package fr.insee.rmes.modules.operations.msd.webservice;

import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetadataReportResourcesLanguageValidationTest {

    @Test
    void getSimsExport_rejectsRequestWithNoLanguageSelected() {
        MetadataReportResources controller = new MetadataReportResources(null, null, null, null);

        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class,
                () -> controller.getSimsExport("1234", true, false, false, true));

        JSONObject details = new JSONObject(exception.getDetails());
        assertEquals(ErrorCodes.SIMS_EXPORT_WITHOUT_LANGUAGE, details.getInt("code"));
        assertEquals("at least one language must be selected for export", details.getString("message"));
        assertEquals("in export of sims: 1234", details.getString("details"));
    }
}
