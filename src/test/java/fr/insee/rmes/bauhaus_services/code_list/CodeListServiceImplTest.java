package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeListServiceImplTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Spy
    @InjectMocks
    CodeListServiceImpl codeListService;

    @Test
    void getCodesJson() throws RmesException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation")).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getCodeListItemsByNotation("notation", 1)).thenReturn("query2");

            JSONObject count = new JSONObject();
            count.put("count", 5);
            when(repositoryGestion.getResponseAsObject(eq("query"))).thenReturn(count);

            JSONObject item = new JSONObject();
            item.put("id", "id");
            JSONArray items = new JSONArray();
            items.put(item);
            when(repositoryGestion.getResponseAsArray(eq("query2"))).thenReturn(items);

            assertEquals("{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}", codeListService.getCodesJson("notation", 1));
        }
    }

    @Test
    void getCodesForCodeList() throws RmesException {
        try (MockedStatic<CodeListQueries> mockedFactory = Mockito.mockStatic(CodeListQueries.class)) {
            mockedFactory.when(() -> CodeListQueries.countCodesForCodeList("notation")).thenReturn("query");
            mockedFactory.when(() -> CodeListQueries.getDetailedCodes("notation", false, 1)).thenReturn("query2");

            JSONObject count = new JSONObject();
            count.put("count", 5);
            when(repositoryGestion.getResponseAsObject(eq("query"))).thenReturn(count);

            JSONObject item = new JSONObject();
            item.put("id", "id");
            JSONArray items = new JSONArray();
            items.put(item);
            when(repositoryGestion.getResponseAsArray(eq("query2"))).thenReturn(items);

            assertEquals("{\"total\":5,\"page\":1,\"items\":[{\"id\":\"id\"}]}", codeListService.getCodesForCodeList("notation", 1));
        }
    }

    @Test
    void updateCodeFromCodeList() throws RmesException {
        doReturn(null).when(codeListService).deleteCodeFromCodeList("notation", "code");
        doReturn("code").when(codeListService).addCodeFromCodeList("notation", "body");
        String code = codeListService.updateCodeFromCodeList("notation", "code", "body");
        assertEquals(code, "code");
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

        doReturn(codesList).when(codeListService).getDetailedCodesListJson("notation", false);

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

            doReturn(codesList).when(codeListService).getDetailedCodesListJson("notation", false);
            codeListService.deleteCodeFromCodeList("notation", "code");
            verify(repositoryGestion, times(1)).deleteObject(eq(codeIRI), eq(null));
        }
    }
}