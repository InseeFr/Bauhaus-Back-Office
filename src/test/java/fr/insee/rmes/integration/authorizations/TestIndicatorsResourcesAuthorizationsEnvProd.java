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
import fr.insee.rmes.webservice.operations.IndicatorsResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IndicatorsResources.class,
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
class TestIndicatorsResourcesAuthorizationsEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService operationsDocumentationsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;
    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    @ValueSource(strings = {
            "/operations/indicators/",
            "/operations/indicators/withSims",
            "/operations/indicators/advanced-search",
            "/operations/indicator/1"
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
            "/operations/indicators/",
            "/operations/indicators/withSims",
            "/operations/indicators/advanced-search",
            "/operations/indicator/1"
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
    void postIndicatorWhenAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(post("/operations/indicator").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isOk());
    }

    @Test
    void postIndicatorWhenIndicatorContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.INDICATOR_CONTRIBUTOR));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(post("/operations/indicator").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isOk());
    }

    @Test
    void postIndicatorWhenIndicatorFakeRole_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("fake"));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(post("/operations/indicator").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isForbidden());
    }

    @Test
    void putIndicatorWhenAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void putIndicatorWhenIndicatorContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.INDICATOR_CONTRIBUTOR));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void putIndicatorWhenIndicatorFakeRole_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("fake"));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"} "))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateIndicatorWhenAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/validate/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void validateIndicatorWhenIndicatorContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.INDICATOR_CONTRIBUTOR));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/validate/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void validateIndicatorWhenIndicatorFakeRole_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("fake"));
        when(operationsService.setIndicator(anyString())).thenReturn("1");
        mvc.perform(put("/operations/indicator/validate/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
