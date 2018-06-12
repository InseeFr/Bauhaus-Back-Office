package fr.insee.rmes.persistance.service.sesame.operations.series;

import fr.insee.rmes.config.Config;

public class SeriesQueries {
	
	public static String seriesQuery() {
		return "SELECT ?series ?label \n"
			+ "FROM <http://rdf.insee.fr/graphes/operations> \n"
			+ "WHERE { \n"
			+ "?series a insee:StatisticalOperationSeries ; skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
			+ "} \n"
			+ "ORDER BY ?series ";	
	}

}
