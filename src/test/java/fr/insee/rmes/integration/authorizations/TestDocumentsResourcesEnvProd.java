package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentsResources.class,
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
        BauhausMethodSecurityExpressionHandler.class})

class TestDocumentsResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private DocumentsService documentsService;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    int documentId=10;


    @Test
    void getDocuments_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("no particular role"));

        mvc.perform(get("/documents").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        )
                .andExpect(status().isOk());
    }


    @Test
    void putDocumentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(put("/documents/document/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putDocumentAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        mvc.perform(put("/documents/document/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putDocumentAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        mvc.perform(put("/documents/document/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putDocumentBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(put("/documents/document/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void putLinkAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(put("/documents/link/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        mvc.perform(put("/documents/link/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        mvc.perform(put("/documents/link/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(put("/documents/link/" + documentId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDocumentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        when(documentsService.deleteDocument(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDocumentAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        when(documentsService.deleteDocument(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDocumentAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        when(documentsService.deleteDocument(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDocument_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        when(documentsService.deleteDocument(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteLinkAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        when(documentsService.deleteLink(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/link/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLink_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bad_role"));
        when(documentsService.deleteDocument(String.valueOf(documentId))).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/link/" + documentId)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }



    @Test
    void postDocument_noAuth() throws Exception {
        mvc.perform(post("/document/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }
}
