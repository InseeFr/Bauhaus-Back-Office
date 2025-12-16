package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.integration.AbstractResourcesEnvProd;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.infrastructure.OidcUserDecoder;
import fr.insee.rmes.modules.users.infrastructure.UserProviderFromSecurityContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.stream.Stream;

import static fr.insee.rmes.integration.authorizations.TokenForTestsConfiguration.configureJwtDecoderMock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = CollectionsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        properties = {
                "fr.insee.rmes.bauhaus.activeModules=concepts",
                "fr.insee.rmes.bauhaus.extensions=pdf,odt"
        }
)
@Import({
        CollectionsResources.class,
        UserProviderFromSecurityContext.class,
        OidcUserDecoder.class
})
class CollectionsResourcesHasAccessIntegrationTest extends AbstractResourcesEnvProd {

    @Configuration
    @EnableMethodSecurity(securedEnabled = true)
    static class TestSecurityConfiguration {
        // Configuration minimale pour activer method security
    }

    @MockitoBean
    private CollectionsService collectionsService;

    String collectionId = "collection123";

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void getAllCollections(Integer code, boolean hasAccessReturn) throws Exception, CollectionsFetchException, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(collectionsService.getAllCollections()).thenReturn(Collections.emptyList());

        var request = get("/concepts/collections").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void getCollectionById(Integer code, boolean hasAccessReturn) throws Exception, CollectionsFetchException, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(collectionsService.getCollection(any())).thenReturn(java.util.Optional.empty());

        var request = get("/concepts/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code == 200 ? 404 : code));
    }

    @MethodSource("providePostCollectionData")
    @ParameterizedTest
    void createCollection(Integer code, boolean hasAccessReturn) throws Exception, CollectionsSaveException, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        when(collectionsService.createCollection(any())).thenReturn(new CollectionId("1"));
        var request = post("/concepts/collections").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content("{\"creator\": \"creator\", \"contributor\": \"contributor\", \"labels\": [{\"value\": \"value\", \"lang\": \"fr\"}], \"descriptions\": [], \"conceptsIdentifiers\": []}");
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void updateCollection(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException, CollectionsSaveException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());
        doNothing().when(collectionsService).update(any());
        var request = put("/concepts/collections/" + collectionId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                        {"id": "%s", "labels": [{"value":"value", "lang": "fr"}], "descriptions": [], "creator": "HIE", "created" : "2025-12-16T11:19:15.773257992", "conceptsIdentifiers": []}
                        """.formatted(collectionId));
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void deleteCollection(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = delete("/concepts/collections/" + collectionId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void publishCollection(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = put("/concepts/collections/" + collectionId + "/validate").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");

        mvc.perform(request).andExpect(status().is(code));
    }

    @MethodSource("provideCollectionData")
    @ParameterizedTest
    void searchCollections(Integer code, boolean hasAccessReturn) throws Exception, MissingUserInformationException {
        when(checker.hasAccess(any(), any(), any(), any())).thenReturn(hasAccessReturn);
        configureJwtDecoderMock(jwtDecoder, idep, timbre, Collections.emptyList());

        var request = get("/concepts/collections/search").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        request.header("Authorization", "Bearer toto");


        mvc.perform(request).andExpect(status().is(code));
    }

    private static Stream<Arguments> providePostCollectionData() {
        return Stream.of(
                Arguments.of(201, true),
                Arguments.of(403, false)
        );
    }

    private static Stream<Arguments> provideCollectionData() {
        return Stream.of(
                Arguments.of(200, true),
                Arguments.of(403, false)
        );
    }
}
