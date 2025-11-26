package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.classifications.ClassificationsService;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.classifications.nomenclatures.webservice.ClassificationsResources;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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


@WebMvcTest(
        controllers = ClassificationsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=classifications",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        ClassificationsResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class TestClassificationsRessourcesEnvProd extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }
    @MockitoBean
    private ClassificationsService classificationsService;
    @MockitoBean
    private ClassificationItemService classificationItemService;

   
    static String familyId="10";
    static String levelId="12";
    static String itemId="14";
    static String conceptVersion="16";
    static String correspondenceId="18";
    static String associationId="20";


    @ParameterizedTest
    @MethodSource("testClassificationFamiliesGetEndpoint")
    void getClassificationsFamilies(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationFamiliesGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications/families", 200, true),
                Arguments.of("/classifications/families", 403, false),

                Arguments.of("/classifications/family/1", 200, true),
                Arguments.of("/classifications/family/1", 403, false),

                Arguments.of("/classifications/family/1/members", 200, true),
                Arguments.of("/classifications/family/1/members", 403, false)
        );
    }


    @ParameterizedTest
    @MethodSource("testClassificationSeriesGetEndpoint")
    void getClassificationsSeries(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationSeriesGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications/series", 200, true),
                Arguments.of("/classifications/series", 403, false),

                Arguments.of("/classifications/series/" + familyId, 200, true),
                Arguments.of("/classifications/series/" + familyId, 403, false),

                Arguments.of("/classifications/series/" + familyId + "/members", 200, true),
                Arguments.of("/classifications/series/" + familyId + "/members", 403, false)
        );
    }

    @ParameterizedTest
    @MethodSource("testClassificationGetEndpoint")
    void getClassifications(String url, Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> testClassificationGetEndpoint(){
        return Arrays.asList(
                Arguments.of("/classifications", 200, true),
                Arguments.of("/classifications", 403, false),

                Arguments.of("/classifications/classification/" + familyId, 200, true),
                Arguments.of("/classifications/classification/" + familyId, 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/items", 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/items", 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId, 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/level/" + levelId, 403, false),

                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members", 200, true),
                Arguments.of("/classifications/classification/"+ familyId + "/level/" + levelId + "/members", 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId, 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId, 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion, 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/notes/" + conceptVersion, 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/levels", 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/levels", 403, false),

                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers", 200, true),
                Arguments.of("/classifications/classification/" + familyId + "/item/" + itemId + "/narrowers", 403, false),

                Arguments.of("/classifications/correspondences", 200, true),
                Arguments.of("/classifications/correspondences", 403, false),

                Arguments.of("/classifications/correspondence/" + familyId, 200, true),
                Arguments.of("/classifications/correspondence/" + familyId, 403, false),

                Arguments.of("/classifications/correspondence/" + familyId + "associations", 200, true),
                Arguments.of("/classifications/correspondence/" + familyId + "associations", 403, false),

                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId, 200, true),
                Arguments.of("/classifications/correspondence/" + correspondenceId + "/association/" + associationId, 403, false)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForPublishClassification")
    void publishClassification(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/classifications/classification/" + familyId + "/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));

    }
    static Collection<Arguments> TestRoleCaseForPublishClassification() {
        return Arrays.asList(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }


    @ParameterizedTest
    @MethodSource("TestRoleCaseForUploadClassification")
    void updateClassificationItemWhenAnyRole(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/classifications/classification/" + familyId + "/item/" + itemId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }



    static Collection<Arguments> TestRoleCaseForUploadClassification(){
        return Arrays.asList(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }
}
