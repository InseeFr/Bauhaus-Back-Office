package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.config.Config;

public class ItemsQueries {
	
	private ItemsQueries() {
		throw new IllegalStateException("Utility class");
	}
	
	public static String itemQuery(String classificationId, String itemId) {
		return "SELECT ?classificationId ?itemId ?prefLabelLg1 ?prefLabelLg2 ?isValidated \n"
				+ "?broaderLg1 ?broaderLg2 ?idBroader ?conceptVersion ?altLabelLg1 ?altLabelLg2\n"
				+ "WHERE { \n"
				+ "?item skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') \n"
				+ "?item skos:inScheme ?classification . \n"
				+ "FILTER(REGEX(STR(?classification),'/codes/" + classificationId + "/')) \n"
				+ "FILTER(STRENDS(STR(?item),'/" + itemId + "')) \n"
				+ "BIND('" + classificationId + "' as ?classificationId) \n"
				+ "OPTIONAL {?item skos:altLabel ?altLabelLg1 . \n"
				+ "FILTER (lang(?altLabelLg1) = '" + Config.LG1 + "')} . \n"
				+ "OPTIONAL {?item skos:altLabel ?altLabelLg2 . \n"
				+ "FILTER (lang(?altLabelLg2) = '" + Config.LG2 + "')} . \n"
				+ "OPTIONAL {?item skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "')} . \n"
				+ "?item skos:notation ?itemId . \n"
				+ "?item insee:isValidated ?isValidated . \n"
				+ "OPTIONAL {?item ?versionnedNote ?versionnedNoteURI . \n"
				+ "?versionnedNoteURI insee:conceptVersion ?conceptVersion . } \n"
				// Parent
				+ "OPTIONAL {?item skos:broader ?broaderURI . \n"
				+ "?broaderURI skos:prefLabel ?broaderLg1 . \n"
				+ "FILTER (lang(?broaderLg1) = '" + Config.LG1 + "') \n"
				+ "?broaderURI skos:notation ?idBroader . } \n"
				+ "OPTIONAL {?item skos:broader ?broaderURI . \n"
				+ "?broaderURI skos:prefLabel ?broaderLg2 . \n"
				+ "FILTER (lang(?broaderLg2) = '" + Config.LG2 + "') } \n"
				+ "} \n"
				+ "ORDER BY DESC(xsd:integer(?conceptVersion)) \n"
				+ "LIMIT 1";	
	}
	
	public static String itemAltQuery(String classificationId, String itemId) {
		return "SELECT ?shortLabelLg1 ?shortLabelLg2 ?length WHERE { \n"
				+ "?item ?altOrPref ?shortLabel . \n"
				+ "?item skos:inScheme ?classification . \n"
				+ "FILTER(REGEX(STR(?classification),'/codes/" + classificationId + "/')) \n"
				+ "FILTER(STRENDS(STR(?item),'/" + itemId + "')) \n"
				+ "OPTIONAL {?shortLabel skosxl:literalForm ?shortLabelLg1 . \n"
				+ "FILTER (lang(?shortLabelLg1) = '" + Config.LG1 + "')} \n"
				+ "?shortLabel xkos:maxLength ?length . \n"
				+ "OPTIONAL {?shortLabel skosxl:literalForm ?shortLabelLg2 . \n"
				+ "FILTER (lang(?shortLabelLg2) = '" + Config.LG2 + "')} \n"
				+ "}";
	}
	
