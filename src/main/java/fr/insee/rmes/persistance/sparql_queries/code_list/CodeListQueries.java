package fr.insee.rmes.persistance.sparql_queries.code_list;

import java.util.HashMap;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class CodeListQueries {

	private static final String CODES_LISTS_GRAPH = "CODES_LISTS_GRAPH";
	private static final String CODES_LIST = "codes-list/";
	private static final String PARTIAL = "PARTIAL";
	private static final String NOTATION = "NOTATION";

	public static String isCodesListValidated(String codesListUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, Config.CODELIST_GRAPH);
		params.put("IRI", codesListUri);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "isCodesListValidated.ftlh", params);
	}

	public static String getAllCodesLists(boolean partial) throws RmesException {

		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, Config.CODELIST_GRAPH);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getAllCodesLists.ftlh", params);
	}
	public static String getCodeListItemsByNotation(String notation) {
		return "SELECT ?code ?labelLg1 ?labelLg2 \n"
				+ "WHERE { GRAPH <"+Config.CODELIST_GRAPH+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notation + "' . \n"
				+ "?item skos:inScheme ?codeList . \n"
				+ "?item skos:notation ?code . \n"
				+ "?item skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?item skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ " }}";
	}

	public static String getCodeListLabelByNotation(String notation) {
		return "SELECT ?codeListLabelLg1 ?codeListLabelLg2 \n"
				+ "WHERE { GRAPH <"+Config.CODELIST_GRAPH+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notation + "' . \n"
				+ "?codeList skos:prefLabel ?codeListLabelLg1 . \n"
				+ "FILTER (lang(?codeListLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "?codeList skos:prefLabel ?codeListLabelLg2 . \n"
				+ "FILTER (lang(?codeListLabelLg2) = '" + Config.LG2 + "') . \n"
				+ " }}";
	}

	public static String getCodeByNotation(String notationCodeList, String notationCode) {
		return "SELECT  ?labelLg1 ?labelLg2  \n"
				+ "WHERE { GRAPH <"+Config.CODELIST_GRAPH+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notationCodeList + "' . \n"
				+ "?item skos:inScheme ?codeList . \n"
				+ "?item skos:notation '"+notationCode +"' . \n"
				+ "?item skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?item skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ " }}";
	}
	
	public static String getCodeUriByNotation(String notationCodeList, String notationCode) {
		return "SELECT  ?uri  \n"
				+ "WHERE { GRAPH <"+Config.CODELIST_GRAPH+"> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notationCodeList + "' . \n"
				+ "?uri skos:inScheme ?codeList . \n"
				+ "?uri skos:notation '"+notationCode +"' . \n"
				+ " }}";
	}

	public static String geCodesListByIRI(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, Config.CODELIST_GRAPH);
		params.put("CODE_LIST", id);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodeListByIRI.ftlh", params);
	}

	public static String getDetailedCodeListByNotation(String notation) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put(NOTATION, notation);
		params.put("CODE_LIST_BASE_URI", Config.CODE_LIST_BASE_URI);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesList.ftlh", params);
	}

	public static String getCodesListsForSearch(boolean partial) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodesListForSearch.ftlh", params);
	}

	public static String getCodesForSearch(boolean partial) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodesForSearch.ftlh", params);
	}

	public static String getDetailedCodes(String notation, boolean partial) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put(NOTATION, notation);
		params.put(PARTIAL, partial);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getDetailedCodes.ftlh", params);
	}

	public static String getCodesSeq(String notation) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put(NOTATION, notation);
		return FreeMarkerUtils.buildRequest(CODES_LIST, "getCodesSeq.ftlh", params);
	}

	private static HashMap<String, Object> getInitParams() {
		HashMap<String, Object> params = new HashMap<>();
		params.put(CODES_LISTS_GRAPH, Config.CODELIST_GRAPH);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		return params;
	}
	
	  private CodeListQueries() {
		    throw new IllegalStateException("Utility class");
	}


	public static String checkCodeListUnicity(String id, String iri, String seeAlso, boolean partial) throws RmesException {
		HashMap<String, Object> params = getInitParams();
		params.put("ID", id);
		params.put("IRI", iri);
		params.put("SEE_ALSO", seeAlso);
		params.put("PARTIAL", partial);

		return FreeMarkerUtils.buildRequest("codes-list/", "checkCodeListUnicity.ftlh", params);
	}
}