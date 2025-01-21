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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.eq;
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
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private DocumentsService documentsService;
    @MockitoBean
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
                .getDocument(id);
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
                .getLink(id);
    }

    @Test
    void getDocumentFileWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/documents/document/"+ id + "/file").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPutDocument")
    void putDocumentTest(String role, ResultMatcher expectedStatus) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        mvc.perform(put("/documents/document/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"10\"}"))
                .andExpect(expectedStatus);
    }
    static Collection<Arguments> TestRoleCaseForPutDocument(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk()),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk()),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk()),
                Arguments.of("BadRole", status().isForbidden())
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPutLink")
    void putLinkTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        JSONObject body = new JSONObject("{\"id\": \"10\"}");
        mvc.perform(put("/documents/link/" + id).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(body)))
                .andExpect(expectedStatus);
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .setLink(id, String.valueOf(body));

    }
    static Collection<Arguments> TestRoleCaseForPutLink(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }



    @ParameterizedTest
    @MethodSource("TestRoleCaseForDeleteDocument")
    void deleteDocumentTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        when(documentsService.deleteDocument(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/document/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .deleteDocument(id);
    }
    static Collection<Arguments> TestRoleCaseForDeleteDocument(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForDeleteLink")
    void deleteLinkTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        when(documentsService.deleteLink(id)).thenReturn(HttpStatus.OK);
        mvc.perform(delete("/documents/link/" + id)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .deleteLink(id);
    }
    static Collection<Arguments> TestRoleCaseForDeleteLink(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }



    @Test
    void postDocument_noAuth() throws Exception {
        mvc.perform(post("/documents/document/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForCreateDocument")
    void postDocumentTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
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
                .andExpect(expectedStatus);
        // Vérification des interactions avec le service
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .createDocument(eq("Données Json"), Mockito.any(InputStream.class),eq("document.txt"));
    }
    static Collection<Arguments> TestRoleCaseForCreateDocument(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }



    @Test
    void postLink_noAuth() throws Exception {
        mvc.perform(post("/documents/link/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("TestRoleCaseForCreateLink")
    void postLinkTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
        mvc.perform(post("/documents/link/")
                        .param("body","Données Json")
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus);
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .setLink("Données Json");
    }
    static Collection<Arguments> TestRoleCaseForCreateLink(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPutFile")
    void putDocumentFileTest(String role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(role));
        String expectedUrl = "http://example.com/documents/12345";
        Mockito.when(documentsService.changeDocument(eq(id), Mockito.any(InputStream.class), Mockito.anyString()))
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
                .andExpect(expectedStatus);
        // Vérifier que le service a été appelé avec les bons arguments
        Mockito.verify(documentsService, Mockito.times(numberOfInvocations))
                .changeDocument(eq(id), Mockito.any(InputStream.class), eq("document.txt"));

    }
    static Collection<Arguments> TestRoleCaseForPutFile(){
        return Arrays.asList(
                Arguments.of(Roles.ADMIN, status().isOk(), 1),
                Arguments.of(Roles.INDICATOR_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of(Roles.SERIES_CONTRIBUTOR, status().isOk(), 1),
                Arguments.of("BadRole", status().isForbidden(), 0)
        );
    }

}

