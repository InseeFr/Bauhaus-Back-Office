package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportUtilsTest {

    @MockitoBean
    DocumentsUtils documentsUtils ;

    ExportUtils exportUtils = new ExportUtils(32,documentsUtils);

    @Test
    void shouldReturnResultsWhenValidationStatus() {
        List<String> actual= new ArrayList<>();
        actual.add(ExportUtils.toValidationStatus("true",true));
        actual.add(ExportUtils.toValidationStatus("true",false));
        actual.add(ExportUtils.toValidationStatus("false",true));
        actual.add(ExportUtils.toValidationStatus("false",false));
        assertEquals("[Publiée, Publié, Provisoire, Provisoire]",actual.toString());
    }

    @Test
    void shouldExportFilesAsResponse() throws RmesException {
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("yellow","lemon");
        xmlContent.put("green","apple");
        xmlContent.put("red","strawberry");
        xmlContent.put(null,"漢字");
        xmlContent.put("--","****");
        xmlContent.put("","");
        boolean actual = exportUtils.exportFilesAsResponse(xmlContent).toString().startsWith("<200 OK OK,URL [");
        assertTrue(actual);}

    @Test
    void shouldThrowNullPointerExceptionWhenExportFilesAsResponse(){
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("exampleOfValue",null);
        NullPointerException exception = assertThrows(NullPointerException.class, () -> exportUtils.exportFilesAsResponse(xmlContent));
        assertEquals("Cannot invoke \"String.getBytes(java.nio.charset.Charset)\" because \"xmlData\" is null", exception.getMessage());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenExportFilesAsInputStream(){
        Map<String, String> xmlContent = new HashMap<>();
        xmlContent.put("red","strawberry");
        RmesException exception = assertThrows(RmesException.class, () -> exportUtils.exportAsInputStream("fileName", xmlContent, "xslFile", "xmlPattern", "zip", "objectType", "extension"));
        assertTrue(exception.getDetails().contains("TransformerConfigurationException"));
    }

}