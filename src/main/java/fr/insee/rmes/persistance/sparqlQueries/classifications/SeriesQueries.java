package fr.insee.rmes.persistance.sparqlQueries.classifications;

import fr.insee.rmes.config.Config;

public class SeriesQueries {
	
	public static String seriesQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { GRAPH<http://rdf.insee.fr/graphes/codes/nomenclatures> { \n"
			+ "?series skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
			+ "FILTER(REGEX(STR(?series),'/serieDeNomenclatures/')) . \n"
			+ "BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) } \n"
			+ "} \n"
			+ "ORDER BY ?label ";	
	}
	
	public static String oneSeriesQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 \n"
				+ "?scopeNoteLg1 ?scopeNoteLg2 ?subject ?publisher ?covers ?familyLg1 ?familyLg2 ?idFamily \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/codes/nomenclatures> { \n"
				+ "?series skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER(REGEX(STR(?series),'/serieDeNomenclatures/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?series skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n"
				+ "{OPTIONAL{ \n"
				+ "SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE { \n"
				+ "?series skos:altLabel ?altLg1 . \n"
				+ "FILTER (lang(?altLg1) = '" + Config.LG1 + "')  . \n"
				+ "FILTER(REGEX(STR(?series),'/codes/serieDeNomenclatures/" + id + "')) . \n"
				+ "}}} \n"
				+ "{OPTIONAL{ \n"
				+ "SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE { \n"
				+ "?series skos:altLabel ?altLg2 . \n"
				+ "FILTER (lang(?altLg2) = '" + Config.LG2 + "')  . \n"
				+ "FILTER(REGEX(STR(?series),'/codes/serieDeNomenclatures/" + id + "')) . \n"
				+ "}}} \n"
				+ "OPTIONAL {?series dc:subject ?subject } . \n"
				+ "OPTIONAL {?series dc:publisher ?publisher } . \n"
				+ "OPTIONAL {?series xkos:covers ?covers } . \n"
				// Remarque lg1
				+ "OPTIONAL {?series skos:scopeNote ?scopeLg1 . \n"
				+ "?scopeLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
				+ "} . \n"
				// Remarque Lg2
				+ "OPTIONAL {?series skos:scopeNote ?scopeLg2 . \n"
				+ "?scopeLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
				+ "} . \n"
				+ "OPTIONAL {?series xkos:belongsTo ?familyURI . \n"
				+ "?familyURI skos:prefLabel ?familyLg1 . \n"
				+ "FILTER (lang(?familyLg1) = '" + Config.LG1 + "')  . \n"
				+ "BIND(STRAFTER(STR(?familyURI),'/codes/familleDeNomenclatures/') AS ?idFamily) } . \n"
				+ "OPTIONAL {?series xkos:belongsTo ?familyURI . \n"
				+ "?familyURI skos:prefLabel ?familyLg2 . \n"
				+ "FILTER (lang(?familyLg2) = '" + Config.LG2 + "') }  . \n"
				+ "}} \n"
				+ "LIMIT 1";	
	}
	
	public static String seriesMembersQuery(String id) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 \n"
			+ "WHERE { \n"
			+ "?classification xkos:belongsTo ?series . \n"
			+ "?classification skos:prefLabel ?labelLg1 . \n"
			+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
			+ "OPTIONAL {?classification skos:prefLabel ?labelLg2 . \n"
			+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') } \n"
			+ "FILTER(REGEX(STR(?series),'/serieDeNomenclatures/" + id + "')) . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?labelLg1 ";	
	}
	
}