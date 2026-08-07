package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private UriUtils uriUtils;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DdiResources ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService, ddi3toDdi4ConverterService, ddiItemConvertService, userProvider, rbacFetcher, uriUtils, new Ddi4SchemaValidator());
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
}
