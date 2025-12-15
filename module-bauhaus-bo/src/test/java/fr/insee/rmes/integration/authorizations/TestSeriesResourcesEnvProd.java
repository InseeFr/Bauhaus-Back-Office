package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.series.webservice.SeriesResources;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = SeriesResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        SeriesResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class TestSeriesResourcesEnvProd extends AbstractResourcesEnvProd  {
    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }
    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    int seriesId = 10;

    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/operations/series", 200, true),
                Arguments.of("/operations/series/withSims", 200, true),
                Arguments.of("/operations/series/1", 200, true),
                Arguments.of("/operations/series/advanced-search", 200, true),
                Arguments.of("/operations/series/advanced-search/stamp", 200, true),
                Arguments.of("/operations/series/1/operationsWithReport", 200, true),
                Arguments.of("/operations/series/1/operationsWithoutReport", 200, true),

                Arguments.of("/operations/series", 403, false),
                Arguments.of("/operations/series/withSims", 403, false),
                Arguments.of("/operations/series/1", 403, false),
                Arguments.of("/operations/series/advanced-search", 403, false),
                Arguments.of("/operations/series/advanced-search/stamp", 403, false),
                Arguments.of("/operations/series/1/operationsWithReport", 403, false),
                Arguments.of("/operations/series/1/operationsWithoutReport", 403, false)

        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getSeries(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
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
    void updateSeries(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/operations/series/1").header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
