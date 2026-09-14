package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialCodeListSchemeResponse;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class CodeListSchemeResourcesTest {

    @Mock
    private DDIService ddiService;

    private CodeListSchemeResources codeListSchemeResources;

    @BeforeEach
    void setUp() {
        codeListSchemeResources = new CodeListSchemeResources(ddiService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldGetCodeListSchemes() {
        List<PartialCodeListScheme> expected = List.of(
                new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee"),
                new PartialCodeListScheme("cls-2", "Schéma 2", new Date(), "fr.insee"));
        when(ddiService.getCodeListSchemes()).thenReturn(expected);

        ResponseEntity<List<PartialCodeListSchemeResponse>> response = codeListSchemeResources.getCodeListSchemes();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialCodeListSchemeResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("cls-1", result.getFirst().getId());
        assertEquals("Schéma 1", result.getFirst().getLabel());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals(
                "http://localhost:8080/ddi/code-list-scheme/fr.insee/cls-1",
                result.getFirst().getRequiredLink("self").getHref());

        assertEquals("cls-2", result.get(1).getId());
        assertEquals(
                "http://localhost:8080/ddi/code-list-scheme/fr.insee/cls-2",
                result.get(1).getRequiredLink("self").getHref());

        verify(ddiService).getCodeListSchemes();
    }
}
