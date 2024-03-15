package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.*;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static fr.insee.rmes.model.ValidationStatus.UNPUBLISHED;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CodeListsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=codelists"}
)
@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})
public class TestCodeListsResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private CodeListService codeListService;
    @MockBean
    protected OperationsDocumentationsService documentationsService;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int codesListId=10;
    ValidationStatus status= UNPUBLISHED;

    @Test
    void putCodesListAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));

        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putCodesListAsCodesListContributor_stampOK() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(true);

        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isCodesListManagerWithStamp(String.valueOf(codesListId),timbre);
    }

    @Test
    void putCodesListAsCodesListContributor_badCodesListStamp() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(false);
        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isCodesListManagerWithStamp(String.valueOf(codesListId),timbre);
    }

    @Test
    void putCodesListAsCodesListContributor_noAuth() throws Exception {
        mvc.perform(put("/codeList/" + codesListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putCodesListAsCodesListContributor_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postCodesListAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(post("/codeList/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void postCodesListAsCodesListContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        mvc.perform(post("/codeList/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": \""+timbre+"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void postCodesList_noAuth() throws Exception {
        mvc.perform(put("/codeList/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postCodesListAsNotCodesListContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("mauvais rôle"));
        mvc.perform(post("/codeList/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCodesListAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(delete("/codeList/" + codesListId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCodesList_noAuth() throws Exception {
        mvc.perform(delete("/codeList/" + codesListId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUnpublishedCodesListAsCodesListContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isUnpublishedCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(true);
        mvc.perform(delete("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(stampAuthorizationChecker).isUnpublishedCodesListManagerWithStamp(String.valueOf(codesListId),timbre);
    }

    @Test
    void deletePublishedCodesListAsCodesListContributor_forbidden() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isUnpublishedCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(false);
        mvc.perform(delete("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(stampAuthorizationChecker).isUnpublishedCodesListManagerWithStamp(String.valueOf(codesListId),timbre);
    }
}
