package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.config.auth.security.SecurityExpressionRootForBauhaus;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.webservice.CodeListsResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static fr.insee.rmes.model.ValidationStatus.UNPUBLISHED;
import static org.mockito.ArgumentMatchers.anyString;
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
        UserProviderFromSecurityContext.class})
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
    @MockBean
    SecurityExpressionRootForBauhaus securityExpressionRootForBauhaus;
    private final String idep = "xxxxux";
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
    void putCodesListAsCodesListContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(true);

        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putCodesListAsCodesListContributor_badSerie() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        when(stampAuthorizationChecker.isCodesListManagerWithStamp(String.valueOf(codesListId+1),timbre)).thenReturn(true);

        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
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
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("toto"));
        when(stampAuthorizationChecker.isCodesListManagerWithStamp(String.valueOf(codesListId),timbre)).thenReturn(true);

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

//    dans isCodesListContributor, on prend le FakeUser avec ses attributs par défaut, donc notamment un stamp vide
//    donc la méthode ne va pas : il faut récupérer le stamp du user
    @Test
    void postCodesListAsCodesListContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));
        FakeUserConfiguration fakeUserConfiguration=new FakeUserConfiguration();
        fakeUserConfiguration.setStamp(Optional.of("fakeStampForDvAndQf"));
        when(securityExpressionRootForBauhaus.isCodesListContributor(anyString())).thenReturn(true);

        mvc.perform(post("/codeList/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\",\"contributor\": \"fakeStampForDvAndQf\"}"))
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
    void postCodesListAsCodesListContributor_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("toto"));
        when(securityExpressionRootForBauhaus.isCodesListContributor(anyString())).thenReturn(true);
        mvc.perform(put("/codeList/" + codesListId).header("Authorization", "Bearer toto")
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

//    ne marche pas car codeListId est à null (mode debug)
    @Test
    void deleteUnpublishedCodesListAsCodesListContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.CODESLIST_CONTRIBUTOR));

//        when(stampAuthorizationChecker.isCodesListManagerWithStampWithValidationStatus(String.valueOf(codesListId),status,timbre)).thenReturn(true);
        when(securityExpressionRootForBauhaus.isContributorOfCodesList(String.valueOf(codesListId),status)).thenReturn(true);
        mvc.perform(delete("/codeList/" + codesListId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
