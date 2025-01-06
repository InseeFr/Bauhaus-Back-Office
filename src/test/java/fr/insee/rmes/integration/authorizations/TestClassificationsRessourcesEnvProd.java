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
import fr.insee.rmes.webservice.ClassificationsResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
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
                "fr.insee.rmes.bauhaus.activeModules=classifications"} ////////////////////////////////////////
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
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private ClassificationsService classificationsService;
    @MockBean
    private ClassificationItemService classificationItemService;
    @MockBean
    StampAuthorizationChecker stampAuthorizationChecker;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    String familyId="10";
    String levelId="12";
    String itemId="14";
    String conceptVersion="16";
    String correspondenceId="18";
    String associationId="20";

    @Test
    void getClassificationFamiliesWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/families").header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFamilyWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/family/" + familyId).header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getFamilyMembersWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/family/" + familyId + "/members").header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationSeriesWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/series").header("Authorization", "Bearer toto")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getOnseSeriesWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/series/" + familyId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getSeriesMembersWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/series/" + familyId + "/members").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationsWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationWithAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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

    @Test
    void shouldNotPublishClassificationWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(put("/classifications/classification/" + familyId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPublishClassificationWhenAdmin() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of(Roles.ADMIN));
        mvc.perform(put("/classifications/classification/" + familyId + "/validate").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationItemsWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/items").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationLevelsWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/levels").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getOneClassificationLevelsWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/level/" + levelId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationLevelMembersWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/"+ familyId + "/level/" + levelId + "/members").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationItemWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/item/" + itemId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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

    @Test
    void getClassificationItemNotesWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getClassificationItemNarrowersWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getCorrespondencesWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/correspondences/").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getCorrespondenceWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/correspondence/" + familyId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getCorrespondenceAssociationsWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/correspondence/" + familyId + "associations").header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getCorrespondenceItemWhenAnyRole() throws Exception {
        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get("/classifications/correspondence/" + correspondenceId + "/association/" + associationId).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
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
