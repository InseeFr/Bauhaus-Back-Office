package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.security.BauhausMethodSecurityExpressionHandler;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext;
import fr.insee.rmes.webservice.classifications.ClassificationsResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.jsoup.helper.HttpConnection.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ClassificationsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=classifications"}
)

@Import({Config.class,
        OpenIDConnectSecurityContext.class,
        DefaultSecurityContext.class,
        CommonSecurityConfiguration.class,
        UserProviderFromSecurityContext.class,
        BauhausMethodSecurityExpressionHandler.class})

class TestClassificationsRessourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private ClassificationsService classificationsService;
    @MockitoBean
    private ClassificationItemService classificationItemService;
    @MockitoBean
    StampAuthorizationChecker stampAuthorizationChecker;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    static String familyId="10";
    static String levelId="12";
    static String itemId="14";
    static String conceptVersion="16";
    static String correspondenceId="18";
    static String associationId="20";


    @ParameterizedTest
    @MethodSource("TestGetEndpointsOkWhenAnyRole")
    void getObjectWithAnyRole(String url) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    static Collection<Arguments> TestGetEndpointsOkWhenAnyRole(){
        return Arrays.asList(
                Arguments.of("/classifications/families"),
                Arguments.of("/classifications/family/" + familyId),
                Arguments.of("/classifications/family/" + familyId + "/members"),
                Arguments.of("/classifications/series"),
                Arguments.of("/classifications/series/" + familyId),
                Arguments.of("/classifications/series/" + familyId + "/members"),
                Arguments.of("/classifications"),
                Arguments.of("/classifications/classification/" + familyId),
                Arguments.of("/classifications/classification/" + familyId + "/items"),
                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId),
                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members"),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion),
                Arguments.of("/classifications/classification/" + familyId + "/levels"),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers"),
                Arguments.of("/classifications/correspondences/"),
                Arguments.of("/classifications/correspondence/" + familyId),
                Arguments.of("/classifications/correspondence/" + familyId + "associations"),
                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForUpdateClassification")
    void updateClassification(List role, ResultMatcher expectedStatus) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, role);
        mvc.perform(put("/classifications/classification/" + familyId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(expectedStatus);
    }
    static Collection<Arguments> TestRoleCaseForUpdateClassification() {
        return Arrays.asList(
                Arguments.of(List.of(Roles.ADMIN), status().isOk()),
                Arguments.of(List.of("BadRole"), status().isForbidden()),
                Arguments.of(List.of(), status().isForbidden())
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPublishClassification")
    void publishClassification(List role, ResultMatcher expectedStatus) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, role);
        mvc.perform(put("/classifications/classification/" + familyId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(expectedStatus);
    }
    static Collection<Arguments> TestRoleCaseForPublishClassification() {
        return Arrays.asList(
                Arguments.of(List.of(Roles.ADMIN), status().isOk()),
                Arguments.of(List.of("BadRole"), status().isForbidden()),
                Arguments.of(List.of(), status().isForbidden())
        );
    }


    @Test
    void updateClassificationItemWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/classifications/classification/" + familyId + "/item/" + itemId).header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForUploadClassification")
    void uploadClassificationWhenValidDatabaseAndFileProvided(List role, ResultMatcher expectedStatus, int numberOfInvocations) throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, role);
        // Création d'un fichier multipart
        MockMultipartFile file = new MockMultipartFile(
                "file",                // Nom du paramètre
                "document.txt",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "Contenu du fichier".getBytes() // Contenu
        );
        MockMultipartFile database = new MockMultipartFile(
                "database",                // Nom du paramètre
                "",        // Nom du fichier
                MediaType.TEXT_PLAIN_VALUE, // Type MIME
                "gestion".getBytes() // Contenu
        );
        mvc.perform(MockMvcRequestBuilders.multipart("/classifications/upload/classification")
                        .file(file)
                        .file(database)
                        .param("database","gestion")
                        .header("Authorization", "Bearer toto")
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(expectedStatus);
        Mockito.verify(classificationsService,Mockito.times(numberOfInvocations)).uploadClassification(file, "gestion");
    }
    static Collection<Arguments> TestRoleCaseForUploadClassification(){
        return Arrays.asList(
                Arguments.of(List.of(Roles.ADMIN), status().isOk(),1),
                Arguments.of(List.of("BadRole"), status().isForbidden(),0),
                Arguments.of(List.of(), status().isForbidden(),0)
        );
    }

    @Test
    void shouldReturnBadRequestWhenAdminAndDatabaseIsMissing() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "This is a test file content".getBytes()
        );
        mvc.perform(multipart("/classifications/upload/classification/")
                        .file(file)
                        .header("Authorization", "Bearer toto")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    void shouldReturnBadRequestWhenAdminAndDatabaseIsUnknown() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "This is a test file content".getBytes()
        );
        mvc.perform(multipart("/classifications/upload/classification/")
                        .file(file)
                        .header("Authorization", "Bearer toto")
                        .param("database", "unknown")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());
    }

}
