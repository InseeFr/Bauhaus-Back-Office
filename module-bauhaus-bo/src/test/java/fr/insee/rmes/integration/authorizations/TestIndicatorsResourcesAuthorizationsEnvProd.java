package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.operations.indicators.webservice.IndicatorsResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IndicatorsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stampClaim=" + STAMP_CLAIM,
                "jwt.roleClaim=" + ROLE_CLAIM,
                "jwt.idClaim=" + ID_CLAIM,
                "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "jwt.sourceClaim=source",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE"}
)
@Import(JwtProperties.class)
class TestIndicatorsResourcesAuthorizationsEnvProd extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService operationsDocumentationsService;


    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    private static Stream<Arguments> provideIndicatorDataGet() {
        return Stream.of(
                Arguments.of("/operations/indicators/", 200, true, true),
                Arguments.of("/operations/indicators/", 403, true, false),
                Arguments.of("/operations/indicators/",401, false, true),

                Arguments.of("/operations/indicators/withSims", 200, true, true),
                Arguments.of("/operations/indicators/withSims", 403, true, false),
                Arguments.of("/operations/indicators/withSims",401, false, true),

                Arguments.of("/operations/indicators/advanced-search", 200, true, true),
                Arguments.of("/operations/indicators/advanced-search", 403, true, false),
                Arguments.of("/operations/indicators/advanced-search",401, false, true),

                Arguments.of("/operations/indicator/1", 200, true, true),
                Arguments.of("/operations/indicator/1", 403, true, false),
                Arguments.of("/operations/indicator/1",401, false, true)
        );
    }

    @MethodSource("provideIndicatorDataGet")
    @ParameterizedTest
    void getIndicator(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_INDICATOR.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideIndicatorData() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }

    @MethodSource("provideIndicatorData")
    @ParameterizedTest
    void postIndicator(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_INDICATOR.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/operations/indicator").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"} ");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideIndicatorData")
    @ParameterizedTest
    void putIndicator(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_INDICATOR.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/operations/indicator/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"} ");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
    @MethodSource("provideIndicatorData")
    @ParameterizedTest
    void validateIndicator(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_INDICATOR.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/operations/indicator/1/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"} ");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}
