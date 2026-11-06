package fr.insee.rmes.bauhaus_services.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.utils.ExportUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

/** Bouchonnage et vérification de l'export ODT, partagés par les tests des exports de concepts et de collections. */
public final class OdtExportStubs {

    private OdtExportStubs() {}

    /** L'export ODT du mock {@code exportUtils} renvoie une réponse vide, qui est retournée. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ResponseEntity<?> givenOdtExportResponse(ExportUtils exportUtils) throws RmesException {
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(new ByteArrayResource(new byte[0]));
        when(exportUtils.exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) expectedResponse);
        return expectedResponse;
    }

    public static void assertSameResponseExportedOnceAsOdt(
            ExportUtils exportUtils, ResponseEntity<?> expectedResponse, ResponseEntity<?> result)
            throws RmesException {
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(exportUtils, times(1))
                .exportAsODT(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString());
    }
}
