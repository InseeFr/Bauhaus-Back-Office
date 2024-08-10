package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.accesscontrol.StampsRestrictionsVerifier;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.webservice.operations.SeriesResources;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
@Import( ConfigurationForTestWithAuth.class)
class TestSeriesResourcesEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OperationsService operationsService;

    @MockBean
    protected OperationsDocumentationsService documentationsService;

    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    @MockBean
    StampsRestrictionsVerifier stampsRestrictionsVerifier;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";
    int seriesId = 10;


    @Test
    void putSeriesAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(put("/operations/series/" + seriesId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putSeriesAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.SERIES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isSeriesManagerWithStamp(String.valueOf(seriesId),new Stamp(timbre))).thenReturn(true);

        mvc.perform(put("/operations/series/" + seriesId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isSeriesManagerWithStamp(String.valueOf(seriesId),new Stamp(timbre));
    }

    @Test
    void putSeriesAsSeriesContributor_badSerieTimbre() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.SERIES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isSeriesManagerWithStamp(String.valueOf(seriesId),new Stamp(timbre))).thenReturn(false);

        mvc.perform(put("/operations/series/" + seriesId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isSeriesManagerWithStamp(String.valueOf(seriesId),new Stamp(timbre));
    }

    @Test
    void putSeriesAsSeriesContributor_noAuth() throws Exception {
        mvc.perform(put("/operations/series/" + seriesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putSeriesAsNotSeriesContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(put("/operations/series/" + seriesId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

}
