package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.operations.SeriesResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SeriesResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=operations"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class})
public class TestSeriesResourcesAuthorizationsEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OperationsService operationsService;

    @MockBean
    private OperationsDocumentationsService operationsDocumentationsService;

    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;
    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    @Test
    void postIndicatorWhenAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/operations/series").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isOk());
    }

    @Test
    void postIndicatorWhenIndicatorFakeRole_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("fake"));
        mvc.perform(post("/operations/series").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isForbidden());
    }

}
