package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.geographies.webservice.GeographyResources;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GeographyResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        GeographyResources.class,
        UserAuthTestConfiguration.class
})
class TestGeographyResourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    GeographyService geographyService;


    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/geo/territories", 200, true),
                Arguments.of("/geo/territory/1", 200, true),

                Arguments.of("/geo/territories", 403, false),
                Arguments.of("/geo/territory/1", 403, false)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        when(geographyService.getGeoFeatureById(any())).thenReturn(new JSONObject());
        when(geographyService.getGeoFeatures()).thenReturn("[]");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");


        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPostEndpoints() {
        return Stream.of(
                Arguments.of(201, true),
                Arguments.of(403, false)
        );
    }


    @MethodSource("provideDataForPostEndpoints")
    @ParameterizedTest
    void create(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);        when(geographyService.createFeature(any())).thenReturn("http://bauhaus/geo/territory/test-id");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = post("/geo/territory")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }


    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void update(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/geo/territory/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }
}
