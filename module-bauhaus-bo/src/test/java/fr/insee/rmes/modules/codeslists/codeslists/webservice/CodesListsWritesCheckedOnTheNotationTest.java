package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.config.auth.UserAuthTestConfiguration;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.codeslists.partialcodeslists.webservice.PartialCodeListsResources;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
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
 * La stratégie STAMP compare le timbre de l'utilisateur aux contributeurs de la liste de codes :
 * chaque écriture sur une liste existante, ou sur l'un de ses codes, doit donc transmettre la
 * notation de la liste au contrôle d'accès. Seule cette notation est autorisée.
 */
@WebMvcTest(
        controllers = {CodesListsResources.class, PartialCodeListsResources.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {"fr.insee.rmes.bauhaus.modules.codelists.enabled=true", "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        })
@Import({CodesListsResources.class, PartialCodeListsResources.class, UserAuthTestConfiguration.class})
class CodesListsWritesCheckedOnTheNotationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private CodeListService codeListService;

    @MockitoBean
    private CodesListsService codesListsService;

    private static final String NOTATION = "CL_SEXE";

    private static final String CODES_LIST_BODY = """
            {
              "id": "%s",
              "labelLg1": "libellé",
              "labelLg2": "label",
              "creator": "http://bauhaus/HIE000000",
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Public",
              "lastListUriSegment": "cl-test",
              "lastClassUriSegment": "ClTest",
              "lastCodeUriSegment": "cl-test-code"
            }""".formatted(NOTATION);

    private static final String PARTIAL_CODES_LIST_BODY = """
            {
              "id": "%s",
              "labelLg1": "libellé",
              "labelLg2": "label",
              "creator": "http://bauhaus/HIE000000",
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Public",
              "iriParent": "http://bauhaus/codes/liste"
            }""".formatted(NOTATION);

    private static final String CODE_BODY = """
            {"code":"2","labelLg1":"libellé","labelLg2":"label"}""";

    static Stream<Arguments> codesListWrites() {
        return Stream.of(
                Arguments.of(
                        "CODESLIST_CODESLIST",
                        "UPDATE",
                        put("/codeList/" + NOTATION).content(CODES_LIST_BODY)),
                Arguments.of("CODESLIST_CODESLIST", "DELETE", delete("/codeList/" + NOTATION)),
                Arguments.of("CODESLIST_CODESLIST", "PUBLISH", put("/codeList/" + NOTATION + "/validate")),
                Arguments.of(
                        "CODESLIST_CODESLIST",
                        "CREATE",
                        post("/codeList/detailed/" + NOTATION + "/codes").content(CODE_BODY)),
                Arguments.of(
                        "CODESLIST_CODESLIST",
                        "UPDATE",
                        put("/codeList/detailed/" + NOTATION + "/codes/2").content(CODE_BODY)),
                Arguments.of("CODESLIST_CODESLIST", "DELETE", delete("/codeList/detailed/" + NOTATION + "/codes/2")),
                Arguments.of(
                        "CODESLIST_PARTIALCODESLIST",
                        "UPDATE",
                        put("/codeList/partial/" + NOTATION).content(PARTIAL_CODES_LIST_BODY)),
                Arguments.of("CODESLIST_PARTIALCODESLIST", "DELETE", delete("/codeList/partial/" + NOTATION)),
                Arguments.of(
                        "CODESLIST_PARTIALCODESLIST", "PUBLISH", put("/codeList/partial/" + NOTATION + "/validate")));
    }

    @ParameterizedTest
    @MethodSource("codesListWrites")
    void checks_the_access_on_the_codes_list_notation(
            String module, String privilege, MockHttpServletRequestBuilder request)
            throws Exception, MissingUserInformationException {
        when(checker.hasAccess(eq(module), eq(privilege), eq(NOTATION), any())).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        request.contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is2xxSuccessful());
    }
}
