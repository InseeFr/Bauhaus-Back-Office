package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Vider les caches Colectica est réservé aux administrateurs : l'endpoint exige le privilège
 * ADMINISTRATION du module DDI_PHYSICALINSTANCE, que seul le rôle Administrateur porte (rbac.yml).
 */
@WebMvcTest(
        controllers = DdiResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = "fr.insee.rmes.bauhaus.modules.ddi.enabled=true")
@Import({DdiResources.class, UserAuthTestConfiguration.class})
class DdiResourcesCacheEvictionHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {}

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @MockitoBean
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @MockitoBean
    private DDIItemConvertService ddiItemConvertService;

    @MockitoBean
    private UserProvider userProvider;

    @MockitoBean
    private RbacFetcher rbacFetcher;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    @MockitoBean
    private Ddi4SchemaService ddi4SchemaService;

    @Test
    void administratorEvictsTheCaches() throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("DDI_PHYSICALINSTANCE"), eq("ADMINISTRATION"), any(), any()))
                .thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(delete("/ddi/cache").header("Authorization", "Bearer toto")).andExpect(status().isNoContent());

        verify(ddiService).evictAllCaches();
    }

    @Test
    void userWithoutAdministrationPrivilegeIsForbidden() throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("DDI_PHYSICALINSTANCE"), eq("ADMINISTRATION"), any(), any()))
                .thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        mvc.perform(delete("/ddi/cache").header("Authorization", "Bearer toto")).andExpect(status().isForbidden());

        verify(ddiService, never()).evictAllCaches();
    }
}
