package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/UserResourcesEnvHorsProdTest.java
=======
import fr.insee.rmes.infrastructure.rbac.Roles;
>>>>>>> 16aff9c1 (refactor: delete CNIS role (#985)):src/test/java/fr/insee/rmes/infrastructure/webservice/UserResourcesEnvHorsProdTest.java
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
<<<<<<< HEAD:module-bauhaus-bo/src/test/java/fr/insee/rmes/infrastructure/webservice/UserResourcesEnvHorsProdTest.java
import fr.insee.rmes.infrastructure.rbac.Roles;
import fr.insee.rmes.onion.infrastructure.stamps.RmesStampsImpl;
=======
import fr.insee.rmes.onion.infrastructure.stamps.RmesStampsImpl;
import fr.insee.rmes.onion.infrastructure.webservice.UserResources;
>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/test/java/fr/insee/rmes/infrastructure/webservice/UserResourcesEnvHorsProdTest.java
import fr.insee.rmes.rbac.RbacFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=HORSPROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        RmesStampsImpl.class,
        FakeUserConfiguration.class})
class UserResourcesEnvHorsProdTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RbacFetcher rbacService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final String FAKE_STAMP_ANSWER = "{\"stamp\": \"fakeStampForDvAndQf\"}";

    @Test
    void getStamp_authent() throws Exception {
        String idep = "xxxxux";
        String timbre = "XX59-YYY";
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(get("/users/stamp").header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(FAKE_STAMP_ANSWER));
    }

    @Test
    void getStamp_anonymous() throws Exception {
        mvc.perform(get("/users/stamp")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(FAKE_STAMP_ANSWER));
    }
}