package fr.insee.rmes.persistance.service.sesame.operations.families;

import fr.insee.rmes.config.Config;

public class FamiliesQueries {

	public static String familiesQuery() {
		return "SELECT DISTINCT ?id ?label  \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?family a insee:StatisticalOperationFamily . \n" 
				+ "?family skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?family),'/operations/famille/') AS ?id) . \n"
				+ "}} \n" 
				+ "GROUP BY ?id ?label \n"
				+ "ORDER BY ?label ";
	}

	public static String familyQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?family skos:prefLabel ?prefLabelLg1 . \n" 
				+ "FILTER(REGEX(STR(?family),'/operations/famille/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?family),'/famille/') AS ?id) . \n" 

				+ "FILTER (lang(?prefLabelLg1) = '"	+ Config.LG1 + "') . \n" 
				+ "OPTIONAL {?family skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n" 

				+ "OPTIONAL {?family dcterms:abstract ?abstractLg1 . \n"
				+ "FILTER (lang(?abstractLg1) = '" + Config.LG1 + "') } . \n" 
				+ "OPTIONAL {?family dcterms:abstract ?abstractLg2 . \n"
				+ "FILTER (lang(?abstractLg2) = '" + Config.LG2 + "') } . \n" 

				+ "}} \n"
				+ "LIMIT 1";
	}

	public static String getSeries(String idFamily) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <http://rdf.insee.fr/graphes/operations> \n"
				+ "WHERE { \n" 

				+ "?family dcterms:hasPart ?id . \n"
				+ "?id skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?id skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"

				+ "FILTER(REGEX(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
	}

}