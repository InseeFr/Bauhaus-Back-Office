package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.webservice.classifications.ClassificationsResources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class TestClassificationsRessourcesEnvProd extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ClassificationsService classificationsService;
    @MockitoBean
    private ClassificationItemService classificationItemService;

    private final String idep = "x xxxxx";
    private final String timbre = "XX59-YYY";

    static String familyId="10";
    static String levelId="12";
    static String itemId="14";
    static String conceptVersion="16";
    static String correspondenceId="18";
    static String associationId="20";


    @ParameterizedTest
    @MethodSource("testClassificationFamiliesGetEndpoint")
    void getClassificationsFamilies(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CLASSIFICATION_FAMILY.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationFamiliesGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications/families", 200, true, true),
                Arguments.of("/classifications/families", 403, true, false),
                Arguments.of("/classifications/families", 401, false, true),

                Arguments.of("/classifications/family/1", 200, true, true),
                Arguments.of("/classifications/family/1", 403, true, false),
                Arguments.of("/classifications/family/1", 401, false, true),

                Arguments.of("/classifications/family/1/members", 200, true, true),
                Arguments.of("/classifications/family/1/members", 403, true, false),
                Arguments.of("/classifications/family/1/members", 401, false, true)
        );
    }


    @ParameterizedTest
    @MethodSource("testClassificationSeriesGetEndpoint")
    void getClassificationsSeries(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CLASSIFICATION_SERIES.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationSeriesGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications/series", 200, true, true),
                Arguments.of("/classifications/series", 403, true, false),
                Arguments.of("/classifications/series", 401, false, true),

                Arguments.of("/classifications/series/" + familyId, 200, true, true),
                Arguments.of("/classifications/series/" + familyId, 403, true, false),
                Arguments.of("/classifications/series/" + familyId, 401, false, true),

                Arguments.of("/classifications/series/" + familyId + "/members", 200, true, true),
                Arguments.of("/classifications/series/" + familyId + "/members", 403, true, false),
                Arguments.of("/classifications/series/" + familyId + "/members", 401, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("testClassificationGetEndpoint")
    void getClassifications(String url, Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CLASSIFICATION_CLASSIFICATION.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications", 200, true, true),
                Arguments.of("/classifications", 403, true, false),
                Arguments.of("/classifications", 401, false, true),

                Arguments.of("/classifications/classification/" + familyId, 200, true, true),
                Arguments.of("/classifications/classification/" + familyId, 403, true, false),
                Arguments.of("/classifications/classification/" + familyId, 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/items", 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/items", 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/items", 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId, 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId, 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId, 401, false, true),

                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members", 200, true, true),
                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members", 403, true, false),
                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members", 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId, 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId, 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId, 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion, 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion, 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion, 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/levels", 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/levels", 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/levels", 401, false, true),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers", 200, true, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers", 403, true, false),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers", 401, false, true),

                Arguments.of("/classifications/correspondences/", 200, true, true),
                Arguments.of("/classifications/correspondences/", 403, true, false),
                Arguments.of("/classifications/correspondences/", 401, false, true),

                Arguments.of("/classifications/correspondence/" + familyId, 200, true, true),
                Arguments.of("/classifications/correspondence/" + familyId, 403, true, false),
                Arguments.of("/classifications/correspondence/" + familyId, 401, false, true),

                Arguments.of("/classifications/correspondence/" + familyId + "associations", 200, true, true),
                Arguments.of("/classifications/correspondence/" + familyId + "associations", 403, true, false),
                Arguments.of("/classifications/correspondence/" + familyId + "associations", 401, false, true),

                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId, 200, true, true),
                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId, 403, true, false),
                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId, 401, false, true)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPublishClassification")
    void publishClassification(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CLASSIFICATION_CLASSIFICATION.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/classifications/classification/" + familyId + "/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));

    }
    static Collection<Arguments> TestRoleCaseForPublishClassification() {
        return Arrays.asList(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForUploadClassification")
    void updateClassificationItemWhenAnyRole(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CLASSIFICATION_CLASSIFICATION.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/classifications/classification/" + familyId + "/item/" + itemId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");

        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }



    static Collection<Arguments> TestRoleCaseForUploadClassification(){
        return Arrays.asList(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }
}
