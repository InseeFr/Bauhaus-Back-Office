package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.config.auth.security.JwtProperties;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.operations.DocumentsResources;
import org.json.JSONObject;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stampClaim=" + STAMP_CLAIM,
                "jwt.roleClaim=" + ROLE_CLAIM,
                "jwt.idClaim=" + ID_CLAIM,
                "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "jwt.sourceClaim=source",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=operations"}
)
@Import(JwtProperties.class)
class TestDocumentsResourcesEnvProd extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;


    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    String id ="10";


    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/documents", 200, true, true),
                Arguments.of("/documents/document/1", 200, true, true),
                Arguments.of("/documents/document/1/file", 200, true, true),
                Arguments.of("/documents/link/1", 200, true, true),

                Arguments.of("/documents", 403, true, false),
                Arguments.of("/documents/document/1", 403, true, false),
                Arguments.of("/documents/document/1/file", 200, true, false),
                Arguments.of("/documents/link/1", 403, true, false),

                Arguments.of("/documents", 401, false, true),
                Arguments.of("/documents/document/1", 401, false, true),
                Arguments.of("/documents/document/1/file", 200, false, true),
                Arguments.of("/documents/link/1", 401, false, true)
        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(documentsService.getDocument("1")).thenReturn(new JSONObject());
        when(documentsService.getLink("1")).thenReturn(new JSONObject());

        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true, true),
                Arguments.of("/documents/document/1", 403, true, false),
                Arguments.of("/documents/document/1", 401, false, true),

                Arguments.of("/documents/link/1", 200, true, true),
                Arguments.of("/documents/link/1", 403, true, false),
                Arguments.of("/documents/link/1", 401, false, true)
        );
    }

    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void updateDocumentOrLink(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.UPDATE.toString()), anyString(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDeleteEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true, true),
                Arguments.of("/documents/document/1", 403, true, false),
                Arguments.of("/documents/document/1", 401, false, true),

                Arguments.of("/documents/link/1", 200, true, true),
                Arguments.of("/documents/link/1", 403, true, false),
                Arguments.of("/documents/link/1", 401, false, true)
        );
    }

    @MethodSource("provideDataForDeleteEndpoints")
    @ParameterizedTest
    void deleteDocumentOrLink(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(documentsService.deleteDocument(anyString())).thenReturn(HttpStatus.OK);
        when(documentsService.deleteLink(anyString())).thenReturn(HttpStatus.OK);
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.DELETE.toString()), anyString(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDocumentPostEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }

    @MethodSource("provideDataForDocumentPostEndpoints")
    @ParameterizedTest
    void postDocument(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        //
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        Mockito.when(documentsService.createDocument(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(id);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        var request = MockMvcRequestBuilders.multipart("/documents/document/")
                        .file(file)
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE);


        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForLinkPostEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }

    @MethodSource("provideDataForLinkPostEndpoints")
    @ParameterizedTest
    void postLink(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
        var request = post("/documents/link/")
                        .param("body","Données Json")
                        .contentType(MediaType.APPLICATION_JSON);



        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForLinkUpdateFileEndpoints() {
        return Stream.of(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(200, false, true)
        );
    }

    @MethodSource("provideDataForLinkUpdateFileEndpoints")
    @ParameterizedTest
    void updateFile(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.OPERATION_DOCUMENT.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        String expectedUrl = "http://example.com/documents/12345";
        Mockito.when(documentsService.changeDocument(eq(id), Mockito.any(InputStream.class), Mockito.anyString()))
                .thenReturn(expectedUrl);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.pdf",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        var request = MockMvcRequestBuilders.multipart(HttpMethod.PUT,"/documents/document/" + id + "/file", id)
                        .file(file) // Ajout du fichier
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
}

