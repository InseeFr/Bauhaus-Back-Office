package fr.insee.rmes.persistance.service.sesame.operations.series;

import fr.insee.rmes.config.Config;

public class SeriesQueries {

	public static String seriesQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" 
				+ "?series skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . \n"
				+ "OPTIONAL{?series skos:altLabel ?altLabelLg1 . \n" + "FILTER (lang(?altLabelLg1) = '" + Config.LG1
				+ "')} \n" + "}} \n" + "GROUP BY ?id ?label \n" + "ORDER BY ?label ";
	}

	public static String oneSeriesQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?series skos:prefLabel ?prefLabelLg1 . \n" 
				+ "FILTER(REGEX(STR(?series),'/operations/serie/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?series),'/serie/') AS ?id) . \n" 
				+ "FILTER (lang(?prefLabelLg1) = '"
				+ Config.LG1 + "') . \n" 
				+ "OPTIONAL {?concept skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n" 
				+ "}} \n"
				+ "LIMIT 1";
	}

	public static String altLabel(String id, String lang) {
		return "SELECT ?altLabel \n" + "WHERE { \n" + "?series skos:altLabel ?altLabel \n"
				+ "FILTER (lang(?altLabel) = '" + lang + "') . \n" 
				+ "FILTER(REGEX(STR(?series),'/operations/serie/"
				+ id + "')) . \n" + "}";
	}

}
