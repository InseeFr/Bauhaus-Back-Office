package fr.insee.rmes.integration;


import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.structures.structures.webservice.StructureResources;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StructureResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        StructureResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class StructureResourcesTest extends AbstractResourcesEnvProd{
    @Autowired
    private MockMvc mvc;

    @MockitoSpyBean
    StructureService structureService;

    @Test
    void xssInjection_shouldBeEscaped() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        String maliciousId = "&lt;script&gt;alert(1);&lt;/script&gt;";
        mvc.perform(delete("/structures/structure/" + maliciousId).header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().bytes(new byte[0]));
        verify(structureService, times(0)).deleteStructure(anyString());
    }



    @TestConfiguration
    static class ConfigurationForTest {
        @Bean
        StructureService structureService() {
            return Mockito.mock(StructureService.class);
        }
    }

}
