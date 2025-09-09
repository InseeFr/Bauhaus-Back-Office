package fr.insee.rmes.integration;

import fr.insee.rmes.bauhaus_services.classifications.ClassificationsService;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.rbac.AccessPrivilegesChecker;
import fr.insee.rmes.onion.infrastructure.webservice.classifications.ClassificationsResources;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClassificationsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=classifications",
                "fr.insee.rmes.bauhaus.baseGraph=http://",
                "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
                "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
                "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
                "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
                "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
                "fr.insee.rmes.bauhaus.adms.graph=adms",
                "fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI=identifiantsAlternatifs/jeuDeDonnees",
                "fr.insee.rmes.bauhaus.lg1=fr",
                "fr.insee.rmes.bauhaus.lg2=en"}

)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class,
        AccessPrivilegesChecker.class
})
class ClassificationsResourcesTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtDecoder jwtDecoder;
        @MockitoBean
    AccessPrivilegesChecker accessPrivilegesChecker;

    @MockitoSpyBean
    ClassificationsService classificationsService;
    @MockitoBean
    ClassificationItemService classificationItemService;
    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    @Test
    void xssInjection_shouldBeEscaped() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        String maliciousId = "&lt;script&gt;alert(1);&lt;/script&gt;";
        mvc.perform(put("/classifications/classification/" + maliciousId + "/validate").header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().bytes(new byte[0]));
        verify(classificationsService, times(0)).setClassificationValidation(anyString());
    }

    @TestConfiguration
    static class ConfigurationForTest {
        @Bean
        ClassificationsService classificationsService() {
            return Mockito.mock(ClassificationsService.class);
        }
    }

}