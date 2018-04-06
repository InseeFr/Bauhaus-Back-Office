package fr.insee.rmes.persistance.service.sesame.classifications.series;

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
	
}