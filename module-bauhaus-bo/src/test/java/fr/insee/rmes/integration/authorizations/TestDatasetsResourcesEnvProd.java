package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.config.auth.security.JwtProperties;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.datasets.DatasetResources;
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

@WebMvcTest(controllers = DatasetResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stampClaim=" + STAMP_CLAIM,
                "jwt.roleClaim=" + ROLE_CLAIM,
                "jwt.idClaim=" + ID_CLAIM,
                "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "jwt.sourceClaim=source",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "logging.level.org.springframework.web=DEBUG",
                "fr.insee.rmes.bauhaus.activeModules=datasets"}
)
@Import(JwtProperties.class)
class TestDatasetsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    DatasetService datasetService;


    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/datasets", 200, true, true),
                Arguments.of("/datasets/1", 200, true, true),
                Arguments.of("/datasets/1/distributions", 200, true, true),

                Arguments.of("/datasets", 403, true, false),
                Arguments.of("/datasets/1", 403, true, false),
                Arguments.of("/datasets/1/distributions", 403, true, false),

                Arguments.of("/datasets", 401, false, true),
                Arguments.of("/datasets/1", 401, false, true),
                Arguments.of("/datasets/1/distributions", 401, false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
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
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = post("/datasets")
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
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/datasets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN_VALUE)
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
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/datasets/1/validate")
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
    void deleteDataset(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.DELETE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = delete("/datasets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPatchEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForPatchEndpoints")
    @ParameterizedTest
    void patchDataset(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.DATASET_DATASET.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = patch("/datasets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN_VALUE)
                .content("{\"numObservations\": 1}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}
