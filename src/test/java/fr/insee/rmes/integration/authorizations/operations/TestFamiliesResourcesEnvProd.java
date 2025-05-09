package fr.insee.rmes.integration.authorizations.operations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.rbac.PropertiesAccessPrivilegesChecker;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.webservice.operations.FamilyResources;
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

import java.util.List;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FamilyResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=operations"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        PropertiesAccessPrivilegesChecker.class})
class TestFamiliesResourcesEnvProd {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService operationsDocumentationsService;

    @MockitoBean
    private PropertiesAccessPrivilegesChecker propertiesAccessPrivilegesChecker;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;
    private final String idep = "xxxxux";
    private final String timbre = "XX59-YYY";


    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/operations/families", 200, "Administrateur_RMESGNCS", true, true),
                Arguments.of("/operations/families/advanced-search", 200, "Administrateur_RMESGNCS", true, true),
                Arguments.of("/operations/families/1/seriesWithReport", 200, "Administrateur_RMESGNCS", true, true),
                Arguments.of("/operations/family/1", 200, "Administrateur_RMESGNCS", true, true),

                Arguments.of("/operations/families", 403, "FAKE", true, false),
                Arguments.of("/operations/families/advanced-search", 403, "FAKE", true, false),
                Arguments.of("/operations/families/1/seriesWithReport", 403, "FAKE", true, false),
                Arguments.of("/operations/family/1", 403, "FAKE", true, false),

                Arguments.of("/operations/families", 401, "Administrateur_RMESGNCS", false, true),
                Arguments.of("/operations/families/advanced-search", 401, "Administrateur_RMESGNCS", false, true),
                Arguments.of("/operations/families/1/seriesWithReport", 401, "Administrateur_RMESGNCS", false, true),
                Arguments.of("/operations/family/1", 401, "Administrateur_RMESGNCS", false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, String role, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(eq(RBAC.Module.OPERATION_FAMILY.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of(200, "Administrateur_RMESGNCS", true, true),
                Arguments.of(403, "Fake", true, false),
                Arguments.of(401, "Administrateur_RMESGNCS", false, true)
        );
    }

    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void setFamilyById(Integer code, String role, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(eq(RBAC.Module.OPERATION_FAMILY.toString()), eq(RBAC.Privilege.UPDATE.toString()), anyString(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        var request = put("/operations/family/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPostEndpoints() {
        return Stream.of(
                Arguments.of(200, "Administrateur_RMESGNCS", true, true),
                Arguments.of(403, "Fake", true, false),
                Arguments.of(401, "Administrateur_RMESGNCS", false, true)
        );
    }

    @MethodSource("provideDataForPostEndpoints")
    @ParameterizedTest
    void createFamily(Integer code, String role, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(eq(RBAC.Module.OPERATION_FAMILY.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        var request = post("/operations/family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPublishEndpoints() {
        return Stream.of(
                Arguments.of(200, "Administrateur_RMESGNCS", true, true),
                Arguments.of(403, "Fake", true, false),
                Arguments.of(401, "Administrateur_RMESGNCS", false, true)
        );
    }

    @MethodSource("provideDataForPublishEndpoints")
    @ParameterizedTest
    void setFamilyValidation(Integer code, String role, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(eq(RBAC.Module.OPERATION_FAMILY.toString()), eq(RBAC.Privilege.PUBLISH.toString()), anyString(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        var request = put("/operations/family/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

}
