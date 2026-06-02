package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeListResourcesTest {

    @Mock
    private DDIService ddiService;

    @InjectMocks
    private CodeListResources codeListResources;

    private static final String AGENCY = "fr.insee";
    private static final String ID = "fc65a527-a04b-4505-85de-0a181e54dbad";
    private static final String VERSION = "2";

    private static Ddi4Response emptyDdi4() {
        return new Ddi4Response("ddi:4.0", null, null, null, null, null, null);
    }

    @Test
    void getCodeListXml_returns200WithXml() {
        String xml = "<ddi:FragmentInstance><Fragment><CodeList/></Fragment></ddi:FragmentInstance>";
        when(ddiService.getCodeListXml(AGENCY, ID, null)).thenReturn(xml);

        ResponseEntity<String> response = codeListResources.getCodeListXml(AGENCY, ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getCodeListXml(AGENCY, ID, null);
    }

    @Test
    void getCodeListXml_returns404WhenNull() {
        when(ddiService.getCodeListXml(AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<String> response = codeListResources.getCodeListXml(AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getCodeListJson_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getCodeList(AGENCY, ID, null)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = codeListResources.getCodeListJson(AGENCY, ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertSame(ddi4, response.getBody());
        verify(ddiService).getCodeList(AGENCY, ID, null);
    }

    @Test
    void getCodeListJson_returns404WhenNull() {
        when(ddiService.getCodeList(AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<Ddi4Response> response = codeListResources.getCodeListJson(AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getCodeListXmlByVersion_returns200WithXml() {
        String xml = "<ddi:FragmentInstance/>";
        when(ddiService.getCodeListXml(AGENCY, ID, VERSION)).thenReturn(xml);

        ResponseEntity<String> response = codeListResources.getCodeListXmlByVersion(AGENCY, ID, VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getCodeListXml(AGENCY, ID, VERSION);
    }

    @Test
    void getCodeListJsonByVersion_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getCodeList(AGENCY, ID, VERSION)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = codeListResources.getCodeListJsonByVersion(AGENCY, ID, VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(ddi4, response.getBody());
        verify(ddiService).getCodeList(AGENCY, ID, VERSION);
    }
}
