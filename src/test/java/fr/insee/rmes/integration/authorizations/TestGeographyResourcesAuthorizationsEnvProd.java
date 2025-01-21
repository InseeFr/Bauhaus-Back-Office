package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.GeographyResources;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GeographyResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
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
        UserProviderFromSecurityContext.class})
class TestGeographyResourcesAuthorizationsEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GeographyService geographyService;

    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;


    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    @Test
    void getTerritoriesAuthentified_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));

        mvc.perform(get("/geo/territories").header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getTerritoriesUnauthentified_401() throws Exception {
        mvc.perform(get("/geo/territories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postTerritoriesUnauthentified_401() throws Exception {
        mvc.perform(post("/geo/territory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postTerritoriesNoAdmin_403() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));
        mvc.perform(post("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());

    }

    @Test
    void postTerritoriesAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));

        mvc.perform(post("/geo/territory").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void optionCreateTerritoriesNoAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bidon"));

        mvc.perform(options("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void optionCreateTerritoriesUnauthentified_ok() throws Exception {
        mvc.perform(options("/geo/territory")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void optionCreateTerritoriesAsAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(options("/geo/territory")
                        .header("Authorization", "Bearer toto")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
