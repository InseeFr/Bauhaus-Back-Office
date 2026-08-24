package fr.insee.rmes.modules.structures.components.webservice;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = ComponentResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {
                "fr.insee.rmes.bauhaus.modules[0].identifier=structures"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ComponentResourcesDeleteTest {

    @MockitoBean
    private StructureService structureService;

    @MockitoBean
    private StructureComponent structureComponentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void deleteComponent_whenComponentDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("Not Found", "component with unknown not found"))
                .when(structureComponentService).deleteComponent("unknown");

        mockMvc.perform(delete("/structures/components/{id}", "unknown"))
                .andExpect(status().isNotFound());
    }
}
