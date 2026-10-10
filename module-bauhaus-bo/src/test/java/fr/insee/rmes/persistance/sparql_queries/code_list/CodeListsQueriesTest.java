package fr.insee.rmes.persistance.sparql_queries.code_list;

import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.persistance.sparql_queries.FreeMarkerRequestStub.QueryCall;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeListsQueriesTest {

    private static final String CODES_LISTS_GRAPH = "http://rdf.insee.fr/graphes/codes/nomenclatures";
    private static final String GET_DETAILED_CODES_TEMPLATE = "getDetailedCodes.ftlh";

    @Mock
    GraphsProperties graphs;

    @Mock
    BauhausUriProperties uris;

    CodeListsQueries codeListsQueries;

    @BeforeEach
    void setUp() {
        codeListsQueries = new CodeListsQueries(
                uris, new BauhausLanguagesProperties("fr", "en"), graphs, new PaginationProperties(5));
    }

    /**
     * La requête d'existence d'un code doit contraindre le code, pas seulement la liste.
     * <p>
     * Elle pointait sur {@code getCodeListLabelByNotation.ftlh}, qui n'interpole que la notation de
     * la liste : la garde d'unicité de {@code addCodeFromCodeList} répondait donc « Code already
     * exists » pour n'importe quel code d'une liste existante, et le 404 de la suppression ne se
     * déclenchait jamais.
     */
    @Test
    void getCodeByNotation_should_constrain_the_code() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);

        String query = codeListsQueries.getCodeByNotation("CL_TEST", "A");

        org.assertj.core.api.Assertions.assertThat(normalize(query))
                .contains("\"CL_TEST\"")
                .contains("\"A\"");
    }

    @Test
    void getCodeListItemsByNotation() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);
        Map<String, Object> map = codeListParams();
        map.put("OFFSET", "5");
        map.put("PER_PAGE", "5");

        assertRequestBuiltWith(
                "getCodeListItemsByNotation.ftlh",
                map,
                () -> codeListsQueries.getCodeListItemsByNotation("NOTATION", 2, null));
    }

    @Test
    void getCodeListItemsByNotationWithoutPerPageValue() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);
        codeListsQueries = new CodeListsQueries(
                uris, new BauhausLanguagesProperties("fr", "en"), graphs, new PaginationProperties(0));

        assertRequestBuiltWith(
                "getCodeListItemsByNotation.ftlh",
                codeListParams(),
                () -> codeListsQueries.getCodeListItemsByNotation("NOTATION", 2, null));
    }

    @Test
    void countCodesForCodeList() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);
        Map<String, Object> map = codeListParams();
        map.put("SEARCH_CODE", "\"code\"");

        assertRequestBuiltWith(
                "countNumberOfCodes.ftlh",
                map,
                () -> codeListsQueries.countCodesForCodeList("NOTATION", List.of("code:code")));
    }

    @Test
    void getDetailedCodesWithPagination() throws RmesException {
        stubCodeListGraphAndBaseUri();
        Map<String, Object> map = detailedCodesParams(false, "?labelLg1");
        map.put("OFFSET", 5);
        map.put("PER_PAGE", 5);
        map.put("SEARCH_CODE", "\"search\"");

        assertRequestBuiltWith(
                GET_DETAILED_CODES_TEMPLATE,
                map,
                () -> codeListsQueries.getDetailedCodes(
                        "NOTATION", CodeListKind.FULL, List.of("code:search"), 2, null, "labelLg1"));
    }

    @Test
    void getDetailedCodesWithoutPagination() throws RmesException {
        stubCodeListGraphAndBaseUri();
        Map<String, Object> map = detailedCodesParams(true, "?labelLg1");
        map.put("SEARCH_CODE", "\"search\"");

        assertRequestBuiltWith(
                GET_DETAILED_CODES_TEMPLATE,
                map,
                () -> codeListsQueries.getDetailedCodes(
                        "NOTATION", CodeListKind.PARTIAL, List.of("code:search"), 0, 0, "labelLg1"));
    }

    @Test
    void getBroaderNarrowerCloseMatch() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);

        assertRequestBuiltWith(
                "getBroaderNarrowerCloseMatch.ftlh",
                codeListParams(),
                () -> codeListsQueries.getBroaderNarrowerCloseMatch("NOTATION"));
    }

    @Test
    void getDetailedCodesWithoutSearch() throws RmesException {
        stubCodeListGraphAndBaseUri();

        assertRequestBuiltWith(
                GET_DETAILED_CODES_TEMPLATE,
                detailedCodesParams(true, "?labelLg1"),
                () -> codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.PARTIAL, null, 0, 0, "labelLg1"));
    }

    @Test
    void getCodeUriByNotation_returnsExpectedSparql() throws RmesException {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);

        String query = normalize(codeListsQueries.getCodeUriByNotation("CL_TEST", "CODE_1"));

        Assertions.assertEquals(normalize("""
                SELECT  ?uri
                WHERE { GRAPH <http://rdf.insee.fr/graphes/codes/nomenclatures> {
                ?codeList rdf:type skos:ConceptScheme .
                ?codeList skos:notation \"CL_TEST\" .
                ?uri skos:inScheme ?codeList .
                ?uri skos:notation \"CODE_1\" .
                 }}
                """), query);
    }

    @Test
    void getDetailedCodesShouldSortOnTheCodeWhenNoSortIsRequested() throws RmesException {
        stubCodeListGraphAndBaseUri();

        assertRequestBuiltWith(
                GET_DETAILED_CODES_TEMPLATE,
                detailedCodesParams(true, "?code"),
                () -> codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.PARTIAL, null, 0, 0, null));
    }

    @Test
    void getDetailedCodesShouldRejectASortOnAColumnTheQueryDoesNotSelect() {
        assertThrows(
                RmesRuntimeBadRequestException.class,
                () -> codeListsQueries.getDetailedCodes("NOTATION", CodeListKind.FULL, null, 1, 10, "unknownColumn"));
    }

    @Test
    void getDetailedCodesShouldRejectASortCarryingAnInjectionPayload() {
        assertThrows(
                RmesRuntimeBadRequestException.class,
                () -> codeListsQueries.getDetailedCodes(
                        "NOTATION", CodeListKind.FULL, null, 1, 10, "code } ORDER BY ?x #"));
    }

    private void stubCodeListGraphAndBaseUri() {
        when(graphs.codeListGraph()).thenReturn(CODES_LISTS_GRAPH);
        when(uris.codeListBaseUri()).thenReturn("codelist-base-uri");
    }

    /** Paramètres communs à tous les templates des listes de codes. */
    private static Map<String, Object> codeListParams() {
        Map<String, Object> map = new HashMap<>();
        map.put("CODES_LISTS_GRAPH", "<" + CODES_LISTS_GRAPH + ">");
        map.put("NOTATION", "\"NOTATION\"");
        map.put("LG1", "\"fr\"");
        map.put("LG2", "\"en\"");
        return map;
    }

    private static Map<String, Object> detailedCodesParams(boolean partial, String sort) {
        Map<String, Object> map = codeListParams();
        map.put("PARTIAL", partial);
        map.put("CODE_LIST_BASE_URI_PREFIX", "\"codelist-base-uri/\"");
        map.put("SORT", sort);
        return map;
    }

    /** La requête rendue est celle construite par le template avec exactement ces paramètres. */
    private static void assertRequestBuiltWith(String template, Map<String, Object> expectedParams, QueryCall call)
            throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("codes-list/"), eq(template), eq(expectedParams)))
                    .thenReturn("request");
            String query = call.call();
            Assertions.assertEquals("request", query);
        }
    }
}
