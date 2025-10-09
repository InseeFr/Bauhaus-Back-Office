package fr.insee.rmes.integration.authorizations;

import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsResources;
import fr.insee.rmes.rbac.RBAC;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsResources;
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
import java.util.List;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ConceptsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stamp-claim=" + STAMP_CLAIM,
                "jwt.role-claim=" + ROLE_CLAIM,
                "jwt.id-claim=" + ID_CLAIM,
                "jwt.role-claim.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=concepts"}
)
class ConceptsAuthorizationTest extends AbstractResourcesEnvProd {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    ConceptsService conceptsService;
    @MockitoBean
    ConceptsCollectionService conceptsCollectionService;

    String idep = "xxxxxx";
    String timbre = "XX59-YYY";
    static String conceptVersion="16";
    static String id ="2025";


    @ParameterizedTest
    @MethodSource("TestGetEndpointsOkWhenAnyRole")
    void getObjectWithAnyRole(String url) throws Exception {
        when(checker.hasAccess(anyString(), anyString(), any(), any())).thenReturn(true);

        configureJwtDecoderMock(jwtDecoder, idep, timbre, List.of());
        mvc.perform(get(url).header("Authorization", "Bearer toto")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    static Collection<Arguments> TestGetEndpointsOkWhenAnyRole(){
        return Arrays.asList(
                Arguments.of("/concepts/"),
                Arguments.of("/concepts/toValidate"),
                Arguments.of("/concepts/linkedConcepts/"+id),
                Arguments.of("/concepts/concept/"+id+"/notes/16"+conceptVersion),
                Arguments.of("/concepts/concept/"+id+"/links"),
                Arguments.of("/concepts/concept/export/"+id),
                Arguments.of("/concepts/collections/toValidate"),
                Arguments.of("/concepts/collections/dashboard"),
                Arguments.of("/concepts/collection/"+id+"/members"),
                Arguments.of("/concepts/collection/export/"+id),
                Arguments.of("/concepts/advanced-search"),
                Arguments.of("/concepts/concept/"+id),
                Arguments.of("/concepts/collection/"+id)
        );
    }

    @ParameterizedTest
    @MethodSource("TestRoleCaseForUpdateCollection")
    void updateCollection(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/concepts/collection/1").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }
        mvc.perform(request).andExpect(status().is(code));
    }
    static Collection<Arguments> TestRoleCaseForUpdateCollection() {
        return Arrays.asList(
                Arguments.of(204, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("TestRoleCaseForPublishConcept")
    void publishConcept(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        var request = put("/concepts/c1116/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");
        if(withBearer){
            request.header("Authorization", "Bearer toto");
        }
        mvc.perform(request).andExpect(status().is(code));
    }

    static Collection<Arguments> TestRoleCaseForPublishConcept() {
        return Arrays.asList(
                Arguments.of(401, false, false),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }
}
