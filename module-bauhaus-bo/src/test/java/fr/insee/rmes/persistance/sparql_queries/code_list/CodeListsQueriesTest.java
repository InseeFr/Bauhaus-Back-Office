package fr.insee.rmes.persistance.sparql_queries.code_list;

import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeListsQueriesTest {


    @Mock
    GraphsProperties graphs;

    @Mock
    BauhausUriProperties uris;

    CodeListsQueries codeListsQueries;

    @BeforeEach
    void setUp() {
        codeListsQueries = new CodeListsQueries(uris, new BauhausLanguagesProperties("fr", "en"), graphs, new PaginationProperties(5));
    }

    @Test
    void getCodeListItemsByNotation() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("NOTATION", "NOTATION");
                put("LG1", "fr");
                put("LG2", "en");
                put("OFFSET", "5");
                put("PER_PAGE", "5");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getCodeListItemsByNotation.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getCodeListItemsByNotation("NOTATION", 2, null);
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getCodeListItemsByNotationWithoutPerPageValue() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        codeListsQueries = new CodeListsQueries(uris, new BauhausLanguagesProperties("fr", "en"), graphs, new PaginationProperties(0));
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("NOTATION", "NOTATION");
                put("LG1", "fr");
                put("LG2", "en");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getCodeListItemsByNotation.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getCodeListItemsByNotation("NOTATION", 2, null);
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void countCodesForCodeList() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("NOTATION", "NOTATION");
                put("LG1", "fr");
                put("LG2", "en");
                put("SEARCH_CODE", "code");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("countNumberOfCodes.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.countCodesForCodeList("NOTATION", List.of("code:code"));
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithPagination() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        when(uris.codeListBaseUri()).thenReturn("codelist-base-uri");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("NOTATION", "NOTATION");
                put("PARTIAL", false);
                put("CODE_LIST_BASE_URI", "codelist-base-uri");
                put("OFFSET", 5);
                put("PER_PAGE", 5);
                put("SEARCH_CODE", "search");
                put("SORT", "labelLg1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getDetailedCodes.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.FULL, List.of("code:search"), 2, null, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithoutPagination() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        when(uris.codeListBaseUri()).thenReturn("codelist-base-uri");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("NOTATION", "NOTATION");
                put("PARTIAL", true);
                put("CODE_LIST_BASE_URI", "codelist-base-uri");
                put("SEARCH_CODE", "search");
                put("SORT", "labelLg1");

            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getDetailedCodes.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.PARTIAL, List.of("code:search"), 0, 0, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getBroaderNarrowerCloseMatch() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("NOTATION", "NOTATION");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getBroaderNarrowerCloseMatch.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getBroaderNarrowerCloseMatch("NOTATION");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithoutSearch() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");
        when(uris.codeListBaseUri()).thenReturn("codelist-base-uri");
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("NOTATION", "NOTATION");
                put("PARTIAL", true);
                put("CODE_LIST_BASE_URI", "codelist-base-uri");
                put("SORT", "labelLg1");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getDetailedCodes.ftlh"), eq(map))).thenReturn("request");
            String query = codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.PARTIAL, null, 0, 0, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getCodeUriByNotation_returnsExpectedSparql() throws RmesException {
        when(graphs.codeListGraph()).thenReturn("codelist-graph");

        String query = normalize(codeListsQueries.getCodeUriByNotation("CL_TEST", "CODE_1"));

        Assertions.assertEquals(normalize("""
                SELECT  ?uri
                WHERE { GRAPH <codelist-graph> {
                ?codeList rdf:type skos:ConceptScheme .
                ?codeList skos:notation 'CL_TEST' .
                ?uri skos:inScheme ?codeList .
                ?uri skos:notation 'CODE_1' .
                 }}
                """), query);
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}
