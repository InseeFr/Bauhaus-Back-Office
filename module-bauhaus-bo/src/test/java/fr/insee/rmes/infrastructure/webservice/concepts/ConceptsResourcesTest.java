package fr.insee.rmes.infrastructure.webservice.concepts;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.ConceptsCollectionService;
import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.onion.infrastructure.webservice.concepts.ConceptsResources;
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

import java.util.Objects;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ConceptsResourcesTest {
    @MockitoBean
    ConceptsService conceptsService;

    @MockitoBean
    ConceptsCollectionService conceptsCollectionService;

    @Test
    void shouldReturnResponseWhenGetConcepts() throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.getRelatedConcepts("id mocked")).thenReturn("mocked result");
        String actual = conceptsResources.getRelatedConcepts("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenDeleteConcept() throws RmesException {
        doNothing().when(conceptsService).deleteConcept("id mocked");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        String actual = conceptsResources.deleteConcept("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,id mocked,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetConceptByID() throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when( conceptsService.getConceptByID("id mocked")).thenReturn("mocked result");
        String actual = conceptsResources.getConceptByID("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetConceptsToValidate() throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.getConceptsToValidate()).thenReturn("mocked result");
        String actual = conceptsResources.getConceptsToValidate().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetConceptLinksByID() throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.getConceptLinksByID("id mocked")).thenReturn("mocked result");
        String actual = conceptsResources.getConceptLinksByID("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 784 ,10,2025})
    void shouldReturnResponseWhenGetConceptNotesByID(int conceptVersion) throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.getConceptNotesByID("id mocked", conceptVersion)).thenReturn("mocked result");
        String actual = conceptsResources.getConceptNotesByID("id mocked", conceptVersion).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCollectionsDashboard()  throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsCollectionService.getCollectionsDashboard()).thenReturn("mocked result");
        String actual = conceptsResources.getCollectionsDashboard().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCollectionsToValidate()  throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.getCollectionsToValidate()).thenReturn("mocked result");
        String actual = conceptsResources.getCollectionsToValidate().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCollectionByID()  throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsCollectionService.getCollectionByID("id mocked")).thenReturn("mocked result");
        String actual = conceptsResources.getCollectionByID("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCollectionMembersByID()  throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsCollectionService.getCollectionMembersByID("id mocked")).thenReturn("mocked result");
        String actual = conceptsResources.getCollectionMembersByID("id mocked").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenSetConcept()  throws RmesException {
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        when(conceptsService.setConcept("mocked body")).thenReturn("mocked result");
        String actual = conceptsResources.setConcept("mocked body").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenSetConceptWithIdAndConcept()  throws RmesException {
        doNothing().when(conceptsService).setConcept("mocked id", "mocked body");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        String actual = conceptsResources.setConcept("mocked id", "mocked body").toString();
        Assertions.assertEquals("<204 NO_CONTENT No Content,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenSetConceptsValidation()  throws RmesException {
        doNothing().when(conceptsService).setConceptsValidation( "mocked body");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        String actual = conceptsResources.setConceptsValidation("mocked id", "mocked body").toString();
        Assertions.assertEquals("<204 NO_CONTENT No Content,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenSetCollection()  throws RmesException {
        var req = new MockHttpServletRequest("POST", "/concepts/collection");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));


        var collection = new Collection("1");
        when(conceptsService.createCollection(collection)).thenReturn("1");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService, conceptsCollectionService);
        var response = conceptsResources.setCollection(collection);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("1", response.getBody());
        Assertions.assertEquals(
                "/concepts/collection/1",
                Objects.requireNonNull(response.getHeaders().getLocation()).getPath()
        );
    }

    @Test
    void shouldReturnResponseWhenSetCollectionWithIdAndBody()  throws RmesException {
        var collection = new Collection("1");
        doNothing().when(conceptsService).updateCollection( "1", collection);
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService, conceptsCollectionService);
        var response = conceptsResources.setCollection( "1", collection);
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldReturnErrorWhenSetCollectionWithDifferentId()  throws RmesException {
        var collection = new Collection("2");
        doNothing().when(conceptsService).updateCollection( "1", collection);
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService, conceptsCollectionService);
        var response = conceptsResources.setCollection( "1", collection);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnResponseWhenGetCollection()  throws RmesException {
        when(conceptsCollectionService.getCollectionByID("1")).thenReturn("{\"id\": \"1\"}");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService, conceptsCollectionService);
        var response = conceptsResources.getCollectionByID( "1");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("{\"id\": \"1\"}", response.getBody());
    }

    @Test
    void shouldReturnResponseWhenSetCollectionsValidation()  throws RmesException {
        doNothing().when(conceptsService).setCollectionsValidation("mocked body");
        ConceptsResources conceptsResources = new ConceptsResources(conceptsService,conceptsCollectionService);
        String actual = conceptsResources.setCollectionsValidation("mocked id","mocked body").toString();
        Assertions.assertEquals("<204 NO_CONTENT No Content,[]>",actual);
    }
}