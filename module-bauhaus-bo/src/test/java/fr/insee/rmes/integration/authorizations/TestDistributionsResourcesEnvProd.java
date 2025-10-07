package fr.insee.rmes.integration.authorizations;


import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.config.auth.security.JwtProperties;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.datasets.DistributionResources;
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

@WebMvcTest(controllers = DistributionResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stampClaim=" + STAMP_CLAIM,
                "jwt.roleClaim=" + ROLE_CLAIM,
                "jwt.idClaim=" + ID_CLAIM,
                "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "jwt.sourceClaim=source",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=datasets"}
)
@Import(JwtProperties.class)
class TestDistributionsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private DatasetService datasetService;
    @MockitoBean
    private DistributionService distributionService;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";


    int distributionId =10;

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/distribution", 200, true, true),
                Arguments.of("/distribution/1", 200, true, true),

                Arguments.of("/distribution", 403, true, false),
                Arguments.of("/distribution/1", 403, true, false),

                Arguments.of("/distribution", 401, false, true),
                Arguments.of("/distribution/1", 401, false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DISTRIBUTION.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }



    private static Stream<Arguments> provideDataForPostEndpoints() {
        return Stream.of(
                Arguments.of(201, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForPostEndpoints")
    @ParameterizedTest
    void create(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DISTRIBUTION.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/distribution")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }



    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void update(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DISTRIBUTION.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/distribution/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPublishEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForPublishEndpoints")
    @ParameterizedTest
    void publish(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DISTRIBUTION.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/distribution/1/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN_VALUE)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDeleteEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForDeleteEndpoints")
    @ParameterizedTest
    void deleteDistribution(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DISTRIBUTION.toString()), eq(RBAC.Privilege.DELETE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = delete("/distribution/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}
