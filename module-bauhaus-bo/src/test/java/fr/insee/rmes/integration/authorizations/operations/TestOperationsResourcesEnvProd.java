package fr.insee.rmes.integration.authorizations.operations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.operations.operations.webservice.OperationsResources;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
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
 * La stratégie STAMP compare le timbre de l'utilisateur aux créateurs de la série parente de
 * l'opération : chaque écriture sur une opération existante doit donc transmettre son identifiant
 * au contrôle d'accès. Seul cet identifiant est autorisé.
 */
@WebMvcTest(
        controllers = OperationsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
            "fr.insee.rmes.bauhaus.modules.operations.enabled=true",
            "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({OperationsResources.class, UserAuthTestConfiguration.class})
class TestOperationsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService documentationsService;

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    private static final String OPERATION_ID = "s42-op1";

    private static final String OPERATION_BODY = """
            {"prefLabelLg1": "Opération", "series": {"id": "s42"}}""";

    static Stream<Arguments> operationWrites() {
        return Stream.of(
                Arguments.of(
                        "UPDATE", put("/operations/operation/" + OPERATION_ID).content(OPERATION_BODY)),
                Arguments.of("PUBLISH", put("/operations/operation/" + OPERATION_ID + "/validate")));
    }

    @ParameterizedTest
    @MethodSource("operationWrites")
    void checks_the_access_on_the_operation_id(String privilege, MockHttpServletRequestBuilder request)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("OPERATION_OPERATION"), eq(privilege), eq(OPERATION_ID), any()))
                .thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is2xxSuccessful());
    }

    /**
     * Une opération n'a pas de créateur propre : à sa création, le contrôle par timbre porte sur
     * les créateurs de la série parente postée, que l'utilisateur doit compter parmi ses
     * organisations.
     */
    @Test
    void checks_the_creation_access_on_the_posted_parent_series() throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccessUnder(eq("OPERATION_OPERATION"), eq("OPERATION_SERIES"), eq("s42"), any()))
                .thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(post("/operations/operation")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(OPERATION_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void refuses_the_creation_when_the_parent_series_is_not_owned_by_the_user()
            throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccessUnder(any(), any(), any(), any())).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(post("/operations/operation")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(OPERATION_BODY))
                .andExpect(status().isForbidden());
    }
}
