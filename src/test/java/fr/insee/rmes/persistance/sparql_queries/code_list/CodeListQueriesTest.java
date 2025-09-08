package fr.insee.rmes.persistance.sparql_queries.code_list;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
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
class CodeListQueriesTest {

    @Mock
    Config config;

    @Test
    void getCodeListItemsByNotation() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        when(config.getPerPage()).thenReturn(5);
        CodeListQueries.setConfig(config);
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
            String query = CodeListQueries.getCodeListItemsByNotation("NOTATION", 2, null);
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getCodeListItemsByNotationWithoutPerPageValue() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        when(config.getPerPage()).thenReturn(0);
        CodeListQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("NOTATION", "NOTATION");
                put("LG1", "fr");
                put("LG2", "en");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getCodeListItemsByNotation.ftlh"), eq(map))).thenReturn("request");
            String query = CodeListQueries.getCodeListItemsByNotation("NOTATION", 2, null);
            Assertions.assertEquals("request",query);
        }
    }

    @Test
    void countCodesForCodeList() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        CodeListQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("NOTATION", "NOTATION");
                put("LG1", "fr");
                put("LG2", "en");
                put("SEARCH_CODE", "code");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("countNumberOfCodes.ftlh"), eq(map))).thenReturn("request");
            String query = CodeListQueries.countCodesForCodeList("NOTATION", List.of("code:code"));
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithPagination() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        when(config.getCodeListBaseUri()).thenReturn("codelist-base-uri");
        when(config.getPerPage()).thenReturn(5);
        CodeListQueries.setConfig(config);
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
            String query = CodeListQueries.getDetailedCodes("NOTATION", false, List.of("code:search"), 2, null, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithoutPagination() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        when(config.getCodeListBaseUri()).thenReturn("codelist-base-uri");
        CodeListQueries.setConfig(config);
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
            String query = CodeListQueries.getDetailedCodes("NOTATION", true, List.of("code:search"), 0, 0, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getBroaderNarrowerCloseMatch() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        CodeListQueries.setConfig(config);
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {{
                put("CODES_LISTS_GRAPH", "codelist-graph");
                put("LG1", "fr");
                put("LG2", "en");
                put("NOTATION", "NOTATION");
            }};
            mockedFactory.when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq("getBroaderNarrowerCloseMatch.ftlh"), eq(map))).thenReturn("request");
            String query = CodeListQueries.getBroaderNarrowerCloseMatch("NOTATION");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDetailedCodesWithoutSearch() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(config.getCodeListGraph()).thenReturn("codelist-graph");
        when(config.getCodeListBaseUri()).thenReturn("codelist-base-uri");
        CodeListQueries.setConfig(config);
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
            String query = CodeListQueries.getDetailedCodes("NOTATION", true,null, 0, 0, "labelLg1");
            Assertions.assertEquals("request", query);
        }
    }
}