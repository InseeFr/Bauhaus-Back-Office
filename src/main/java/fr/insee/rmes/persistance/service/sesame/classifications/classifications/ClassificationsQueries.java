package fr.insee.rmes.persistance.service.sesame.classifications.classifications;

import fr.insee.rmes.config.Config;

public class ClassificationsQueries {
	
	public static String classificationsQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "FILTER(regex(str(?classification),'/codes/')) \n"
			+ "?classification skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?label ";	
	}
	
}
