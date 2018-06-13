package fr.insee.rmes.persistance.service.sesame.operations.operations;

import fr.insee.rmes.config.Config;

public class OperationsQueries {

	public static String operationsQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?operation a insee:StatisticalOperation . \n" + "?operation skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) . \n"
				+ "OPTIONAL{?operation skos:altLabel ?altLabelLg1 . \n" + "FILTER (lang(?altLabelLg1) = '" + Config.LG1
				+ "')} \n" + "}} \n" + "GROUP BY ?id ?label \n" + "ORDER BY ?label ";
	}

}
