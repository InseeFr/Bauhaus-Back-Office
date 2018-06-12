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
			+ "OPTIONAL{?series skos:altLabel ?altLabelLg1 . \n"
			+ "FILTER (lang(?altLabelLg1) = '" + Config.LG1 + "')} \n"
			+ "}} \n"
			+ "GROUP BY ?id ?label \n"
			+ "ORDER BY ?label ";	
	}

}
