package fr.insee.rmes.integration.authorizations;


import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.datasets.distributions.webservice.DistributionResources;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
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
        controllers = DistributionResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=datasets",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        DistributionResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class TestDistributionsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private DatasetService datasetService;
    @MockitoBean
    private DistributionService distributionService;

    int distributionId =10;

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/distribution", 200, true),
                Arguments.of("/distribution/1", 200, true),

                Arguments.of("/distribution", 403, false),
                Arguments.of("/distribution/1", 403, false)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
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
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/distribution")
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

        var request = put("/distribution/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPublishEndpoints() {
        return Stream.of(
                Arguments.of(200, true),

                Arguments.of(403, false)
        );
    }


    @MethodSource("provideDataForPublishEndpoints")
    @ParameterizedTest
    void publish(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/distribution/1/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN_VALUE)
                .content("{\"id\": \"1\"}");

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDeleteEndpoints() {
        return Stream.of(
                Arguments.of(200, true),

                Arguments.of(403, false)
        );
    }


    @MethodSource("provideDataForDeleteEndpoints")
    @ParameterizedTest
    void deleteDistribution(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = delete("/distribution/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
