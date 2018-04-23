package fr.insee.rmes.persistance.service.sesame.classifications.correspondences;

import fr.insee.rmes.config.Config;

public class CorrespondencesQueries {
	
	public static String correspondencesQuery() {
		return "SELECT DISTINCT ?id ?label \n"
				+ "WHERE { \n"
				+ "?correspondence rdf:type xkos:Correspondence . \n"
				+ "?correspondence skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id) \n"
				+ "} \n"
				+ "ORDER BY ?label ";
	}

}
