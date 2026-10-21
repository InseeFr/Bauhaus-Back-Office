package fr.insee.rmes.modules.users;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
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
 * Hors PROD, l'utilisateur porté par le {@code DevAuthenticationFilter} se règle par propriétés :
 * on peut ainsi rejouer en local le parcours d'un gestionnaire limité à son timbre.
 */
@WebMvcTest(
        controllers = UserResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({
    UserConfiguration.class,
    ConfiguredDevUserIntegrationTest.TestConfig.class,
    JwtProperties.class,
    RoleClaimExtractor.class,
    PropertiesRbacFetcher.class
})
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.env=local",
            "fr.insee.rmes.bauhaus.cors.allowedOrigin=http://localhost",
            "fr.insee.rmes.bauhaus.dev-user.roles=Gestionnaire_variables_RMESGNCS",
            "fr.insee.rmes.bauhaus.dev-user.stamps=HIE2004993",
            "fr.insee.rmes.bauhaus.dev-user.source=ssm",
            "jwt.id-claim=preferred_username",
            "jwt.source-claim=source",
            "jwt.role-claim=realm_access",
            "jwt.role-claim-config.roles=roles",
            "rbac.config.Gestionnaire_variables_RMESGNCS.ddi_physicalinstance.read=STAMP",
            "rbac.config.Administrateur_RMESGNCS.ddi_physicalinstance.read=ALL"
        })
class ConfiguredDevUserIntegrationTest {

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
    void should_authenticate_requests_with_the_configured_stamp() throws Exception {
        mvc.perform(get("/users/stamp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stamp").value("HIE2004993"));
    }

    @Test
    void should_grant_the_privileges_of_the_configured_role() throws Exception {
        mvc.perform(get("/users/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$[?(@.application == 'DDI_PHYSICALINSTANCE')].privileges[?(@.privilege == 'READ')].strategy")
                        .value("STAMP"));
    }

    @Test
    void should_replace_the_configured_stamps_with_the_dev_user_header() throws Exception {
        mvc.perform(get("/users/stamp").header("X-Dev-User-Stamps", "HIE2000331"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stamp").value("HIE2000331"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void should_replace_the_configured_roles_with_the_dev_user_header() throws Exception {
        mvc.perform(get("/users/info").header("X-Dev-User-Roles", "Administrateur_RMESGNCS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$[?(@.application == 'DDI_PHYSICALINSTANCE')].privileges[?(@.privilege == 'READ')].strategy")
                        .value("ALL"));
    }

    @Test
    void should_replace_the_configured_source_with_the_dev_user_header() throws Exception {
        mvc.perform(get("/users/info").header("X-Dev-User-Source", "insee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$[?(@.application == 'DDI_PHYSICALINSTANCE')].privileges[?(@.privilege == 'READ')].strategy")
                        .value("ALL"));
    }

    @Test
    void should_accept_several_comma_separated_roles_and_stamps() throws Exception {
        mvc.perform(get("/users/stamp").header("X-Dev-User-Stamps", "HIE2000331, HIE9990001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].stamp").value(containsInAnyOrder("HIE2000331", "HIE9990001")));
    }
}
