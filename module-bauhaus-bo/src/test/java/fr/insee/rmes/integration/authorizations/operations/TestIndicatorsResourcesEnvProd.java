package fr.insee.rmes.integration.authorizations.operations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.indicators.webservice.IndicatorsResources;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import java.util.stream.Stream;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * La stratégie STAMP compare le timbre de l'utilisateur aux créateurs (dc:creator) de l'indicateur :
 * chaque écriture sur un indicateur existant doit donc transmettre son identifiant au contrôle
 * d'accès. Seul cet identifiant est autorisé.
 */
@WebMvcTest(
        controllers = IndicatorsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
            "fr.insee.rmes.bauhaus.modules.operations.enabled=true",
            "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({IndicatorsResources.class, UserAuthTestConfiguration.class})
class TestIndicatorsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private OperationsService operationsService;

    private static final String INDICATOR_ID = "p1";

    private static final String INDICATOR_BODY = """
            {"prefLabelLg1": "Indicateur"}""";

    static Stream<Arguments> indicatorWrites() {
        return Stream.of(
                Arguments.of(
                        "UPDATE", put("/operations/indicator/" + INDICATOR_ID).content(INDICATOR_BODY)),
                Arguments.of("PUBLISH", put("/operations/indicator/" + INDICATOR_ID + "/validate")));
    }

    @ParameterizedTest
    @MethodSource("indicatorWrites")
    void checks_the_access_on_the_indicator_id(String privilege, MockHttpServletRequestBuilder request)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("OPERATION_INDICATOR"), eq(privilege), eq(INDICATOR_ID), any()))
                .thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is2xxSuccessful());
    }
}
