package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.concepts.concept.webservice.ConceptsResources;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = ConceptsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=concepts",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        ConceptsResources.class,
        UserAuthTestConfiguration.class
})
class ConceptsAuthorizationTest extends AbstractResourcesEnvProd {
    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    ConceptsService conceptsService;
    @MockitoBean
    ConceptsCollectionService conceptsCollectionService;

    static String conceptVersion="16";
    static String id ="2025";


    @ParameterizedTest
    @MethodSource("TestGetEndpointsOkWhenAnyRole")
    void getObjectWithAnyRole(String url) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(anyString(), anyString(), any(), any())).thenReturn(true);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    static Collection<Arguments> TestGetEndpointsOkWhenAnyRole(){
        return Arrays.asList(
                Arguments.of("/concepts"),
                Arguments.of("/concepts/toValidate"),
                Arguments.of("/concepts/concept/"+id+"/notes/16"+conceptVersion),
                Arguments.of("/concepts/concept/"+id+"/links"),
                Arguments.of("/concepts/concept/export/"+id),
                Arguments.of("/concepts/collections/toValidate"),
                Arguments.of("/concepts/collections/dashboard"),
                Arguments.of("/concepts/collection/"+id+"/members"),
                Arguments.of("/concepts/collection/export/"+id),
                Arguments.of("/concepts/advanced-search"),
                Arguments.of("/concepts/concept/"+id)
        );
    }

    @ParameterizedTest
    @MethodSource("TestRoleCaseForUpdateCollection")
    void updateCollection(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/concepts/collection/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> TestRoleCaseForUpdateCollection() {
        return Arrays.asList(
                Arguments.of(204, true),
                Arguments.of(403, false)
        );
    }

    @ParameterizedTest
    @MethodSource("TestRoleCaseForPublishConcept")
    void publishConcept(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/concepts/c1116/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }

    static Collection<Arguments> TestRoleCaseForPublishConcept() {
        return Arrays.asList(
                Arguments.of(204, true),
                Arguments.of(403, false)
        );
    }
}
