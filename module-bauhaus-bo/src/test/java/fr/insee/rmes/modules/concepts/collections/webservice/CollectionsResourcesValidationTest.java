package fr.insee.rmes.modules.concepts.collections.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contrat du corps de requête d'une collection de concepts : un corps sans identifiant, sans
 * libellé ou sans propriétaire, ou dont un libellé n'a pas de langue, est refusé en 400 nommant le
 * champ, avant que le service n'écrive quoi que ce soit. Avant, le domaine refusait les premiers
 * par un 400 sans champ, et une liste absente ou une langue manquante échouait en NPE.
 */
@WebMvcTest(
        value = CollectionsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.concepts.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class CollectionsResourcesValidationTest {

    private static final String COMPLETE_BODY = """
            {
              "id": "c1000",
              "labels": [{"value": "Collection", "lang": "fr"}, {"value": "Collection EN", "lang": "en"}],
              "descriptions": [{"value": "Description", "lang": "fr"}, {"value": "", "lang": "en"}],
              "creator": "DG75-L201",
              "contributor": "DG75-L201",
              "conceptsIdentifiers": ["c1", "c2"]
            }""";

    @MockitoBean
    private CollectionsService service;

    @Autowired
    MockMvc mockMvc;

    @Test
    void post_with_an_empty_body_is_rejected_naming_the_fields() throws Throwable {
        mockMvc.perform(post("/concepts/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder(
                                "id", "labels", "descriptions", "creator", "conceptsIdentifiers")));

        verify(service, never()).createCollection(any());
    }

    @Test
    void put_with_an_empty_body_is_rejected_naming_the_fields() throws Throwable {
        mockMvc.perform(put("/concepts/collections/{id}", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder(
                                "id", "labels", "descriptions", "creator", "conceptsIdentifiers")));

        verify(service, never()).update(any());
    }

    @Test
    void post_with_a_label_without_language_is_rejected() throws Throwable {
        mockMvc.perform(post("/concepts/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "c1000", "labels": [{"value": "Collection"}], "descriptions": [],
                                 "creator": "DG75-L201", "conceptsIdentifiers": []}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("labels[0].lang"));

        verify(service, never()).createCollection(any());
    }

    @Test
    void post_with_a_blank_concept_identifier_is_rejected() throws Throwable {
        mockMvc.perform(post("/concepts/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "c1000", "labels": [{"value": "Collection", "lang": "fr"}],
                                 "descriptions": [], "creator": "DG75-L201", "conceptsIdentifiers": [" "]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("conceptsIdentifiers[0]"));

        verify(service, never()).createCollection(any());
    }

    @Test
    void post_with_a_complete_body_forwards_every_field_to_the_service() throws Throwable {
        when(service.createCollection(any())).thenReturn(new CollectionId("c1000"));

        mockMvc.perform(post("/concepts/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateCollectionCommand> forwarded = ArgumentCaptor.forClass(CreateCollectionCommand.class);
        verify(service).createCollection(forwarded.capture());
        CreateCollectionCommand command = forwarded.getValue();
        assertThat(command.id()).isEqualTo("c1000");
        assertThat(command.labels())
                .containsExactly(
                        new LocalisedLabel("Collection", Lang.FR), new LocalisedLabel("Collection EN", Lang.EN));
        assertThat(command.descriptions())
                .containsExactly(new LocalisedLabel("Description", Lang.FR), new LocalisedLabel("", Lang.EN));
        assertThat(command.creator()).isEqualTo("DG75-L201");
        assertThat(command.contributor()).contains("DG75-L201");
        assertThat(command.conceptsIdendifiers()).containsExactly("c1", "c2");
    }

    @Test
    void put_without_concepts_updates_the_collection() throws Throwable {
        mockMvc.perform(put("/concepts/collections/{id}", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "c1000", "labels": [{"value": "Collection", "lang": "fr"}],
                                 "descriptions": [], "creator": "DG75-L201", "conceptsIdentifiers": []}"""))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateCollectionCommand> forwarded = ArgumentCaptor.forClass(UpdateCollectionCommand.class);
        verify(service).update(forwarded.capture());
        assertThat(forwarded.getValue().conceptsIdendifiers()).isEmpty();
    }

    @Test
    void publish_with_an_empty_list_is_rejected() throws Throwable {
        mockMvc.perform(put("/concepts/collections/{id}/validate", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.errors[0].field").value("body"));

        verify(service, never()).publishCollections(anyList());
    }
}
