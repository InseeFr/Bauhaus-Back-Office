package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.operations.DocumentsResources;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
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

    String id ="10";


    @Test
    void getDocumentsWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/documents").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void getDocumentIdWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        JSONObject jsonObject = new JSONObject("{\"id\": \"10\"}");
        when(documentsService.getDocument(id)).thenReturn(jsonObject);
        mvc.perform(get("/documents/document/"+ id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .getDocument(Mockito.eq(id));
    }

    @Test
    void getLinkdWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        JSONObject jsonObject = new JSONObject("{\"id\": \"10\"}");
        when(documentsService.getLink(id)).thenReturn(jsonObject);
        mvc.perform(get("/documents/link/"+ id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .getLink(Mockito.eq(id));
    }

    @Test
    void getDocumentFileWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        JSONObject jsonObject = new JSONObject("{\"id\": \"10\"}");
        mvc.perform(get("/documents/document/"+ id + "/file").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }


    @Test
    void putDocumentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(put("/documents/document/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }


    @Test
    void putDocumentAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        mvc.perform(put("/documents/document/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putDocumentAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        mvc.perform(put("/documents/document/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putDocumentBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(put("/documents/document/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isForbidden());
    }


    @Test
    void putLinkAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        mvc.perform(put("/documents/link/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        mvc.perform(put("/documents/link/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        mvc.perform(put("/documents/link/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void putLinkBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(put("/documents/link/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDocumentAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        when(documentsService.deleteDocument(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .deleteDocument(Mockito.eq(id));
    }

    @Test
    void deleteDocumentAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        when(documentsService.deleteDocument(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .deleteDocument(Mockito.eq(id));
    }

    @Test
    void deleteDocumentAsIndicatorsContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        when(documentsService.deleteDocument(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .deleteDocument(Mockito.eq(id));
    }

    @Test
    void deleteDocument_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(delete("/documents/document/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(documentsService, Mockito.times(0))
                .deleteDocument(Mockito.eq(id));
    }


    @Test
    void deleteLinkAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        when(documentsService.deleteLink(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/link/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .deleteLink(Mockito.eq(id));
    }

    @Test
    void deleteLink_badRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("bad_role"));
        mvc.perform(delete("/documents/link/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        Mockito.verify(documentsService, Mockito.times(0))
                .deleteLink(Mockito.eq(id));
    }



    @Test
    void postDocument_noAuth() throws Exception {
        mvc.perform(post("/documents/document/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateADocumentIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        Mockito.when(documentsService.createDocument(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(id);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart("/documents/document/")
                        .file(file)
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());
        // Vérification des interactions avec le service
        Mockito.verify(documentsService, Mockito.times(1))
                .createDocument(Mockito.eq("Données Json"), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }

    @Test
    void shouldNotCreateADocumentBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        Mockito.when(documentsService.createDocument(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(id);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart("/documents/document/")
                        .file(file)
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isForbidden());
        Mockito.verify(documentsService, Mockito.times(0))
                .createDocument(Mockito.eq("Données Json"), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }

    @Test
    void shouldCreateADocumentIndicatorContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        Mockito.when(documentsService.createDocument(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(id);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart("/documents/document/")
                        .file(file)
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .createDocument(Mockito.eq("Données Json"), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }

    @Test
    void shouldCreateADocumentSeriesContributorBasedOnStamp() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        Mockito.when(documentsService.createDocument(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(id);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart("/documents/document/")
                        .file(file)
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .createDocument(Mockito.eq("Données Json"), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }

    @Test
    void postLink_noAuth() throws Exception {
        mvc.perform(post("/documents/link/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateALinkIfAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
                mvc.perform(post("/documents/link/")
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .setLink(Mockito.eq("Données Json"));
    }

    @Test
    void shouldNotCreateALinkBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        mvc.perform(post("/documents/link/")
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateALinkAsIndicatorContributor() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
        mvc.perform(post("/documents/link/")
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateALinkAsSeriesContributorBasedOnStamp() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
        mvc.perform(post("/documents/link/")
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(documentsService, Mockito.times(1))
                .setLink(Mockito.eq("Données Json"));
    }

    @Test
    void putDocumentFileAdmin_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Administrateur_RMESGNCS"));
        String expectedUrl = "http://example.com/documents/12345";
        Mockito.when(documentsService.changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(expectedUrl);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/documents/document/" + id + "/file", id)
                        .file(file) // Ajout du fichier
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE)
                        .header("Authorization", "Bearer toto"))
                .andExpect(status().isOk());
        // Vérifier que le service a été appelé avec les bons arguments
        Mockito.verify(documentsService, Mockito.times(1))
                .changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }

    @Test
    void putDocumentFileAsSeriesContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_serie_RMESGNCS"));
        String expectedUrl = "http://example.com/documents/12345";
        Mockito.when(documentsService.changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(expectedUrl);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/documents/document/" + id + "/file", id)
                        .file(file) // Ajout du fichier
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE)
                        .header("Authorization", "Bearer toto"))
                .andExpect(status().isOk());
        // Vérifier que le service a été appelé avec les bons arguments
        Mockito.verify(documentsService, Mockito.times(1))
                .changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }


    @Test
    void putDocumentFileAsIndicatorContributor_ok() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Gestionnaire_indicateur_RMESGNCS"));
        String expectedUrl = "http://example.com/documents/12345";
        Mockito.when(documentsService.changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(expectedUrl);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/documents/document/" + id + "/file", id)
                        .file(file) // Ajout du fichier
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE)
                        .header("Authorization", "Bearer toto"))
                .andExpect(status().isOk());
        // Vérifier que le service a été appelé avec les bons arguments
        Mockito.verify(documentsService, Mockito.times(1))
                .changeDocument(Mockito.eq(id), Mockito.any(InputStream.class), Mockito.eq("document.txt"));
    }


    @Test
    void putDocumentFileBadRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of("Bad_role"));
        String expectedUrl = "http://example.com/documents/12345";
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/documents/document/" + id + "/file", id)
                        .file(file) // Ajout du fichier
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE)
                        .header("Authorization", "Bearer toto"))
                .andExpect(status().isForbidden());
    }

}

