package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(
        controllers = DocumentsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=operations",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        DocumentsResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class DocumentsResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    String id ="10";


    private static Stream<Arguments> provideDataForGetEndpoints() {
        return Stream.of(
                Arguments.of("/documents", 200, true),
                Arguments.of("/documents/document/1", 200, true),
                Arguments.of("/documents/document/1/file", 200, true),
                Arguments.of("/documents/link/1", 200, true),

                Arguments.of("/documents", 403, false),
                Arguments.of("/documents/document/1", 403, false),
                Arguments.of("/documents/document/1/file", 200, false),
                Arguments.of("/documents/link/1", 403, false)

        );
    }


    @MethodSource("provideDataForGetEndpoints")
    @ParameterizedTest
    void getData(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(documentsService.getDocument("1")).thenReturn(new JSONObject());
        when(documentsService.getLink("1")).thenReturn(new JSONObject());

        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForPutEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true, true),
                Arguments.of("/documents/document/1", 403, false),

                Arguments.of("/documents/link/1", 200, true, true),
                Arguments.of("/documents/link/1", 403, false)
        );
    }

    @MethodSource("provideDataForPutEndpoints")
    @ParameterizedTest
    void updateDocumentOrLink(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}");

        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDeleteEndpoints() {
        return Stream.of(
                Arguments.of("/documents/document/1", 200, true),
                Arguments.of("/documents/document/1", 403, false),

                Arguments.of("/documents/link/1", 200, true),
                Arguments.of("/documents/link/1", 403, false)
        );
    }

    @MethodSource("provideDataForDeleteEndpoints")
    @ParameterizedTest
    void deleteDocumentOrLink(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(documentsService.deleteDocument(anyString())).thenReturn(HttpStatus.OK);
        when(documentsService.deleteLink(anyString())).thenReturn(HttpStatus.OK);
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForDocumentPostEndpoints() {
        return Stream.of(
                Arguments.of(201, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideDataForDocumentPostEndpoints")
    @ParameterizedTest
    void postDocument(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
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
        var request = MockMvcRequestBuilders.multipart("/documents/document")
                        .file(file)
                        .param("body","Données Json")
                        .contentType(MULTIPART_FORM_DATA_VALUE);



        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }


    private static Stream<Arguments> provideDataForLinkPostEndpoints() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideDataForLinkPostEndpoints")
    @ParameterizedTest
    void postLink(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        Mockito.when(documentsService.setLink(Mockito.anyString()))
                .thenReturn(id);
        var request = post("/documents/link")
                        .param("body","Données Json")
                        .contentType(MediaType.APPLICATION_JSON);

        request.header("Authorization", "Bearer toto");
        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideDataForLinkUpdateFileEndpoints() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }

    @MethodSource("provideDataForLinkUpdateFileEndpoints")
    @ParameterizedTest
    void updateFile(Integer code,  boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

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

        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
}

