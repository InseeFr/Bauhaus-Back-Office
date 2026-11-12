package fr.insee.rmes.integration.authorizations.operations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationExportService;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.webservice.MetadataReportResources;
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
 * La stratégie STAMP compare le timbre de l'utilisateur aux créateurs de la cible du rapport :
 * chaque écriture sur un rapport existant doit donc transmettre son identifiant au contrôle
 * d'accès. Seul cet identifiant est autorisé.
 */
@WebMvcTest(
        controllers = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
            "fr.insee.rmes.bauhaus.modules.operations.enabled=true",
            "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({MetadataReportResources.class, UserAuthTestConfiguration.class})
class TestMetadataReportResourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private OperationsDocumentationsService documentationsService;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private DocumentationService documentationService;

    @MockitoBean
    private DocumentationExportService documentationExportService;

    private static final String SIMS_ID = "1679";

    private static final String SIMS_BODY = """
            {"labelLg1": "Rapport"}""";

    static Stream<Arguments> simsWrites() {
        return Stream.of(
                Arguments.of(
                        "UPDATE", put("/operations/metadataReport/" + SIMS_ID).content(SIMS_BODY)),
                Arguments.of("DELETE", delete("/operations/metadataReport/" + SIMS_ID)),
                Arguments.of("PUBLISH", put("/operations/metadataReport/" + SIMS_ID + "/validate")));
    }

    @ParameterizedTest
    @MethodSource("simsWrites")
    void checks_the_access_on_the_sims_id(String privilege, MockHttpServletRequestBuilder request)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("OPERATION_SIMS"), eq(privilege), eq(SIMS_ID), any()))
                .thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is2xxSuccessful());
    }

    /**
     * Un rapport n'a pas de créateur propre : à sa création, le contrôle par timbre porte sur les
     * créateurs de sa cible postée — la série parente d'une opération, la série ou l'indicateur.
     */
    static Stream<Arguments> simsTargets() {
        return Stream.of(
                Arguments.of("idOperation", "s2098", "OPERATION_OPERATION"),
                Arguments.of("idSeries", "s1001", "OPERATION_SERIES"),
                Arguments.of("idIndicator", "p1001", "OPERATION_INDICATOR"));
    }

    @ParameterizedTest
    @MethodSource("simsTargets")
    void checks_the_creation_access_on_the_posted_target(String targetField, String targetId, String targetModule)
            throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccessUnder(eq("OPERATION_SIMS"), eq(targetModule), eq(targetId), any()))
                .thenReturn(true);
        when(documentationsService.createMetadataReport(any())).thenReturn("1680");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .content("""
                                {"labelLg1": "Rapport", "%s": "%s"}""".formatted(targetField, targetId)))
                .andExpect(status().isOk());
    }

    @Test
    void refuses_the_creation_when_the_target_is_not_owned_by_the_user()
            throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccessUnder(any(), any(), any(), any())).thenReturn(false);
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .content("""
                                {"labelLg1": "Rapport", "idSeries": "s1001"}"""))
                .andExpect(status().isForbidden());
    }

    /** Un rapport sans cible n'a aucun propriétaire dont hériter : sa création est refusée. */
    @Test
    void refuses_the_creation_of_a_report_without_target() throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccessUnder(any(), any(), any(), any())).thenReturn(true);
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(true);
        when(documentationsService.createMetadataReport(any())).thenReturn("1680");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer toto")
                        .content("""
                                {"labelLg1": "Rapport", "idOperation": "", "idSeries": "", "idIndicator": ""}"""))
                .andExpect(status().isForbidden());
    }
}
