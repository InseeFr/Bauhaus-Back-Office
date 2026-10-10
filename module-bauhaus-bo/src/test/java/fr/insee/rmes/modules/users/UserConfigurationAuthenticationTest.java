package fr.insee.rmes.modules.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.RoleClaimExtractor;
import org.junit.jupiter.api.Nested;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The real {@link UserConfiguration} filter chain, driven by {@code fr.insee.rmes.bauhaus.env}: only
 * an explicit development value hands every request the fake ADMIN user, any other platform
 * requires a token.
 */
@WebMvcTest(
        controllers = UserConfigurationAuthenticationTest.ProbeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({
    UserConfigurationAuthenticationTest.ProbeController.class,
    UserConfigurationAuthenticationTest.TestConfig.class,
    UserConfiguration.class,
    JwtProperties.class,
    RoleClaimExtractor.class
})
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.cors.allowedOrigin=http://localhost:3000")
class UserConfigurationAuthenticationTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    OrganisationsService organisationsService;

    @MockitoBean
    RbacFetcher rbacFetcher;

    @MockitoBean
    StampChecker stampChecker;

    @Autowired
    MockMvc mvc;

    @Nested
    @TestPropertySource(properties = "fr.insee.rmes.bauhaus.env=pre-prod")
    class OnPreProduction {

        @Test
        void a_request_without_token_is_rejected() throws Exception {
            mvc.perform(get("/probe")).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @TestPropertySource(properties = "fr.insee.rmes.bauhaus.env=NoAuth")
    class OnLocalDevelopment {

        @Test
        void a_request_without_token_is_let_through_as_the_fake_user() throws Exception {
            mvc.perform(get("/probe")).andExpect(status().isOk());
        }
    }

    @RestController
    static class ProbeController {

        @GetMapping("/probe")
        String probe() {
            return "ok";
        }
    }

    @TestConfiguration
    @EnableConfigurationProperties(BauhausConfiguration.class)
    static class TestConfig {}
}
