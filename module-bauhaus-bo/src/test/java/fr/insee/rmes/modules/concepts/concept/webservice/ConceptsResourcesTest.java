package fr.insee.rmes.modules.concepts.concept.webservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptForAdvancedSearch;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.concepts.concept.domain.model.PartialConcept;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ConceptsResourcesTest {

    @MockitoBean
    fr.insee.rmes.bauhaus_services.ConceptsService legacyConceptsService;

    @MockitoBean
    fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService conceptsService;

    private ConceptsResources newController() {
        return new ConceptsResources(legacyConceptsService, conceptsService);
    }

    @Test
    void shouldExposeTheAlternativeLabelWhenListingConcepts() throws Throwable {
        PartialConcept concept = new PartialConcept(
                new ConceptId("c00001"),
                LocalisedLabel.ofDefaultLanguage("Répertoire des personnes physiques"),
                LocalisedLabel.ofDefaultLanguage("RNIPP"));

        when(conceptsService.getAllConcepts()).thenReturn(List.of(concept));

        var response = newController().getConcepts();

        JsonNode body = new ObjectMapper().valueToTree(response.getBody());
        assertThat(body.get(0).get("id").asText()).isEqualTo("c00001");
        assertThat(body.get(0).get("altLabel").asText()).isEqualTo("RNIPP");
    }

    @Test
    void shouldExposeANullAlternativeLabelWhenTheConceptHasNone() throws Throwable {
        PartialConcept concept = new PartialConcept(
                new ConceptId("c00002"),
                LocalisedLabel.ofDefaultLanguage("Concept sans sigle"),
                null);

        when(conceptsService.getAllConcepts()).thenReturn(List.of(concept));

        var response = newController().getConcepts();

        JsonNode body = new ObjectMapper().valueToTree(response.getBody());
        assertThat(body.get(0).get("altLabel").isNull()).isTrue();
    }

    @Test
    void shouldReturnConceptsSearchWithHateoasLinks() throws RmesException {
        ConceptForAdvancedSearch concept1 = new ConceptForAdvancedSearch("search-1", "Search Concept 1", "altLabel1", "owner1", "disseminationStatus1", "validationStatus1", "definition1", "2024-01-01", "2024-01-02", "true", "");
        ConceptForAdvancedSearch concept2 = new ConceptForAdvancedSearch("search-2", "Search Concept 2", "altLabel2", "owner2", "disseminationStatus2", "validationStatus2", "definition2", "2024-02-01", "2024-02-02", "false", "");

        when(legacyConceptsService.getConceptsSearch()).thenReturn(List.of(concept1, concept2));

        var response = newController().getConceptsSearch();

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(2, response.getBody().size());
    }

    @Test
    void shouldReturnResponseWhenGetConceptLinksByID() throws RmesException {
        when(legacyConceptsService.getConceptLinksByID("id mocked")).thenReturn("mocked result");
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",
                newController().getConceptLinksByID("id mocked").toString());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 784, 10, 2025})
    void shouldReturnResponseWhenGetConceptNotesByID(int conceptVersion) throws RmesException {
        when(legacyConceptsService.getConceptNotesByID("id mocked", conceptVersion)).thenReturn("mocked result");
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",
                newController().getConceptNotesByID("id mocked", conceptVersion).toString());
    }

    @Test
    void shouldReturnLocationHeaderWhenCreateConcept() throws RmesException {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/concepts/concept");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        when(legacyConceptsService.setConcept("mocked body")).thenReturn("test-concept-123");

        var response = newController().setConcept("mocked body");

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("test-concept-123", response.getBody());
        Assertions.assertEquals(
                "/concepts/concept/test-concept-123",
                Objects.requireNonNull(response.getHeaders().getLocation()).getPath()
        );
    }

    @Test
    void shouldReturnResponseWhenSetConceptWithIdAndConcept() throws RmesException {
        doNothing().when(legacyConceptsService).setConcept("mocked id", "mocked body");
        Assertions.assertEquals("<204 NO_CONTENT No Content,[]>",
                newController().setConcept("mocked id", "mocked body").toString());
    }
}
