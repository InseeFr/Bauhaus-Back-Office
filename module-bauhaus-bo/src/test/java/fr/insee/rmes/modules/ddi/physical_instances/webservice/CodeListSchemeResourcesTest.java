package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.modules.ddi.physical_instances.webservice.DdiResourcesTestSupport.assertOkListOfSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialCodeListSchemeResponse;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith({MockitoExtension.class, LocalhostRequestContextExtension.class})
class CodeListSchemeResourcesTest {

    @Mock
    private DDIService ddiService;

    private CodeListSchemeResources codeListSchemeResources;

    @BeforeEach
    void setUp() {
        codeListSchemeResources = new CodeListSchemeResources(ddiService);
    }

    @Test
    void shouldGetCodeListSchemes() {
        List<PartialCodeListScheme> expected = List.of(
                new PartialCodeListScheme("cls-1", "Schéma 1", new Date(), "fr.insee"),
                new PartialCodeListScheme("cls-2", "Schéma 2", new Date(), "fr.insee"));
        when(ddiService.getCodeListSchemes()).thenReturn(expected);

        ResponseEntity<List<PartialCodeListSchemeResponse>> response = codeListSchemeResources.getCodeListSchemes();

        List<PartialCodeListSchemeResponse> result = assertOkListOfSize(response, 2);

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
