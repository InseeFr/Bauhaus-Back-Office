package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodesListResourcesTest {

    @Mock
    private DDIService ddiService;

    @InjectMocks
    private CodesListResources codesListResources;

    @Test
    void getCodeListUsers_shouldReturn200WithList() {
        List<CodeListVariableUsage> usages = List.of(
                new CodeListVariableUsage("fr.insee", "pi-1", "fr.insee", "var-1"),
                new CodeListVariableUsage("fr.insee", "pi-2", "fr.insee", "var-2")
        );
        when(ddiService.getVariablesUsingCodeList("fr.insee", "cl-1")).thenReturn(usages);

        ResponseEntity<List<CodeListVariableUsage>> response =
                codesListResources.getCodeListUsers("fr.insee", "cl-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).variableId()).isEqualTo("var-1");
        assertThat(response.getBody().get(0).physicalInstanceId()).isEqualTo("pi-1");
        verify(ddiService).getVariablesUsingCodeList("fr.insee", "cl-1");
    }

    @Test
    void getCodeListUsers_shouldReturn500OnError() {
        when(ddiService.getVariablesUsingCodeList("fr.insee", "cl-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<CodeListVariableUsage>> response =
                codesListResources.getCodeListUsers("fr.insee", "cl-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
