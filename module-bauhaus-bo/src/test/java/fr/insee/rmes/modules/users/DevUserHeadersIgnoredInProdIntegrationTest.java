package fr.insee.rmes.modules.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.infrastructure.DevAuthenticationFilter;
import fr.insee.rmes.modules.users.infrastructure.DevUserProperties;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.PropertiesRbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.RBACConfiguration;
import fr.insee.rmes.modules.users.infrastructure.RoleClaimExtractor;
import fr.insee.rmes.modules.users.webservice.UserResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * En PROD, l'utilisateur vient du jeton : les en-têtes {@code X-Dev-User-*} du
 * {@code DevAuthenticationFilter} ne doivent jamais authentifier une requête.
 */
@WebMvcTest(
        controllers = UserResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({
    UserConfiguration.class,
    DevUserHeadersIgnoredInProdIntegrationTest.TestConfig.class,
    JwtProperties.class,
    RoleClaimExtractor.class,
    PropertiesRbacFetcher.class
})
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.env=PROD",
            "fr.insee.rmes.bauhaus.cors.allowedOrigin=http://localhost",
            "jwt.id-claim=preferred_username",
            "jwt.source-claim=source",
            "jwt.role-claim=realm_access",
            "jwt.role-claim-config.roles=roles"
        })
class DevUserHeadersIgnoredInProdIntegrationTest {

    @TestConfiguration
    @EnableConfigurationProperties({BauhausConfiguration.class, RBACConfiguration.class, DevUserProperties.class})
    static class TestConfig {}

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private OrganisationsService organisationsService;

    @MockitoBean
    private StampChecker stampChecker;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_reject_a_request_carrying_only_dev_user_headers() throws Exception {
        mvc.perform(get("/users/stamp")
                        .header(DevAuthenticationFilter.ROLES_HEADER, "Administrateur_RMESGNCS")
                        .header(DevAuthenticationFilter.STAMPS_HEADER, "HIE2000331")
                        .header(DevAuthenticationFilter.SOURCE_HEADER, "insee"))
                .andExpect(status().isUnauthorized());
    }
}
