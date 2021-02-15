package fr.insee.rmes.persistance.sparql_queries.code_list;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

import java.util.HashMap;

public class CodeListQueries {

	public static String isCodesListValidated(String codesListUri) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("CODES_LISTS_GRAPH", Config.CODELIST_GRAPH);
		params.put("IRI", codesListUri);
		return FreeMarkerUtils.buildRequest("codes-list/", "isCodesListValidated.ftlh", params);
	}

	public static String getAllCodesLists() throws RmesException {

		HashMap<String, Object> params = new HashMap<>();
		params.put("CODES_LISTS_GRAPH", Config.CODELIST_GRAPH);
		return FreeMarkerUtils.buildRequest("codes-list/", "getAllCodesLists.ftlh", params);
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
	
	public static String getCodeListNotationByUri(String uri) {
		return "SELECT ?notation \n"
				+ "WHERE { GRAPH <"+Config.CODELIST_GRAPH+"> { \n"
						
				+ "    OPTIONAL {<"+uri+"> rdfs:seeAlso ?codeListCS . \n" 
				+ "      ?codeListCS rdf:type skos:ConceptScheme . \n"
				+"       ?codeListCS skos:notation ?notation "
				+ "		} \n"
				
				+ "    OPTIONAL {<"+uri+"> rdf:type skos:ConceptScheme . \n"
				+"      <"+uri+"> skos:notation ?notation "
				+ "		} \n"
		
				+ " }}";
	}

	public static String geCodesListByIRI(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("CODES_LISTS_GRAPH", Config.CODELIST_GRAPH);
		params.put("CODE_LIST", id);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
		return FreeMarkerUtils.buildRequest("codes-list/", "getCodeListByIRI.ftlh", params);
	}
}