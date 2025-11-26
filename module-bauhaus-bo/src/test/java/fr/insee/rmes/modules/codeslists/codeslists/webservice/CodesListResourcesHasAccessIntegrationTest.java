package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
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

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = CodesListsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=codelists",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        CodesListsResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class CodesListResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private CodeListService codeListService;

    int codesListId=10;

    @MethodSource("provideCodeListData")
    @ParameterizedTest
    void updateCodeList(Integer code, boolean hasAccessReturn) throws MissingUserInformationException, Exception {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/codeList/" + codesListId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("providePostCodeListData")
    @ParameterizedTest
    void postCodeList(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/codeList").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> providePostCodeListData() {
        return Stream.of(
                Arguments.of(201, true),
                Arguments.of(403, false)
        );
    }

    private static Stream<Arguments> provideCodeListData() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideCodeListData")
    @ParameterizedTest
    void deleteCodeList(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = delete("/codeList/" + codesListId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideCodeData() {
        return Stream.of(
                Arguments.of(201, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideCodeData")
    @ParameterizedTest
    void postCode(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/codeList/detailed/1/codes").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }


    @MethodSource("provideCodeListData")
    @ParameterizedTest
    void putCode(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/codeList/detailed/1/codes/2").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCodeListData")
    @ParameterizedTest
    void putCodesList(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/codeList/1/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

}
