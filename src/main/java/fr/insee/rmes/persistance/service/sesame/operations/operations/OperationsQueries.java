package fr.insee.rmes.persistance.service.sesame.operations.operations;

import fr.insee.rmes.config.Config;

public class OperationsQueries {

	public static String operationsQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?operation a insee:StatisticalOperation . \n" 
				+ "?operation skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) . \n"
				+ "OPTIONAL{?operation skos:altLabel ?altLabelLg1 . \n" 
				+ "FILTER (lang(?altLabelLg1) = '" + Config.LG1 + "')} \n" 
				+ "}} \n" 
				+ "GROUP BY ?id ?label \n"
				+ "ORDER BY ?label ";
	}

	public static String operationQuery(String id){
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?motherSeries ?motherSeriesLabelLg1 ?motherSeriesLabelLg2\n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?operation skos:prefLabel ?prefLabelLg1 . \n" 
				+ "FILTER(REGEX(STR(?operation),'/operations/operation/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?operation),'/operation/') AS ?id) . \n" 

		+ "FILTER (lang(?prefLabelLg1) = '"	+ Config.LG1 + "') . \n" 
		+ "OPTIONAL {?operation skos:prefLabel ?prefLabelLg2 . \n"
		+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n" 

		+ "?motherSeries dcterms:hasPart ?operation . \n"
		+ "?motherSeries skos:prefLabel ?motherSeriesLabelLg1 . \n"
		+ "FILTER (lang(?motherSeriesLabelLg1) = '" + Config.LG1 + "') . \n"
		+ "?motherSeries skos:prefLabel ?motherSeriesLabelLg2 . \n"
		+ "FILTER (lang(?motherSeriesLabelLg2) = '" + Config.LG2 + "') . \n"

		+ "}} \n"
		+ "LIMIT 1";
	}

	public static String altLabel(String id, String lang) {
		return "SELECT ?altLabel \n" + "WHERE { \n" 
				+ "?operation skos:altLabel ?altLabel \n"
				+ "FILTER (lang(?altLabel) = '" + lang + "') . \n" 
				+ "FILTER(REGEX(STR(?series),'/operations/operation/" + id + "')) . \n"
				+ "}";
	}



}
