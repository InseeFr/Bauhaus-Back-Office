package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.concepts.ConceptsResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ConceptsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=concepts"}
)

@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})

class ConceptsResourcesAuthorizationsTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private ConceptsService conceptsService;
    @MockitoBean
    private ConceptsCollectionService conceptsCollectionService;
    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;

    String idep = "xxxxxx";
    String timbre = "XX59-YYY";
    static String familyId= "c1116";
    static String conceptVersion="16";
    static String id ="2025";


    @ParameterizedTest
    @MethodSource("TestGetEndpointsOkWhenAnyRole")
    void getObjectWithAnyRole(String url) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    static Collection<Arguments> TestGetEndpointsOkWhenAnyRole(){
        return Arrays.asList(
                Arguments.of("/concepts/"),
                Arguments.of("/concepts/toValidate"),
                Arguments.of("/concepts/linkedConcepts/"+id),
                Arguments.of("/concepts/concept/"+id+"/notes/"+conceptVersion),
                Arguments.of("/concepts/concept/"+id+"/links"),
                Arguments.of("/concepts/concept/export/"+id),
                Arguments.of("/concepts/collections/toValidate"),
                Arguments.of("/concepts/collections/dashboard"),
                Arguments.of("/concepts/collection/"+id+"/members"),
                Arguments.of("/concepts/collection/export/"+id),
                Arguments.of("/concepts/advanced-search"),
                Arguments.of("/concepts/concept/"+id),
                Arguments.of("/concepts/collection/"+id)
        );
    }

}
