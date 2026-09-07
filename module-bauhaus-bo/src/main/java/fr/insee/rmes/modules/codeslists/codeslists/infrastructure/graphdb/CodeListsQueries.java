package fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb;


import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CodeListsQueries {
	private static final String CODES_LISTS_GRAPH = "CODES_LISTS_GRAPH";
	private static final String CODES_LIST = "codes-list/";
	private static final String PARTIAL = "PARTIAL";
	private static final String NOTATION = "NOTATION";
	private static final String CODE = "CODE";
	public static final String CODE_LIST_BASE_URI = "CODE_LIST_BASE_URI";
	private static final String DEFAULT_SORT = "code";
	/**
	 * Un nom de variable ne peut pas être échappé : il est validé contre les colonnes que
	 * getDetailedCodes.ftlh projette, sans quoi le tri ouvrirait une injection dans le ORDER BY.
	 */
	private static final Set<String> SORTABLE_COLUMNS = Set.of(
			"code", "labelLg1", "labelLg2", "descriptionLg1", "descriptionLg2", "parents", "codeUri",
			"lastCodeUriSegment");

    private final BauhausUriProperties uris;
    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;
	private final PaginationProperties paginationProperties;

	public CodeListsQueries(BauhausUriProperties uris, BauhausLanguagesProperties languages, GraphsProperties graphs, PaginationProperties paginationProperties) {
        this.uris = uris;
        this.languages = languages;
        this.graphs = graphs;
		this.paginationProperties = paginationProperties;
	}

	public String isCodesListValidated(String codesListUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put("IRI", SparqlLiterals.iri(codesListUri));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "isCodesListValidated.ftlh", params);
	}

	public String getAllCodesLists(CodeListKind kind) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(PARTIAL, kind.isPartial());
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getAllCodesLists.ftlh", params);
	}

	public int getPerPageConfiguration(Integer perPage) {
		if (perPage == null) {
			return paginationProperties.perPage();
		}
		return perPage;
	}

	public String getBroaderNarrowerCloseMatch(String notation) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, SparqlLiterals.literal(notation));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getBroaderNarrowerCloseMatch.ftlh", params);
	}

	private void addSearchPredicates(Map<String, Object> params, List<String> search) {
		if (search != null) {
			search.forEach(s -> {
				if (!s.isEmpty()) {
					String key = s.startsWith("code:") ? "SEARCH_CODE" : "SEARCH_LABEL_LG1";
					params.put(key, SparqlLiterals.literal(s.substring(s.indexOf(":") + 1)));
				}
			});
		}
	}

	public String getDetailedCodes(String notation, CodeListKind kind, List<String> search, int page, Integer perPage, String sort) throws RmesException {
		String sortVariable = SparqlLiterals.variable(sortColumn(sort));
		Map<String, Object> params = new HashMap<>();
		int perPageValue = getPerPageConfiguration(perPage);
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put(NOTATION, SparqlLiterals.literal(notation));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		params.put(PARTIAL, kind.isPartial());
		params.put("CODE_LIST_BASE_URI_PREFIX", SparqlLiterals.literal(uris.codeListBaseUri() + "/"));
		params.put("SORT", sortVariable);

		addSearchPredicates(params, search);

		if (perPageValue > 0) {
			params.put("OFFSET", perPageValue * (page - 1));
			params.put("PER_PAGE", perPageValue);
		}
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodes.ftlh", params);
	}

	private static String sortColumn(String sort) {
		if (sort == null) {
			return DEFAULT_SORT;
		}
		if (!SORTABLE_COLUMNS.contains(sort)) {
			throw new RmesRuntimeBadRequestException("Unknown sort column: " + sort + ". Expected one of " + SORTABLE_COLUMNS);
		}
		return sort;
	}

	public String countCodesForCodeList(String notation, List<String> search) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put(NOTATION, SparqlLiterals.literal(notation));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		addSearchPredicates(params, search);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "countNumberOfCodes.ftlh", params);
	}

	public String getCodeListItemsByNotation(String notation, int page, Integer perPage) throws RmesException {
		int perPageValue = getPerPageConfiguration(perPage);

		Map<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put(NOTATION, SparqlLiterals.literal(notation));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		if (perPageValue > 0) {
			var offset = perPageValue * (page - 1);
			params.put("OFFSET", String.valueOf(offset));
			params.put("PER_PAGE", String.valueOf(perPageValue));
		}
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListItemsByNotation.ftlh", params);
	}

	public String getCodeListLabelByNotation(String notation) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, SparqlLiterals.literal(notation));
		return buildCodesListRequest("getCodeListLabelByNotation.ftlh", params);
	}

	/**
	 * Existence d'un code <em>dans</em> une liste. Pointait sur getCodeListLabelByNotation.ftlh, qui
	 * n'interpole que la notation de la liste : la garde d'unicité de l'ajout de code répondait donc
	 * « Code already exists » pour n'importe quel code d'une liste existante, et le 404 de la
	 * suppression ne se déclenchait jamais.
	 */
	public String getCodeByNotation(String notationCodeList, String notationCode) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, SparqlLiterals.literal(notationCodeList));
		params.put(CODE, SparqlLiterals.literal(notationCode));
		return buildCodesListRequest("getCodeByNotation.ftlh", params);
	}

	public String getCodeUriByNotation(String notationCodeList, String notationCode) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put(NOTATION, SparqlLiterals.literal(notationCodeList));
		params.put(CODE, SparqlLiterals.literal(notationCode));
		return buildCodesListRequest("getCodeUriByNotation.ftlh", params);
	}

	public String geCodesListByIRI(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put("CODE_LIST", SparqlLiterals.iri(id));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListByIRI.ftlh", params);
	}

	/**
	 * L'IRI est l'identité stable d'une liste de codes : sa notation, elle, peut être renommée.
	 */
	public String getCodesListByIri(String iri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("IRI", SparqlLiterals.iri(iri));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodesListByIri.ftlh", params);
	}

	public String getDetailedCodeListByNotation(String notation) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, SparqlLiterals.literal(notation));
		params.put("CODE_LIST_BASE_URI_PREFIX", SparqlLiterals.literal(uris.codeListBaseUri() + "/"));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesList.ftlh", params);
	}

	public String getCodesListsForSearch(CodeListKind kind) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(PARTIAL, kind.isPartial());
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesListForSearch.ftlh", params);
	}

	public String getCodesForSearch(CodeListKind kind) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(PARTIAL, kind.isPartial());
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodesForSearch.ftlh", params);
	}

	private void initParams(HashMap<String, Object> params) {
		params.put(CODES_LISTS_GRAPH, SparqlLiterals.iri(graphs.codeListGraph()));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
	}

	public String checkCodeListUnicity(String id, String iri, String seeAlso, CodeListKind kind) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", SparqlLiterals.literal(id));
		params.put("IRI", SparqlLiterals.iri(iri));
		params.put("SEE_ALSO", SparqlLiterals.iri(seeAlso));
		params.put(PARTIAL, kind.isPartial());
		return FreeMarkerUtils.buildRequest(CODES_LIST, "checkCodeListUnicity.ftlh", params);
	}

	public String getPartialCodeListByParentUri(String iri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("IRI", SparqlLiterals.iri(iri));
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getPartialCodeListByParentUri.ftlh", params);
	}

	private String buildCodesListRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest(CODES_LIST, fileName, params);
	}

	public String getCodesListContributors(String IRI) throws RmesException {
		Map<String, Object> params = Map.of("GRAPH", SparqlLiterals.iri(graphs.codeListGraph()), "IRI", SparqlLiterals.iri(IRI), "PREDICATE", "dc:contributor");
		return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
	}
}