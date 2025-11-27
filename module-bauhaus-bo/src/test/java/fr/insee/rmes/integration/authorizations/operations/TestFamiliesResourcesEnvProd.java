package fr.insee.rmes.integration.authorizations.operations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.infrastructure.JwtProperties;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.operations.families.webservice.FamilyResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FamilyResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        FamilyResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class TestFamiliesResourcesEnvProd {
    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    protected JwtProperties jwtProperties;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService operationsDocumentationsService;

    @MockitoBean
    private FamilyService familyService;

    @MockitoBean(name = "propertiesAccessPrivilegesChecker")
    protected AccessPrivilegesCheckerService checker;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/operations/families", 200, true),
                Arguments.of("/operations/families/advanced-search", 200, true),
                Arguments.of("/operations/families/1/seriesWithReport", 200, true),
                Arguments.of("/operations/family/1", 200, true),

                Arguments.of("/operations/families", 403, false),
                Arguments.of("/operations/families/advanced-search", 403, false),
                Arguments.of("/operations/families/1/seriesWithReport", 403, false),
                Arguments.of("/operations/family/1", 403, false)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(familyService.getFamily(anyString())).thenReturn(new OperationFamily(
                "id",
                "prefLabelLg1",
                "prefLabelLg2",
                "abstractLg1",
                "abstractLg2",
                "validationState",
                "created",
                "modified",
                Collections.emptyList(),
                Collections.emptyList()
                ));
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
    void setFamilyById(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/operations/family/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPostEndpoints() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideDataForPostEndpoints")
    @ParameterizedTest
    void createFamily(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = post("/operations/family")
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
    void setFamilyValidation(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/operations/family/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

}
