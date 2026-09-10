package fr.insee.rmes.bauhaus_services.code_list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.CodesListErrorCodes;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.codeslists.codeslists.webservice.CodeRequest;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.Page;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeListServiceImplTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    CodeListsQueries codeListsQueries;

    @Mock
    JSONObject counter;

    @Spy
    @InjectMocks
    CodeListServiceImpl codeListService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final IRI OWL_CLASS_IRI = RdfUtils.createIRI("http://concept/lastClassUriSegment");
    private static final IRI CODE_IRI = RdfUtils.createIRI("http://lastCodeUriSegment/code");
    private static final IRI CODES_LIST_IRI = RdfUtils.createIRI("http://codeLists");
    private static final IRI CODES_LIST_GRAPH = RdfUtils.createIRI("http://codesListGraph");

    /**
     * Service réel, doté des deux langues, dont seule la lecture de la liste de codes est simulée :
     * les tests d'écriture d'un code veulent le vrai modèle RDF, pas un stub.
     */
    private CodeListServiceImpl serviceWritingInto(String notation) throws RmesException {
        JSONObject codesList = new JSONObject()
                .put("lastClassUriSegment", "lastClassUriSegment")
                .put("lastListUriSegment", "lastListUriSegment")
                .put("lastCodeUriSegment", "lastCodeUriSegment");
        CodeListServiceImpl service = spy(new CodeListServiceImpl(
                repositoryGestion,
                null,
                null,
                new BauhausLanguagesProperties("fr", "en"),
                null,
                null,
                codeListsQueries));
        doReturn(codesList).when(service).getDetailedCodesListJson(notation);
        return service;
    }

    /** IRI et littéraux passent par des méthodes statiques : sans ces stubs, le modèle sort vide. */
    private static void stubRdfUtilsForCode(MockedStatic<RdfUtils> rdfUtils) {
        rdfUtils.when(() -> RdfUtils.codeListIRI("concept/lastClassUriSegment")).thenReturn(OWL_CLASS_IRI);
        rdfUtils.when(() -> RdfUtils.codeListIRI("lastCodeUriSegment/code")).thenReturn(CODE_IRI);
        rdfUtils.when(() -> RdfUtils.codeListIRI("lastListUriSegment")).thenReturn(CODES_LIST_IRI);
        rdfUtils.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any()))
                .thenCallRealMethod();
        rdfUtils.when(() -> RdfUtils.setLiteralString(any(String.class))).thenCallRealMethod();
        rdfUtils.when(() -> RdfUtils.setLiteralString(any(String.class), any(String.class)))
                .thenCallRealMethod();
        rdfUtils.when(RdfUtils::codesListGraph).thenReturn(CODES_LIST_GRAPH);
    }

    /** Réponse de la requête d'existence d'un code : vide = le code n'est pas dans la liste. */
    private void stubCodeExistence(String notation, String code, JSONObject... responses) throws RmesException {
        when(codeListsQueries.getCodeByNotation(notation, code)).thenReturn("code-query");
        JSONObject[] next = Arrays.copyOfRange(responses, 1, responses.length);
        when(repositoryGestion.getResponseAsObject("code-query")).thenReturn(responses[0], next);
    }

    private static JSONObject noCode() {
        return new JSONObject();
    }

    private static JSONObject existingCode(String code) {
        return new JSONObject().put("code", code);
    }

    @Test
    void getAllCodesLists() throws RmesException, JsonProcessingException {
        when(codeListsQueries.getAllCodesLists(CodeListKind.FULL)).thenReturn("query");

        JSONArray response = new JSONArray();
        response.put(new JSONObject()
                .put("id", "1")
                .put("uri", "uri")
                .put("labelLg1", "labelLg1")
                .put("labelLg2", "labelLg2")
                .put("range", "range"));

        response.put(new JSONObject()
                .put("id", "1")
                .put("uri", "uri")
                .put("labelLg1", "élabelLg1")
                .put("labelLg2", "labelLg2")
                .put("range", "range"));

        response.put(new JSONObject()
                .put("id", "1")
                .put("uri", "uri")
                .put("labelLg1", "alabelLg1")
                .put("labelLg2", "labelLg2")
                .put("range", "range"));
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(response);

        var codesLists = codeListService.getAllCodesLists(CodeListKind.FULL);
        assertEquals(3, codesLists.size());
        assertEquals("alabelLg1", codesLists.get(0).labelLg1());
        assertEquals("élabelLg1", codesLists.get(1).labelLg1());
        assertEquals("labelLg1", codesLists.get(2).labelLg1());
    }

    @Test
    void getCodesJson() throws RmesException {
        when(codeListsQueries.countCodesForCodeList("notation", null)).thenReturn("query");
        when(codeListsQueries.getCodeListItemsByNotation("notation", 1, null)).thenReturn("query2");

        JSONObject count = new JSONObject();
        count.put("count", 5);
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(count);

        JSONObject item = new JSONObject();
        item.put("id", "id");
        JSONArray items = new JSONArray();
        items.put(item);
        when(repositoryGestion.getResponseAsArray("query2")).thenReturn(items);

        assertEquals(
                "{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}",
                codeListService.getCodesJson("notation", 1, null));
    }

    @Test
    void getCodesForCodeList() throws RmesException, JsonProcessingException {
        when(codeListsQueries.countCodesForCodeList("notation", List.of("search")))
                .thenReturn("query");
        when(codeListsQueries.getDetailedCodes("notation", CodeListKind.FULL, List.of("search"), 1, null, "code"))
                .thenReturn("query2");
        when(codeListsQueries.getBroaderNarrowerCloseMatch("notation")).thenReturn("query3");

        JSONObject count = new JSONObject();
        count.put("count", 5);
        when(repositoryGestion.getResponseAsObject("query")).thenReturn(count);

        JSONObject item = new JSONObject();
        item.put("code", "A");
        JSONArray items = new JSONArray();
        items.put(item);
        when(repositoryGestion.getResponseAsArray("query2")).thenReturn(items);

        JSONObject related = new JSONObject();
        related.put("code", "A");
        related.put("linkCode", "A1");
        related.put("linkType", "broader");
        JSONArray relatedList = new JSONArray();
        relatedList.put(related);
        when(repositoryGestion.getResponseAsArray("query3")).thenReturn(relatedList);
        Page response = codeListService.getCodesForCodeList("notation", List.of("search"), 1, null, "code");
        String responseJson = objectMapper.writeValueAsString(response);
        String expectedJson = "{\"total\":5,\"page\":1,\"items\":[{\"code\":\"A\",\"broader\":[\"A1\"]}]}";
        assertEquals(objectMapper.readTree(expectedJson), objectMapper.readTree(responseJson));
    }

    @Test
    void updateCodeFromCodeList() throws RmesException {
        CodeRequest body = new CodeRequest("code", "labelLg1", "labelLg2", null, null);
        doReturn(null).when(codeListService).deleteCodeFromCodeList("notation", "code");
        doReturn("code").when(codeListService).addCodeFromCodeList("notation", body);
        String code = codeListService.updateCodeFromCodeList("notation", "code", body);
        assertEquals("code", code);
    }

    @Test
    void addCodeFromCodeList() throws RmesException {
        CodeListServiceImpl service = serviceWritingInto("notation");
        stubCodeExistence("notation", "code", noCode());

        CodeRequest code = new CodeRequest("code", "labelLg1", "labelLg2", null, null);

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            stubRdfUtilsForCode(mockedFactory);

            String result = service.addCodeFromCodeList("notation", code);

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(CODE_IRI), model.capture(), eq(null));

            assertEquals("code", result);
            Assertions.assertEquals("""
                    [(http://lastCodeUriSegment/code, http://www.w3.org/2004/02/skos/core#notation, "code") [http://codesListGraph], \
                    (http://lastCodeUriSegment/code, http://www.w3.org/2004/02/skos/core#prefLabel, "labelLg1"@fr) [http://codesListGraph], \
                    (http://lastCodeUriSegment/code, http://www.w3.org/2004/02/skos/core#prefLabel, "labelLg2"@en) [http://codesListGraph]]""", model.getValue().toString());
        }
    }

    @Test
    void addCodeFromCodeList_whenTheCodeAlreadyExists_shouldThrowBadRequestAndKeepTheFirstCode() throws RmesException {
        CodeListServiceImpl service = serviceWritingInto("notation");
        // Le premier ajout ne trouve rien, le second retrouve le code que le premier vient d'écrire.
        stubCodeExistence("notation", "code", noCode(), existingCode("code"));

        CodeRequest first = new CodeRequest("code", "labelLg1", "labelLg2", null, null);
        CodeRequest second = new CodeRequest("code", "autre libellé", "another label", null, null);

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            stubRdfUtilsForCode(mockedFactory);

            assertEquals("code", service.addCodeFromCodeList("notation", first));

            RmesException exception =
                    assertThrows(RmesBadRequestException.class, () -> service.addCodeFromCodeList("notation", second));

            assertEquals(400, exception.getStatus());
            assertThat(exception.getDetails()).contains("Code already exists in this code list");
            assertThat(exception.getDetails())
                    .contains(String.valueOf(CodesListErrorCodes.CODE_LIST_CODE_ALREADY_EXISTS));
            // Le second appel n'a rien écrit : la liste garde le code et les libellés du premier.
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(CODE_IRI), any(), eq(null));
        }
    }

    @Test
    void updateCodeFromCodeList_whenTheCodeExists_shouldStillReplaceIt() throws RmesException {
        CodeListServiceImpl service = serviceWritingInto("notation");
        // L'ordre delete puis add protège la mise à jour : le delete trouve le code, l'add ne le trouve plus.
        stubCodeExistence("notation", "code", existingCode("code"), noCode());

        CodeRequest body = new CodeRequest("code", "nouveau libellé", "new label", null, null);

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            stubRdfUtilsForCode(mockedFactory);

            assertEquals("code", service.updateCodeFromCodeList("notation", "code", body));

            InOrder inOrder = inOrder(repositoryGestion);
            inOrder.verify(repositoryGestion).deleteObject(CODE_IRI, null);
            inOrder.verify(repositoryGestion).loadSimpleObject(eq(CODE_IRI), any(), eq(null));
        }
    }

    private static String codesListBody(String id) {
        return new JSONObject()
                .put(Constants.ID, id)
                .put(Constants.LABEL_LG1, "labelLg1")
                .put(Constants.LABEL_LG2, "labelLg2")
                .put("lastClassUriSegment", "lastClassUriSegment")
                .put("lastListUriSegment", "lastListUriSegment")
                .put("lastCodeUriSegment", "lastCodeUriSegment")
                .toString();
    }

    @Test
    void updateCodeFromCodeList_whenTheBodyCodeDoesNotMatchTheUrlCode_shouldThrowBadRequest() throws RmesException {
        CodeRequest body = new CodeRequest("tutu", "labelLg1", "labelLg2", null, null);

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.updateCodeFromCodeList("notation", "toto", body));

        assertEquals(400, exception.getStatus());
        assertThat(exception.getDetails()).contains("The code of the body should match the code of the url");
        verify(codeListService, never()).deleteCodeFromCodeList(anyString(), anyString());
    }

    @Test
    void setCodesList_whenTheBodyIdDoesNotMatchTheUrlId_shouldThrowBadRequest() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.setCodesList("CL_TEST", codesListBody("CL_OTHER"), CodeListKind.FULL));

        assertEquals(400, exception.getStatus());
        assertThat(exception.getDetails()).contains("The id of the list should match the id of the url");
    }

    /**
     * L'existence est cherchée par IRI et non par notation : l'identifiant d'une liste complète
     * reste modifiable depuis le front, un contrôle par notation renverrait 404 sur un renommage.
     */
    @Test
    void setCodesList_whenTheCodeListDoesNotExist_shouldThrowNotFound() throws RmesException {
        IRI codeListIri = RdfUtils.createIRI("http://codelists/lastListUriSegment");

        try (MockedStatic<RdfUtils> mockedRdfUtils = Mockito.mockStatic(RdfUtils.class, CALLS_REAL_METHODS)) {
            mockedRdfUtils
                    .when(() -> RdfUtils.codeListIRI("lastListUriSegment"))
                    .thenReturn(codeListIri);
            when(codeListsQueries.getCodesListByIri("http://codelists/lastListUriSegment"))
                    .thenReturn("iri-query");
            when(repositoryGestion.getResponseAsObject("iri-query")).thenReturn(new JSONObject());

            RmesException exception = assertThrows(
                    RmesNotFoundException.class,
                    () -> codeListService.setCodesList("CL_TEST", codesListBody("CL_TEST"), CodeListKind.FULL));

            assertEquals(404, exception.getStatus());
            verify(repositoryGestion, never()).loadSimpleObject(any(), any(), any());
            verify(codeListsQueries, never()).getDetailedCodeListByNotation(anyString());
        }
    }

    @Test
    void setCodesList_whenTheIdIsRenamed_shouldKeepTheIriAndThePersistedCreationDate() throws RmesException {
        IRI codeListIri = RdfUtils.createIRI("http://codelists/lastListUriSegment");
        IRI owlClassIri = RdfUtils.createIRI("http://codelists/concept/lastClassUriSegment");
        IRI graph = RdfUtils.createIRI("http://codesListGraph");
        CodeListServiceImpl service = new CodeListServiceImpl(
                repositoryGestion,
                null,
                null,
                new BauhausLanguagesProperties("fr", "en"),
                null,
                null,
                codeListsQueries);

        try (MockedStatic<RdfUtils> mockedRdfUtils = Mockito.mockStatic(RdfUtils.class, CALLS_REAL_METHODS)) {
            mockedRdfUtils
                    .when(() -> RdfUtils.codeListIRI("lastListUriSegment"))
                    .thenReturn(codeListIri);
            mockedRdfUtils
                    .when(() -> RdfUtils.codeListIRI("concept/lastClassUriSegment"))
                    .thenReturn(owlClassIri);
            mockedRdfUtils.when(RdfUtils::codesListGraph).thenReturn(graph);

            when(codeListsQueries.getCodesListByIri("http://codelists/lastListUriSegment"))
                    .thenReturn("iri-query");
            when(repositoryGestion.getResponseAsObject("iri-query"))
                    .thenReturn(new JSONObject().put("created", "2020-01-01T00:00:00"));

            String id = service.setCodesList("CL_RENAMED", codesListBody("CL_RENAMED"), CodeListKind.FULL);

            assertEquals("CL_RENAMED", id);
            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion).loadSimpleObject(eq(codeListIri), model.capture(), eq(null));
            assertThat(model.getValue().toString())
                    .contains("http://purl.org/dc/terms/created, \"2020-01-01T00:00:00\"")
                    .contains("\"CL_RENAMED\"");
        }
    }

    @Test
    void getCodeListJson_whenCodeListDoesNotExist_shouldThrowNotFound() throws RmesException {
        when(codeListsQueries.getCodeListLabelByNotation("unknown")).thenReturn("label-query");
        when(repositoryGestion.getResponseAsObject("label-query")).thenReturn(new JSONObject());

        RmesException exception =
                assertThrows(RmesNotFoundException.class, () -> codeListService.getCodeListJson("unknown"));

        assertEquals(404, exception.getStatus());
        // Le front résout le libellé traduit via `code` : il doit être exposé comme sur les 400.
        assertThat(exception.getDetails())
                .contains("\"code\":" + CodesListErrorCodes.CODE_LIST_UNKNOWN_ID)
                .contains("\"message\":\"CodeList not found\"");
    }

    @Test
    void deleteCodeList_whenCodeListDoesNotExist_shouldThrowNotFound() throws RmesException {
        when(codeListsQueries.getDetailedCodeListByNotation("unknown")).thenReturn("detailed-query");
        when(repositoryGestion.getResponseAsObject("detailed-query")).thenReturn(new JSONObject());

        RmesException exception = assertThrows(
                RmesNotFoundException.class, () -> codeListService.deleteCodeList("unknown", CodeListKind.FULL));

        assertEquals(404, exception.getStatus());
    }

    @Test
    void deleteCodeList_whenPartialCodeListDoesNotExist_shouldThrowNotFound() throws RmesException {
        when(codeListsQueries.getDetailedCodeListByNotation("unknown")).thenReturn("detailed-query");
        when(repositoryGestion.getResponseAsObject("detailed-query")).thenReturn(new JSONObject());

        RmesException exception = assertThrows(
                RmesNotFoundException.class, () -> codeListService.deleteCodeList("unknown", CodeListKind.PARTIAL));

        assertEquals(404, exception.getStatus());
    }

    @Test
    void deleteCodeFromCodeList() throws RmesException {
        IRI codeIRI = RdfUtils.createIRI("http://lastCodeUriSegment/code");

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            mockedFactory
                    .when(() -> RdfUtils.codeListIRI("lastCodeUriSegment/code"))
                    .thenReturn(codeIRI);
            JSONObject codesList = new JSONObject();
            codesList.put("lastCodeUriSegment", "lastCodeUriSegment");

            when(codeListsQueries.getCodeByNotation("notation", "code")).thenReturn("code-query");
            when(repositoryGestion.getResponseAsObject("code-query")).thenReturn(new JSONObject().put("code", "code"));
            doReturn(codesList).when(codeListService).getDetailedCodesListJson("notation");
            codeListService.deleteCodeFromCodeList("notation", "code");
            verify(repositoryGestion, times(1)).deleteObject(codeIRI, null);
        }
    }

    @Test
    void deleteCodeFromCodeList_whenCodeDoesNotExist_shouldThrowNotFound() throws RmesException {
        when(codeListsQueries.getCodeByNotation("notation", "unknown")).thenReturn("code-query");
        when(repositoryGestion.getResponseAsObject("code-query")).thenReturn(new JSONObject());

        RmesException exception = assertThrows(
                RmesNotFoundException.class, () -> codeListService.deleteCodeFromCodeList("notation", "unknown"));

        assertEquals(404, exception.getStatus());
        verify(repositoryGestion, never()).deleteObject(any(IRI.class), any());
    }

    /**
     * Ce qui reste du contrôle de présence historique, c'est-à-dire le seul chemin qui passe encore
     * par lui : les listes partielles.
     * <p>
     * Les cas {@code lastClassUriSegment} et {@code lastListUriSegment} ont disparu d'ici, ils sont
     * couverts par {@code CodesListsResourcesValidationTest} — sur les trois formes de vide, pas
     * seulement sur l'absence de la clé.
     */
    @Test
    void shouldThrowRmesBadRequestExceptionsWhenValidateCodeList() {

        JSONObject jsonObjectWithoutId = new JSONObject()
                .put(Constants.LABEL_LG1, "labelLg1Example")
                .put(Constants.LABEL_LG2, "labelLg2Example")
                .put("code", "codeExample");
        JSONObject jsonObjectWithoutLabelLg1 = new JSONObject()
                .put(Constants.ID, "idExample")
                .put(Constants.LABEL_LG2, "labelLg2Example")
                .put("code", "codeExample");
        JSONObject jsonObjectWithoutLabelLg2 = new JSONObject()
                .put(Constants.ID, "idExample")
                .put(Constants.LABEL_LG1, "labelLg1Example")
                .put("code", "codeExample");
        JSONObject jsonObjectWithoutCodeKey = new JSONObject()
                .put(Constants.ID, "idExample")
                .put(Constants.LABEL_LG1, "labelLg1Example")
                .put(Constants.LABEL_LG2, "labelLg2Example");

        RmesException exceptionId = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.validateCodeList(jsonObjectWithoutId, CodeListKind.PARTIAL));
        RmesException exceptionLabelLg1 = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.validateCodeList(jsonObjectWithoutLabelLg1, CodeListKind.PARTIAL));
        RmesException exceptionLabelLg2 = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.validateCodeList(jsonObjectWithoutLabelLg2, CodeListKind.PARTIAL));
        RmesException exceptionCodeKey = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.validateCodeList(jsonObjectWithoutCodeKey, CodeListKind.PARTIAL));

        boolean cantValidateId =
                "{\"message\":\"The id of the list should be defined\"}".equals(exceptionId.getDetails());
        boolean cantValidateLabelLg1 =
                "{\"message\":\"The labelLg1 of the list should be defined\"}".equals(exceptionLabelLg1.getDetails());
        boolean cantValidateLabelLg2 =
                "{\"message\":\"The labelLg2 of the list should be defined\"}".equals(exceptionLabelLg2.getDetails());
        boolean cantValidateCodeKey = "{\"code\":1102,\"message\":\"A code list should contain at least one code\"}"
                .equals(exceptionCodeKey.getDetails());

        List<Boolean> actual = List.of(cantValidateId, cantValidateLabelLg1, cantValidateLabelLg2, cantValidateCodeKey);
        List<Boolean> expected = List.of(true, true, true, true);

        assertEquals(expected, actual);
    }

    @Test
    void publishCodeList_shouldReturn400_whenTheCodeListIsAlreadyPublished() throws RmesException {
        JSONObject codesList =
                new JSONObject().put("iri", "http://codelist/CL_TEST").put("validationState", "Validated");
        doReturn(codesList).when(codeListService).getDetailedPartialCodesListJson("CL_TEST");

        RmesBadRequestException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.publishCodeList("CL_TEST", CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("This codes list is already published");
        assertThat(exception.getDetails()).contains("Codes list: CL_TEST");
        verify(repositoryGestion, never()).objectValidation(any(), any());
    }
}
