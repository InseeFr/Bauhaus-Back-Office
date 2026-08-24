package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingValuesRepresentationInUseException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingValuesRepresentationNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CategoryCodeListUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UsageItem;
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
import static org.mockito.Mockito.doThrow;
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
                new CodeListVariableUsage("fr.insee", "su-1", "Recensement 2024",
                        "fr.insee", "pi-1", "Fichier détail", "fr.insee", "var-1", "Sexe"),
                new CodeListVariableUsage("fr.insee", "su-1", "Recensement 2024",
                        "fr.insee", "pi-2", "Fichier ménage", "fr.insee", "var-2", "Âge")
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

    // --- /ddi/category/{agencyId}/{id}/users (catégorie partagée) ---

    @Test
    void getCategoryUsers_shouldReturn200WithList() {
        UsageItem group = new UsageItem("fr.insee", "grp-1", "Groupe démographie");
        UsageItem studyUnit = new UsageItem("fr.insee", "su-1", "Recensement 2024");
        List<CategoryCodeListUsage> usages = List.of(
                new CategoryCodeListUsage(
                        group, studyUnit,
                        new UsageItem("fr.insee", "pi-1", "Fichier détail"),
                        new UsageItem("fr.insee", "var-1", "Sexe"),
                        new UsageItem("fr.insee", "cl-1", "Pays")),
                new CategoryCodeListUsage(
                        group, studyUnit,
                        new UsageItem("fr.insee", "pi-2", "Fichier ménage"),
                        new UsageItem("fr.insee", "var-2", "Âge"),
                        new UsageItem("fr.insee", "cl-2", "Pays de naissance"))
        );
        when(ddiService.getCodeListsUsingCategory("fr.insee", "cat-1")).thenReturn(usages);

        ResponseEntity<List<CategoryCodeListUsage>> response =
                codesListResources.getCategoryUsers("fr.insee", "cat-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).codeList().id()).isEqualTo("cl-1");
        assertThat(response.getBody().get(0).codeList().label()).isEqualTo("Pays");
        verify(ddiService).getCodeListsUsingCategory("fr.insee", "cat-1");
    }

    @Test
    void getCategoryUsers_shouldReturn500OnError() {
        when(ddiService.getCodeListsUsingCategory("fr.insee", "cat-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<CategoryCodeListUsage>> response =
                codesListResources.getCategoryUsers("fr.insee", "cat-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- /ddi/missing-values-representations/{agencyId}/{id}/users (valeurs sentinelles, #1566) ---

    @Test
    void getMissingValuesRepresentationUsers_shouldReturn200WithList() {
        List<CodeListVariableUsage> usages = List.of(
                new CodeListVariableUsage("fr.insee", "su-1", "Recensement 2024",
                        "fr.insee", "pi-1", "Fichier détail", "fr.insee", "var-1", "Sexe")
        );
        when(ddiService.getVariablesUsingMissingValuesRepresentation("fr.insee", "mmvr-1"))
                .thenReturn(usages);

        ResponseEntity<List<CodeListVariableUsage>> response =
                codesListResources.getMissingValuesRepresentationUsers("fr.insee", "mmvr-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).variableId()).isEqualTo("var-1");
        verify(ddiService).getVariablesUsingMissingValuesRepresentation("fr.insee", "mmvr-1");
    }

    @Test
    void getMissingValuesRepresentationUsers_shouldReturn500OnError() {
        when(ddiService.getVariablesUsingMissingValuesRepresentation("fr.insee", "mmvr-1"))
                .thenThrow(new RuntimeException("Colectica error"));

        ResponseEntity<List<CodeListVariableUsage>> response =
                codesListResources.getMissingValuesRepresentationUsers("fr.insee", "mmvr-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // --- DELETE /ddi/missing-values-representations/{agencyId}/{id} (#1566) ---

    @Test
    void deleteMissingValuesRepresentation_shouldReturn204() {
        ResponseEntity<Void> response =
                codesListResources.deleteMissingValuesRepresentation("fr.insee", "mmvr-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ddiService).deleteMissingValuesRepresentation("fr.insee", "mmvr-1");
    }

    @Test
    void deleteMissingValuesRepresentation_shouldReturn409WhenStillUsed() {
        doThrow(new MissingValuesRepresentationInUseException("utilisée"))
                .when(ddiService).deleteMissingValuesRepresentation("fr.insee", "mmvr-1");

        ResponseEntity<Void> response =
                codesListResources.deleteMissingValuesRepresentation("fr.insee", "mmvr-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deleteMissingValuesRepresentation_shouldReturn404WhenUnknown() {
        doThrow(new MissingValuesRepresentationNotFoundException("introuvable"))
                .when(ddiService).deleteMissingValuesRepresentation("fr.insee", "unknown");

        ResponseEntity<Void> response =
                codesListResources.deleteMissingValuesRepresentation("fr.insee", "unknown");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteMissingValuesRepresentation_shouldReturn500OnError() {
        doThrow(new RuntimeException("Colectica error"))
                .when(ddiService).deleteMissingValuesRepresentation("fr.insee", "mmvr-1");

        ResponseEntity<Void> response =
                codesListResources.deleteMissingValuesRepresentation("fr.insee", "mmvr-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
