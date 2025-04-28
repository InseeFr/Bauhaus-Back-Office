package fr.insee.rmes.bauhaus_services.code_list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.swagger.model.code_list.Page;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeListServiceImplTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    JSONObject counter;

    @Spy
    @InjectMocks
    CodeListServiceImpl codeListService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void getAllCodesLists() throws RmesException, JsonProcessingException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.getAllCodesLists(false)).thenReturn("query");

            JSONArray response = new JSONArray();
            response.put(new JSONObject()
                    .put("id", "1")
                    .put("uri", "uri")
                    .put("labelLg1", "labelLg1")
                    .put("labelLg2", "labelLg2")
                    .put("range", "range")
            );

            response.put(new JSONObject()
                    .put("id", "1")
                    .put("uri", "uri")
                    .put("labelLg1", "élabelLg1")
                    .put("labelLg2", "labelLg2")
                    .put("range", "range")
            );

            response.put(new JSONObject()
                    .put("id", "1")
                    .put("uri", "uri")
                    .put("labelLg1", "alabelLg1")
                    .put("labelLg2", "labelLg2")
                    .put("range", "range")
            );
            when(repositoryGestion.getResponseAsArray("query")).thenReturn(response);

            var codesLists = codeListService.getAllCodesLists(false);
            assertEquals(3, codesLists.size());
            assertEquals("alabelLg1", codesLists.get(0).labelLg1());
            assertEquals("élabelLg1", codesLists.get(1).labelLg1());
            assertEquals("labelLg1", codesLists.get(2).labelLg1());
        }
    }
    @Test
    void getCodesJson() throws RmesException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation", null)).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getCodeListItemsByNotation("notation", 1, null)).thenReturn("query2");

            JSONObject count = new JSONObject();
            count.put("count", 5);
            when(repositoryGestion.getResponseAsObject("query")).thenReturn(count);

            JSONObject item = new JSONObject();
            item.put("id", "id");
            JSONArray items = new JSONArray();
            items.put(item);
            when(repositoryGestion.getResponseAsArray("query2")).thenReturn(items);

            assertEquals("{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}", codeListService.getCodesJson("notation", 1, null));
        }
    }


    @Test
    void getCodesForCodeList() throws RmesException, JsonProcessingException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation", List.of("search"))).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getDetailedCodes("notation", false, List.of("search"), 1, null, "code")).thenReturn("query2");
            mockedFactory.when(() -> CodeListQueries.getBroaderNarrowerCloseMatch("notation")).thenReturn("query3");

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
    }

    @Test
    void updateCodeFromCodeList() throws RmesException {
        doReturn(null).when(codeListService).deleteCodeFromCodeList("notation", "code");
        doReturn("code").when(codeListService).addCodeFromCodeList("notation", "body");
        String code = codeListService.updateCodeFromCodeList("notation", "code", "body");
        assertEquals("code", code);
    }

    @Test
    void addCodeFromCodeList() throws RmesException {
        IRI owlClassUri = RdfUtils.createIRI("http://concept/lastClassUriSegment");
        IRI codeIri = RdfUtils.createIRI("http://lastCodeUriSegment/code");
        IRI codesListIri = RdfUtils.createIRI("http://codeLists");
        IRI codesListGraph = RdfUtils.createIRI("http://codesListGraph");

        JSONObject codesList = new JSONObject();
        codesList.put("lastClassUriSegment", "lastClassUriSegment");
        codesList.put("lastListUriSegment", "lastListUriSegment");
        codesList.put("lastCodeUriSegment", "lastCodeUriSegment");

        doReturn(codesList).when(codeListService).getDetailedCodesListJson("notation");

        JSONObject code = new JSONObject();
        code.put("code", "code");


        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            mockedFactory.when(() -> RdfUtils.codeListIRI("concept/lastClassUriSegment")).thenReturn(owlClassUri);
            mockedFactory.when(() -> RdfUtils.codeListIRI("lastCodeUriSegment/code")).thenReturn(codeIri);
            mockedFactory.when(() -> RdfUtils.codeListIRI("lastListUriSegment")).thenReturn(codesListIri);
            mockedFactory.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.setLiteralString(any(String.class))).thenCallRealMethod();
            mockedFactory.when(RdfUtils::codesListGraph).thenReturn(codesListGraph);

            String result = codeListService.addCodeFromCodeList("notation", code.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(codeIri), model.capture(), eq(null));

            assertEquals("code", result);
            Assertions.assertEquals("[(http://lastCodeUriSegment/code, http://www.w3.org/2004/02/skos/core#notation, \"code\", http://codesListGraph) [http://codesListGraph]]", model.getValue().toString());
        }
    }

    @Test
    void deleteCodeFromCodeList() throws RmesException {
        IRI codeIRI = RdfUtils.createIRI("http://lastCodeUriSegment/code");

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            mockedFactory.when(() -> RdfUtils.codeListIRI("lastCodeUriSegment/code")).thenReturn(codeIRI);
            JSONObject codesList = new JSONObject();
            codesList.put("lastCodeUriSegment", "lastCodeUriSegment");

            doReturn(codesList).when(codeListService).getDetailedCodesListJson("notation");
            codeListService.deleteCodeFromCodeList("notation", "code");
            verify(repositoryGestion, times(1)).deleteObject(codeIRI, null);
        }
    }

    @Test
    void shouldThrowRmesBadRequestExceptionsWhenValidateCodeList() throws RmesException {

        JSONObject jsonObjectWithoutId = new JSONObject().put(Constants.LABEL_LG1,"labelLg1Example").put(Constants.LABEL_LG2,"labelLg2Example").put("lastClassUriSegment","lastClassUriSegmentExample").put("lastListUriSegment","lastListUriSegmentExample").put("code","codeExample");
        JSONObject jsonObjectWithoutLabelLg1 = new JSONObject().put(Constants.ID,"idExample").put(Constants.LABEL_LG2,"labelLg2Example").put("lastClassUriSegment","lastClassUriSegmentExample").put("lastListUriSegment","lastListUriSegmentExample").put("code","codeExample");
        JSONObject jsonObjectWithoutLabelLg2=new JSONObject().put(Constants.ID,"idExample").put(Constants.LABEL_LG1,"labelLg1Example").put("lastClassUriSegment","lastClassUriSegmentExample").put("lastListUriSegment","lastListUriSegmentExample").put("code","codeExample");
        JSONObject jsonObjectWithoutLastClassUriSegment= new JSONObject().put(Constants.ID,"idExample").put(Constants.LABEL_LG1,"labelLg1Example").put(Constants.LABEL_LG2,"labelLg2Example").put("lastListUriSegment","lastListUriSegmentExample").put("code","codeExample");
        JSONObject jsonObjectWithoutLastListUriSegment = new JSONObject().put(Constants.ID,"idExample").put(Constants.LABEL_LG1,"labelLg1Example").put(Constants.LABEL_LG2,"labelLg2Example").put("lastClassUriSegment","lastClassUriSegmentExample").put("code","codeExample");
        JSONObject jsonObjectWithoutCodeKey = new JSONObject().put(Constants.ID,"idExample").put(Constants.LABEL_LG1,"labelLg1Example").put(Constants.LABEL_LG2,"labelLg2Example").put("lastClassUriSegment","lastClassUriSegmentExample").put("lastListUriSegment","lastListUriSegmentExample");

        RmesException exceptionId = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutId,false));
        RmesException exceptionLabelLg1 = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutLabelLg1,false));
        RmesException exceptionLabelLg2 = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutLabelLg2,false));
        RmesException exceptionLastClassUriSegment = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutLastClassUriSegment,false));
        RmesException exceptionLastListUriSegment = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutLastListUriSegment,false));
        RmesException exceptionCodeKey = assertThrows(RmesBadRequestException.class, () -> codeListService.validateCodeList(jsonObjectWithoutCodeKey,true));

        boolean cantValidateId = "{\"message\":\"The id of the list should be defined\"}".equals(exceptionId.getDetails());
        boolean cantValidateLabelLg1 = "{\"message\":\"The labelLg1 of the list should be defined\"}".equals(exceptionLabelLg1.getDetails());
        boolean cantValidateLabelLg2 = "{\"message\":\"The labelLg2 of the list should be defined\"}".equals(exceptionLabelLg2.getDetails());
        boolean cantValidateLastClassUriSegment = "{\"message\":\"The lastClassUriSegment of the list should be defined\"}".equals(exceptionLastClassUriSegment.getDetails());
        boolean cantValidateLastListUriSegment = "{\"message\":\"The lastListUriSegment of the list should be defined\"}".equals(exceptionLastListUriSegment.getDetails());
        boolean cantValidateCodeKey= "{\"code\":1102,\"message\":\"A code list should contain at least one code\"}".equals(exceptionCodeKey.getDetails());

        List<Boolean> actual = List.of(cantValidateId,cantValidateLabelLg1,cantValidateLabelLg2,cantValidateLastClassUriSegment,cantValidateLastListUriSegment,cantValidateCodeKey);
        List<Boolean> expected = List.of(true,true,true,true,true,true);

        assertEquals(expected,actual);
    }
}