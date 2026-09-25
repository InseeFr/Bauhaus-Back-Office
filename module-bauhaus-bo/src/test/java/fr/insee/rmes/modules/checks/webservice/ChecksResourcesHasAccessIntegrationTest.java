package fr.insee.rmes.modules.checks.webservice;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.checks.domain.port.clientside.CheckerService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import java.util.List;
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

@WebMvcTest(
        controllers = ChecksResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({ChecksResources.class, UserAuthTestConfiguration.class})
class ChecksResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private CheckerService checkerService;

    private static Stream<Arguments> provideChecksData() {
        return Stream.of(Arguments.of(200, true), Arguments.of(403, false));
    }

    /**
     * Les contrôles portent sur les concepts ; seul le privilège ADMINISTRATION du module
     * CONCEPT_CONCEPT, détenu par le seul rôle administrateur dans rbac.yml, y donne accès.
     */
    @MethodSource("provideChecksData")
    @ParameterizedTest
    void runAllChecks_requiresConceptAdministrationPrivilege(Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        when(checkerService.checks()).thenReturn(List.of());
        when(checker.hasAccess(eq("CONCEPT_CONCEPT"), eq("ADMINISTRATION"), any(), any()))
                .thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get("/checks").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
