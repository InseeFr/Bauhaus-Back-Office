package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.webservice.operations.SeriesResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class TestSeriesResourcesEnvProd extends AbstractResourcesEnvProd  {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";
    int seriesId = 10;

    @ValueSource(strings = {
            "/operations/series/",
            "/operations/series/withSims",
            "/operations/series/1",
            "/operations/series/advanced-search",
            "/operations/series/advanced-search/stamp",
            "/operations/series/1/operationsWithReport",
            "/operations/series/1/operationsWithoutReport"
    })
    @ParameterizedTest
    void getWithAdmin_Ok(String url) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ValueSource(strings = {
            "/operations/series/",
            "/operations/series/withSims",
            "/operations/series/1",
            "/operations/series/advanced-search",
            "/operations/series/advanced-search/stamp",
            "/operations/series/1/operationsWithReport",
            "/operations/series/1/operationsWithoutReport"
    })
    @ParameterizedTest
    void getWithContributor_Ok(String url) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.SERIES_CONTRIBUTOR));
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void putSeriesAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
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
