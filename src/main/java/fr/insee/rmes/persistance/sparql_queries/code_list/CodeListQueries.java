package fr.insee.rmes.persistance.sparql_queries.code_list;


import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeListQueries extends GenericQueries {
	private static final String CODES_LISTS_GRAPH = "CODES_LISTS_GRAPH";
	private static final String CODES_LIST = "codes-list/";
	private static final String PARTIAL = "PARTIAL";
	private static final String NOTATION = "NOTATION";
	private static final String URI_CODESLIST = "URI_CODESLIST";
	public static final String CODE_LIST_BASE_URI = "CODE_LIST_BASE_URI";

	public static String isCodesListValidated(String codesListUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("IRI", codesListUri);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "isCodesListValidated.ftlh", params);
	}

	public static String getAllCodesLists(boolean partial) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getAllCodesLists.ftlh", params);
	}

	public static int getPerPageConfiguration(Integer perPage){
		if(perPage == null){
			return config.getPerPage();
		}
		return perPage;
	}

	public static String getBroaderNarrowerCloseMatch(String notation) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, notation);

		return FreeMarkerUtils.buildRequest(CODES_LIST, "getBroaderNarrowerCloseMatch.ftlh", params);
	}

	private static void addSearchPredicates(Map<String, Object> params, List<String> search) {
		if(search != null){
			search.forEach(s -> {
				if(!StringUtils.isEmpty(s)){
					String key = s.startsWith("code:") ? "SEARCH_CODE" : "SEARCH_LABEL_LG1";
					params.put(key, s.substring(s.indexOf(":") + 1 ));
				}
			});
		}
	}

	public static String getDetailedCodes(String notation, boolean partial, List<String> search, int page, Integer perPage) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		int perPageValue = getPerPageConfiguration(perPage);
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put(NOTATION, notation);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(PARTIAL, partial);
		params.put(CODE_LIST_BASE_URI, config.getCodeListBaseUri());

		addSearchPredicates(params, search);

		if(perPageValue > 0){
			params.put("OFFSET", perPageValue * (page - 1));
			params.put("PER_PAGE", perPageValue);
		}
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodes.ftlh", params);
	}

	public static String countCodesForCodeList(String notation, List<String> search) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put(NOTATION, notation);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		addSearchPredicates(params, search);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "countNumberOfCodes.ftlh", params);
	}

	public static String getCodeListItemsByNotation(String notation, int page, Integer perPage) throws RmesException {
		int perPageValue = getPerPageConfiguration(perPage);

		Map<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put(NOTATION, notation);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		if(perPageValue > 0){
			var offset = perPageValue * (page - 1);
			params.put("OFFSET", String.valueOf(offset));
			params.put("PER_PAGE", String.valueOf(perPageValue));
		}
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListItemsByNotation.ftlh", params);
	}

	public static String getContributorsByCodesListUri(String uriCodesList) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(URI_CODESLIST, uriCodesList);
		return buildCodesListRequest("getCodesListContributorsByUriQuery.ftlh", params);
	}

	public static String getCodeListLabelByNotation(String notation) {
		return "SELECT ?codeListLabelLg1 ?codeListLabelLg2 \n"
				+ "WHERE { GRAPH <"+config.getCodeListGraph()+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notation + "' . \n"
				+ "?codeList skos:prefLabel ?codeListLabelLg1 . \n"
				+ "FILTER (lang(?codeListLabelLg1) = '" + config.getLg1() + "') . \n"
				+ "?codeList skos:prefLabel ?codeListLabelLg2 . \n"
				+ "FILTER (lang(?codeListLabelLg2) = '" + config.getLg2() + "') . \n"
				+ " }}";
	}

	public static String getCodeByNotation(String notationCodeList, String notationCode) {
		return "SELECT  ?labelLg1 ?labelLg2  \n"
				+ "WHERE { GRAPH <"+config.getCodeListGraph()+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notationCodeList + "' . \n"
				+ "?item skos:inScheme ?codeList . \n"
				+ "?item skos:notation '"+notationCode +"' . \n"
				+ "?item skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "?item skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') . \n"
				+ " }}";
	}

	public static String getCodeUriByNotation(String notationCodeList, String notationCode) {
		return "SELECT  ?uri  \n"
				+ "WHERE { GRAPH <"+config.getCodeListGraph()+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notationCodeList + "' . \n"
				+ "?uri skos:inScheme ?codeList . \n"
				+ "?uri skos:notation '"+notationCode +"' . \n"
				+ " }}";
	}

	public static String geCodesListByIRI(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("CODE_LIST", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListByIRI.ftlh", params);
	}

	public static String getDetailedCodeListByNotation(String notation, String baseInternalUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, notation);
		params.put(CODE_LIST_BASE_URI, config.getCodeListBaseUri());
		params.put("BASE_INTERNAL_URI", baseInternalUri);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesList.ftlh", params);
	}
	public static String getCodeListIRIByNotation(String notation, String baseInternalUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(NOTATION, notation);
		params.put(CODE_LIST_BASE_URI, config.getCodeListBaseUri());
		params.put("BASE_INTERNAL_URI", baseInternalUri);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListIRIByNotation.ftlh", params);
	}


	public static String getCodesListsForSearch(boolean partial) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesListForSearch.ftlh", params);
	}

	public static String getCodesForSearch( boolean partial) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodesForSearch.ftlh", params);
	}




	private static void initParams(HashMap<String, Object> params) {
		params.put(CODES_LISTS_GRAPH, config.getCodeListGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
	}

	private CodeListQueries() {
		throw new IllegalStateException("Utility class");
	}


	public static String checkCodeListUnicity(String id, String iri, String seeAlso, boolean partial) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("ID", id);
		params.put("IRI", iri);
		params.put("SEE_ALSO", seeAlso);
		params.put(PARTIAL, partial);

		return FreeMarkerUtils.buildRequest(CODES_LIST, "checkCodeListUnicity.ftlh", params);
	}

	public static String getPartialCodeListByParentUri(String iri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		initParams(params);
		params.put("IRI", iri);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getPartialCodeListByParentUri.ftlh", params);
	}

	private static String buildCodesListRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest(CODES_LIST, fileName, params);
	}
}