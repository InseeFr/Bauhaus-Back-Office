package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.MissingSchemeException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DdiExceptionHandlerTest {

    @Mock
    private DDIService ddiService;

    @Mock
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Mock
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @Mock
    private DDIItemConvertService ddiItemConvertService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    @Mock
    private BauhausUriBuilder bauhausUriBuilder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DdiResources ddiResources = new DdiResources(
                ddiService,
                ddi4toDdi3ConverterService,
                ddi3toDdi4ConverterService,
                ddiItemConvertService,
                userProvider,
                rbacFetcher,
                bauhausUriBuilder,
                mock(Ddi4SchemaService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(ddiResources)
                .setControllerAdvice(new DdiExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnNotFoundWithMessageWhenStudyUnitIsMissing() throws Exception {
        when(ddiService.updateFullPhysicalInstance(eq("fr.insee"), eq("pi-111"), any()))
                .thenThrow(new StudyUnitNotFoundException("No study unit found for physical instance fr.insee/pi-111"));

        mockMvc.perform(put("/ddi/physical-instance/fr.insee/pi-111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No study unit found for physical instance fr.insee/pi-111"));
    }

    @Test
    void shouldReturnConflictWithMessageWhenPatchedInstanceHasNoScheme() throws Exception {
        String message = "L'opération (StudyUnit fr.insee/su-1) n'a pas de VariableScheme pour ranger ses variables";
        when(ddiService.updatePhysicalInstance(eq("fr.insee"), eq("pi-111"), any()))
                .thenThrow(new MissingSchemeException(
                        MissingSchemeException.Code.STUDY_UNIT_MISSING_VARIABLE_SCHEME,
                        Map.of("studyUnit", "fr.insee/su-1"),
                        message));

        mockMvc.perform(patch("/ddi/physical-instance/fr.insee/pi-111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.code").value("STUDY_UNIT_MISSING_VARIABLE_SCHEME"))
                .andExpect(jsonPath("$.params.studyUnit").value("fr.insee/su-1"));
    }

    @Test
    void shouldReturnConflictWithMessageWhenSavedInstanceHasNoScheme() throws Exception {
        String message = "La série (Group fr.insee/group-1) n'a pas de CodeListScheme pour ranger ses listes de codes";
        when(ddiService.updateFullPhysicalInstance(eq("fr.insee"), eq("pi-111"), any()))
                .thenThrow(new MissingSchemeException(
                        MissingSchemeException.Code.GROUP_MISSING_CODE_LIST_SCHEME,
                        Map.of("group", "fr.insee/group-1"),
                        message));

        mockMvc.perform(put("/ddi/physical-instance/fr.insee/pi-111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.code").value("GROUP_MISSING_CODE_LIST_SCHEME"))
                .andExpect(jsonPath("$.params.group").value("fr.insee/group-1"));
    }
}
