package fr.insee.rmes.integration.authorizations.operations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.rbac.*;
import fr.insee.rmes.webservice.operations.FamilyResources;
import fr.insee.rmes.webservice.operations.SeriesResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.KEY_FOR_ROLES_IN_ROLE_CLAIM;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

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
        HasAccessAspect.class,
        PropertiesAccessPrivilegesChecker.class})
public class TestFamiliesResourcesEnvProd {
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


    @Test
    void getFamilies_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(get("/operations/families").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFamilies_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFamilies_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getFamiliesForSearch_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(get("/operations/families/advanced-search").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFamiliesForSearch_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families/advanced-search").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFamiliesForSearch_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families/advanced-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSeriesWithReport_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(get("/operations/families/1/seriesWithReport").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getSeriesWithReport_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families/1/seriesWithReport").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSeriesWithReport_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/families/1/seriesWithReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getFamilyByID_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(get("/operations/family/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFamilyByID_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.READ)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/family/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFamilyByID_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(get("/operations/family/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setFamilyById_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.UPDATE)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/operations/family/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void setFamilyById_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.UPDATE)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(put("/operations/family/1").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void setFamilyById_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(put("/operations/family/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createFamily_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.CREATE)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/operations/family").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void createFamily_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.CREATE)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(post("/operations/family").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createFamily_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(post("/operations/family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setFamilyValidation_shouldReturn200IfHasAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.VALIDATE)).thenReturn(true);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/operations/family/1/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void setFamilyValidation_shouldReturn403IfHasNotAccess() throws Exception {
        when(propertiesAccessPrivilegesChecker.hasAccess(RBAC.Module.FAMILY, RBAC.Privilege.VALIDATE)).thenReturn(false);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(put("/operations/family/1/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void setFamilyValidation_shouldReturn403IfAnonymous() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("FAKE"));
        mvc.perform(put("/operations/family/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }
}
