package fr.insee.rmes.integration.authorizations;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.structures.components.webservice.ComponentResources;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collections;
import java.util.List;
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

@WebMvcTest(
        controllers = ComponentResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
            "fr.insee.rmes.bauhaus.modules.structures.enabled=true",
            "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({ComponentResources.class, UserAuthTestConfiguration.class})
class TestComponentsResourcesEnvProd extends AbstractResourcesEnvProd {
    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private StructureService structureService;

    @MockitoBean
    private StructureComponent structureComponentService;

    /**
     * Corps valide : la validation du @RequestBody passe avant le contrôle RBAC, un corps
     * incomplet répondrait 400 sans atteindre le contrôle testé ici — y compris dans le cas 403.
     */
    private static final String COMPONENT_BODY = """
            {"identifiant": "NOTATION", "labelLg1": "Composant", "labelLg2": "Component",
             "type": "http://purl.org/linked-data/cube#DimensionProperty",
             "contributor": ["DG75-L201", "DG75-G401"]}""";

    private static Stream<Arguments> provideCreationData() {
        return Stream.of(Arguments.of(201, true), Arguments.of(403, false));
    }

    @MethodSource("provideCreationData")
    @ParameterizedTest
    void create(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccess(any(), any(), any())).thenReturn(hasAccessReturn);
        when(structureComponentService.createComponent(any())).thenReturn("c1001");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/structures/components")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(COMPONENT_BODY);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    /**
     * Un composant n'existe pas encore à sa création : le contrôle par timbre porte sur ses
     * gestionnaires postés ({@code dc:contributor}), que l'utilisateur doit compter parmi ses
     * organisations.
     */
    @Test
    void checks_the_creation_access_on_the_posted_contributors() throws Exception, MissingUserInformationException {
        when(checker.hasCreateAccess(eq("STRUCTURE_COMPONENT"), eq(List.of("DG75-L201", "DG75-G401")), any()))
                .thenReturn(true);
        when(structureComponentService.createComponent(any())).thenReturn("c1001");
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/structures/components")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(COMPONENT_BODY);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().isCreated());
    }
}
