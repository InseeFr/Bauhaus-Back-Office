package fr.insee.rmes.modules.users;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.modules.users.infrastructure.PropertiesRbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.RBACConfiguration;
import fr.insee.rmes.modules.users.infrastructure.RoleClaimExtractor;
import fr.insee.rmes.modules.users.webservice.UserResources;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Hors PROD, l'API n'est pas authentifiée : chaque requête est portée par le FAKE_USER du
 * {@code DevAuthenticationFilter}. Un jeton reçu malgré tout — typiquement celui qu'un front
 * obtient par SSO silencieux sur une session Keycloak ouverte en prod — ne doit pas substituer
 * l'identité de son porteur à celle du FAKE_USER.
 */
@WebMvcTest(
        controllers = UserResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({
    UserConfiguration.class,
    DevAuthenticationIntegrationTest.TestConfig.class,
    JwtProperties.class,
    RoleClaimExtractor.class,
    PropertiesRbacFetcher.class
})
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.env=local",
            "fr.insee.rmes.bauhaus.cors.allowedOrigin=http://localhost",
            "jwt.id-claim=preferred_username",
            "jwt.source-claim=source",
            "jwt.role-claim=realm_access",
            "jwt.role-claim-config.roles=roles",
            "rbac.config.Administrateur_RMESGNCS.concept_concept.administration=ALL"
        })
class DevAuthenticationIntegrationTest {

    @TestConfiguration
    @EnableConfigurationProperties({BauhausConfiguration.class, RBACConfiguration.class})
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
    void should_keep_the_fake_user_privileges_when_a_bearer_token_is_sent_outside_prod() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(prodUserWithoutBauhausRole());

        mvc.perform(get("/users/info").header("Authorization", "Bearer prod-sso-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$[?(@.application == 'CONCEPT_CONCEPT')].privileges[?(@.privilege == 'ADMINISTRATION')].strategy")
                        .value("ALL"));
    }

    private static Jwt prodUserWithoutBauhausRole() {
        return Jwt.withTokenValue("prod-sso-token")
                .header("alg", "RS256")
                .claim("preferred_username", "prod-user")
                .claim("source", "insee")
                .claim("realm_access", Map.of("roles", List.of("offline_access")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
