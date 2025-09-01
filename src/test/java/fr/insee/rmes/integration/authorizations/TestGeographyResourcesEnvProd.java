package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.GeographyResources;
import org.json.JSONObject;
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
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "logging.level.org.springframework.web=DEBUG"}
)
class TestGeographyResourcesEnvProd extends AbstractResourcesEnvProd {

    @MockitoBean
    GeographyService geographyService;

    @Autowired
    private MockMvc mvc;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/geo/territories", 200, true, true),
                Arguments.of("/geo/territory/1", 200, true, true),

                Arguments.of("/geo/territories", 403, true, false),
                Arguments.of("/geo/territory/1", 403, true, false),

                Arguments.of("/geo/territories", 401, false, true),
                Arguments.of("/geo/territory/1", 401, false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.GEOGRAPHY.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        when(geographyService.getGeoFeatureById(any())).thenReturn(new JSONObject());
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPostEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),

                Arguments.of(403, true, false),

                Arguments.of(401, false, true)
        );
    }


    @MethodSource("provideDataForPostEndpoints")
    @ParameterizedTest
    void create(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.GEOGRAPHY.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = post("/geo/territory")
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
        when(checker.hasAccess(eq(RBAC.Module.GEOGRAPHY.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/geo/territory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}
