package fr.insee.rmes.persistance.service.sesame.code_list;

import fr.insee.rmes.config.Config;

public class CodeListQueries {

	public static String getCodeListItemsByNotation(String notation) {
		return "SELECT ?code ?labelLg1 ?labelLg2 \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes> { \n"
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
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes> { \n"
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
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes> { \n"
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
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes> { \n"
				+ "?codeList rdf:type skos:ConceptScheme . \n"
				+ "?codeList skos:notation '" + notationCodeList + "' . \n"
				+ "?uri skos:inScheme ?codeList . \n"
				+ "?uri skos:notation '"+notationCode +"' . \n"
				+ " }}";
	}
	
	public static String getCodeListNotationByUri(String uri) {
		return "SELECT ?notation \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes> { \n"
						
				+ "    OPTIONAL {<"+uri+"> rdfs:seeAlso ?codeListCS . \n" 
				+ "      ?codeListCS rdf:type skos:ConceptScheme . \n"
				+"       ?codeListCS skos:notation ?notation "
				+ "		} \n"
				
				+ "    OPTIONAL {<"+uri+"> rdf:type skos:ConceptScheme . \n"
				+"      <"+uri+"> skos:notation ?notation "
				+ "		} \n"
		
				+ " }}";
	}

}