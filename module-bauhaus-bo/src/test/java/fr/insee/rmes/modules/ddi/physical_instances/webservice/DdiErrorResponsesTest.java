package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.webservice.UnexpectedErrorHandler;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.DdiItemNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Réponses d'erreur des contrôleurs DDI (ticket 16 de l'audit #1264, ADR-1264) : jamais de corps
 * vide, un {@code code} traduisible pour les erreurs fréquentes.
 */
@ExtendWith(MockitoExtension.class)
class DdiErrorResponsesTest {

    @Mock
    private GroupService groupService;

    @Mock
    private StudyUnitService studyUnitService;

    @Mock
    private DDIService ddiService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new GroupResources(groupService, ddiService, userProvider, rbacFetcher),
                        new StudyUnitResources(studyUnitService, ddiService),
                        new CodesListResources(ddiService))
                .setControllerAdvice(new DdiExceptionHandler(), new UnexpectedErrorHandler())
                .build();
    }

    @Test
    void unknownGroupAnswers404WithATranslatableCode() throws Exception {
        when(ddiService.getDdi4Group("fr.insee", "unknown"))
                .thenThrow(DdiItemNotFoundException.group("fr.insee", "unknown"));

        mockMvc.perform(get("/ddi/group/fr.insee/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DDI_GROUP_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Group not found: fr.insee/unknown"))
                .andExpect(jsonPath("$.params.id").value("unknown"));
    }

    @Test
    void unreachableColecticaAnswers503WithATranslatableCode() throws Exception {
        when(groupService.getAll())
                .thenThrow(new RuntimeException(
                        "Failed to fetch groups", new ResourceAccessException("I/O error", new IOException())));

        mockMvc.perform(get("/ddi/groups"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("COLECTICA_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void failingColecticaAnswers503WithATranslatableCode() throws Exception {
        when(studyUnitService.getAll())
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", null, null, null));

        mockMvc.perform(get("/ddi/study-units"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("COLECTICA_UNAVAILABLE"));
    }

    @Test
    void unexpectedFailureOfAGroupEndpointAnswersAMessageInsteadOfAnEmptyBody() throws Exception {
        when(ddiService.getLogicalProductsByGroup("fr.insee", "g1")).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/ddi/groups/fr.insee/g1/logical-products"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void unexpectedFailureOfACodeListUsageEndpointAnswersAMessageInsteadOfAnEmptyBody() throws Exception {
        when(ddiService.getVariablesUsingCodeList("fr.insee", "cl1")).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/ddi/codes-list/fr.insee/cl1/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
