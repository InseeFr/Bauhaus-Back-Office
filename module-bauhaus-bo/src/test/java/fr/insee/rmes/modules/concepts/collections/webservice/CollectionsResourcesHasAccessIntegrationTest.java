package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.config.auth.security.JwtProperties;
import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.rbac.RBAC;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollectionsResources.class,
        properties = {"fr.insee.rmes.bauhaus.env=PROD",
                "jwt.stampClaim=" + STAMP_CLAIM,
                "jwt.roleClaim=" + ROLE_CLAIM,
                "jwt.idClaim=" + ID_CLAIM,
                "jwt.roleClaimConfig.roles=" + KEY_FOR_ROLES_IN_ROLE_CLAIM,
                "jwt.sourceClaim=source",
                "logging.level.org.springframework.security=DEBUG",
                "logging.level.org.springframework.security.web.access=TRACE",
                "logging.level.fr.insee.rmes.config.auth=TRACE",
                "fr.insee.rmes.bauhaus.activeModules=concepts"}
)
@Import(JwtProperties.class)
class CollectionsResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CollectionsService collectionsService;

    private final String idep = "xxxxxx";
    private final String timbre = "XX59-YYY";

    String collectionId = "collection123";

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void getAllCollections(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, CollectionsFetchException {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(collectionsService.getAllCollections()).thenReturn(Collections.emptyList());

        var request = get("/concepts/collections").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void getCollectionById(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception, CollectionsFetchException {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(collectionsService.getCollection(any())).thenReturn(java.util.Optional.empty());

        var request = get("/concepts/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code == 200 ? 404 : code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void createCollection(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.CREATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = post("/concepts/collections").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void updateCollection(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.UPDATE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/concepts/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"id\": \"1\"}");

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void deleteCollection(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.DELETE.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = delete("/concepts/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void publishCollection(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.PUBLISH.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/concepts/collections/" + collectionId + "/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void searchCollections(Integer code, boolean withBearer, boolean hasAccessReturn) throws Exception {
        when(checker.hasAccess(eq(RBAC.Module.CONCEPT_COLLECTION.toString()), eq(RBAC.Privilege.READ.toString()), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get("/concepts/collections/search").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);

        if (withBearer) {
            request.header("Authorization", "Bearer toto");
        }

        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> provideCollectionData() {
        return Stream.of(
                Arguments.of(200, true, true),
                Arguments.of(403, true, false),
                Arguments.of(401, false, true)
        );
    }
}
