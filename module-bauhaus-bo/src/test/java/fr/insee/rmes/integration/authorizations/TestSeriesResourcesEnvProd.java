package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.operations.SeriesResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/operations/series/", 200, true, true),
                Arguments.of("/operations/series/withSims", 200, true, true),
                Arguments.of("/operations/series/1", 200, true, true),
                Arguments.of("/operations/series/advanced-search", 200, true, true),
                Arguments.of("/operations/series/advanced-search/stamp", 200, true, true),
                Arguments.of("/operations/series/1/operationsWithReport", 200, true, true),
                Arguments.of("/operations/series/1/operationsWithoutReport", 200, true, true),

                Arguments.of("/operations/series/", 403, true, false),
                Arguments.of("/operations/series/withSims", 403, true, false),
                Arguments.of("/operations/series/1", 403, true, false),
                Arguments.of("/operations/series/advanced-search", 403, true, false),
                Arguments.of("/operations/series/advanced-search/stamp", 403, true, false),
                Arguments.of("/operations/series/1/operationsWithReport", 403, true, false),
                Arguments.of("/operations/series/1/operationsWithoutReport", 403, true, false),

                Arguments.of("/operations/series/", 401, false, true),
                Arguments.of("/operations/series/withSims", 401, false, true),
                Arguments.of("/operations/series/1", 401, false, true),
                Arguments.of("/operations/series/advanced-search", 401, false, true),
                Arguments.of("/operations/series/advanced-search/stamp", 401, false, true),
                Arguments.of("/operations/series/1/operationsWithReport", 401, false, true),
                Arguments.of("/operations/series/1/operationsWithoutReport", 401, false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getSeries(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_SERIES.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false)
        );
    }


    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void updateSeries(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_SERIES.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/operations/series/1").header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}
