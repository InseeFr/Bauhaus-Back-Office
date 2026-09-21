package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
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
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

@WebMvcTest(
        controllers = GroupResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
@Import({GroupResources.class, UserAuthTestConfiguration.class})
class GroupResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private RbacFetcher rbacFetcher;

    private static Stream<Arguments> provideGetGroupsData() {
        return Stream.of(Arguments.of(200, true), Arguments.of(403, false));
    }

    private static Stream<Arguments> providePostGroupsData() {
        return Stream.of(Arguments.of(201, true), Arguments.of(403, false));
    }

    @MethodSource("provideGetGroupsData")
    @ParameterizedTest
    void getGroups_requiresDdiReadPrivilege(Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        when(groupService.getAll()).thenReturn(List.of());

        var request = get("/ddi/groups").accept(MediaType.APPLICATION_JSON);

        assertStatusWhenGranted(request, "READ", code, hasAccessReturn);
    }

    @MethodSource("providePostGroupsData")
    @ParameterizedTest
    void postGroups_requiresDdiCreatePrivilege(Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        var request = post("/ddi/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"$type\": \"Group\", \"ID\": \"group-1\", \"Agency\": \"fr.insee\"}");

        assertStatusWhenGranted(request, "CREATE", code, hasAccessReturn);
    }

    /**
     * Seul le couple (DDI_PHYSICALINSTANCE, {@code privilege}) répond {@code hasAccessReturn} ; tout
     * autre couple est refusé, si bien qu'une annotation visant un autre module échoue au cas 200/201.
     */
    private void assertStatusWhenGranted(
            AbstractMockHttpServletRequestBuilder<?> request, String privilege, Integer code, boolean hasAccessReturn)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq("DDI_PHYSICALINSTANCE"), eq(privilege), any(), any()))
                .thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}