	public static String itemNotesQuery(String classificationId, String itemId, int conceptVersion) {
		return "SELECT ?definitionLg1 ?definitionLg2 ?scopeNoteLg1 ?scopeNoteLg2 ?coreContentNoteLg1 ?coreContentNoteLg2 "
				+ "?additionalContentNoteLg1 ?additionalContentNoteLg2 ?exclusionNoteLg1 ?exclusionNoteLg2 \n"
				+ "?changeNoteLg1 ?changeNoteLg2 ?changeNoteDate \n"
				+ "WHERE { \n" 
				//+ "?item skos:notation '" + itemId + "' . \n" 
				+ "?item skos:inScheme ?classification . \n"
				+ "FILTER(REGEX(STR(?classification),'/codes/" + classificationId + "/')) \n"
				+ "FILTER(STRENDS(STR(?item),'/" + itemId + "')) \n"

				// definitionLg1
				+ "OPTIONAL {?item skos:definition ?defLg1 . \n"
				+ "?defLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?defLg1 evoc:noteLiteral ?definitionLg1 . \n"
				+ "?defLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				// definitionLg2
				+ "OPTIONAL {?item skos:definition ?defLg2 . \n"
				+ "?defLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?defLg2 evoc:noteLiteral ?definitionLg2 . \n"
				+ "?defLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				//scopeNoteLg1
				+ "OPTIONAL {?item skos:scopeNote ?scopeLg1 . \n"
				+ "?scopeLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
				+ "?scopeLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} .  \n"
				// scopeNoteLg2
				+ "OPTIONAL {?item skos:scopeNote ?scopeLg2 . \n"
				+ "?scopeLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
				+ "?scopeLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// coreContentNoteLg1
				+ "OPTIONAL {?item xkos:coreContentNote ?coreContentLg1 . \n"
				+ "?coreContentLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?coreContentLg1 evoc:noteLiteral ?coreContentNoteLg1 . \n"
				+ "?coreContentLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// coreContentNoteLg2
				+ "OPTIONAL {?item xkos:coreContentNote ?coreContentLg2 . \n"
				+ "?coreContentLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?coreContentLg2 evoc:noteLiteral ?coreContentNoteLg2 . \n"
				+ "?coreContentLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// additionalContentNoteLg1
				+ "OPTIONAL {?item xkos:additionalContentNote ?additionalContentLg1 . \n"
				+ "?additionalContentLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?additionalContentLg1 evoc:noteLiteral ?additionalContentNoteLg1 . \n"
				+ "?additionalContentLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// additionalContentNoteLg2
				+ "OPTIONAL {?item xkos:additionalContentNote ?additionalContentLg2 . \n"
				+ "?additionalContentLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?additionalContentLg2 evoc:noteLiteral ?additionalContentNoteLg2 . \n"
				+ "?additionalContentLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// exclusionNoteLg1
				+ "OPTIONAL {?item xkos:exclusionNote ?exclusionLg1 . \n"
				+ "?exclusionLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?exclusionLg1 evoc:noteLiteral ?exclusionNoteLg1 . \n"
				+ "?exclusionLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// exclusionNoteLg2
				+ "OPTIONAL {?item xkos:exclusionNote ?exclusionLg2 . \n"
				+ "?exclusionLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?exclusionLg2 evoc:noteLiteral ?exclusionNoteLg2 . \n"
				+ "?exclusionLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int . \n"
				+ "} . \n"
				// Note changement Lg1
				+ "OPTIONAL {?item skos:changeNote ?noteChangeLg1 . \n"
				+ "?noteChangeLg1 dcterms:issued ?changeNoteDate . \n"
				+ "?noteChangeLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?noteChangeLg1 evoc:noteLiteral ?changeNoteLg1 . \n"
				+ "?noteChangeLg1 insee:conceptVersion '" + conceptVersion + "'^^xsd:int} . \n"
				// Note changement Lg2
				+ "OPTIONAL {?item skos:changeNote ?noteChangeLg2 . \n"
				+ "?noteChangeLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?noteChangeLg2 evoc:noteLiteral ?changeNoteLg2 . \n"
				+ "?noteChangeLg2 insee:conceptVersion '" + conceptVersion + "'^^xsd:int} . \n"
				+ "} \n";	
	}
	
	public static String itemNarrowersQuery(String classificationId, String itemId) {
		return "SELECT ?id ?labelLg1 ?labelLg2 WHERE { \n"
				+ "?item skos:narrower ?narrower . \n"
				+ "?item skos:inScheme ?classification . \n"
				+ "?narrower skos:inScheme ?classificationNarrower . \n"
				+ "FILTER(REGEX(STR(?classification),'/codes/" + classificationId + "/')) \n"
				+ "FILTER(REGEX(STR(?classificationNarrower),'/codes/" + classificationId + "/')) \n"
				+ "FILTER(STRENDS(STR(?item),'/" + itemId + "')) \n"
				+ "?narrower skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "?narrower skos:notation ?id . \n"
				+ "OPTIONAL {?narrower skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} \n"
				+ "}"
				+ "ORDER BY ?id ";
	}

}
