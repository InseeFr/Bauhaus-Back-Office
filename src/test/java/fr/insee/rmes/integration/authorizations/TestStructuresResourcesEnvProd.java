package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
import fr.insee.rmes.bauhaus_services.structures.impl.StructureComponentImpl;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.webservice.structures.StructureResources;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StructureResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=structures"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})
class TestStructuresResourcesEnvProd {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private StructureService structureService;
    @MockitoBean
    StructureComponentImpl structureComponentService;
    @MockitoBean
    protected OperationsDocumentationsService documentationsService;
    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;
    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int structureId=10;
    int componentId=12;

    @Test
    void putStructureAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putStructureAsStructureContributor_StampOK() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre))).thenReturn(true);
        mvc.perform(put("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre));
    }

    @Test
    void putStructureAsStructureContributor_badStructureStamp() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre))).thenReturn(false);
        mvc.perform(put("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre));
    }

    @Test
    void putStructureAsStructureContributor_noAuth() throws Exception {
        mvc.perform(put("/structures/structure/" + structureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putStructureAsStructureContributor_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(put("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postStructureAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/structures/structure/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void postStructureAsStructureContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        mvc.perform(post("/structures/structure").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": [\""+timbre+"\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void postStructure_noAuth() throws Exception {
        mvc.perform(put("/structures/structure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postStructureAsNotStructureContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(post("/structures/structure/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postStructureAsStructureContributorWrongStamp_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        mvc.perform(post("/structures/structure").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": [\"wrong\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteStructureAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(delete("/structures/structure/" + structureId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteStructure_noAuth() throws Exception {
        mvc.perform(delete("/structures/structure/" + structureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUnpublishedStructureAsStructureContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre))).thenReturn(true);
        mvc.perform(delete("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre));
    }

    @Test
    void deletePublishedStructureAsStructureContributor_forbidden() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre))).thenReturn(false);
        mvc.perform(delete("/structures/structure/" + structureId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isStructureManagerWithStamp(String.valueOf(structureId),new Stamp(timbre));
    }

    @Test
    void postComponentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(post("/structures/components/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postComponentAsStructureContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        mvc.perform(post("/structures/components").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": [\""+timbre+"\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void postComponent_noAuth() throws Exception {
        mvc.perform(put("/structures/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postComponentAsNotStructureContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(post("/structures/components/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postComponentAsStructureContributorWrongStamp_ko() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        mvc.perform(post("/structures/components").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": [\"wrong\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void putComponentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/structures/components/" + componentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putComponentAsComponentContributor_StampOK() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre))).thenReturn(true);
        mvc.perform(put("/structures/components/" + componentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre));
    }

    @Test
    void putComponentAsComponentContributor_badComponentStamp() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre))).thenReturn(false);
        mvc.perform(put("/structures/components/" + componentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre));
    }

    @Test
    void putComponentAsComponentContributor_noAuth() throws Exception {
        mvc.perform(put("/structures/components/" + componentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteComponentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(delete("/structures/components/" + componentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComponent_noAuth() throws Exception {
        mvc.perform(delete("/structures/components/" + componentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUnpublishedComponentAsStructureContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre))).thenReturn(true);
        mvc.perform(delete("/structures/components/" + componentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre));
    }

    @Test
    void deletePublishedComponentAsStructureContributor_forbidden() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.STRUCTURES_CONTRIBUTOR));
        when(stampAuthorizationChecker.isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre))).thenReturn(false);
        mvc.perform(delete("/structures/components/" + componentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isComponentManagerWithStamp(String.valueOf(componentId),new Stamp(timbre));
    }



}
