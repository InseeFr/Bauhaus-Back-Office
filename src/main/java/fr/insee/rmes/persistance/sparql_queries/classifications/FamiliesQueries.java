package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.config.Config;

public class FamiliesQueries {
	
	public static String familiesQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { GRAPH <"+ Config.getClassifFamiliesGraph() + "> { \n"
			+ "?families skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.getLg1() + "') \n"
			+ "FILTER(REGEX(STR(?families),'/familleDeNomenclatures/')) . \n"
			+ "BIND(STRAFTER(STR(?families),'/codes/familleDeNomenclatures/') AS ?id) } \n"
			+ "} \n"
			+ "ORDER BY ?label ";	
	}
	
	public static String familyQuery(String id) {
		return "SELECT ?prefLabelLg1 \n"
			+ "WHERE { GRAPH<"+ Config.getClassifFamiliesGraph() + "> { \n"
			+ "?family skos:prefLabel ?prefLabelLg1 . \n"
			+ "FILTER (lang(?prefLabelLg1) = '" + Config.getLg1() + "') \n"
			+ "FILTER(REGEX(STR(?family),'/familleDeNomenclatures/" + id + "')) } \n"
			+ "} \n";	
	}
	
	public static String familyMembersQuery(String id) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 \n"
			+ "WHERE { \n"
			+ "?series xkos:belongsTo ?family . \n"
			+ "?series skos:prefLabel ?labelLg1 . \n"
			+ "FILTER (lang(?labelLg1) = '" + Config.getLg1() + "') \n"
			+ "OPTIONAL {?series skos:prefLabel ?labelLg2 . \n"
			+ "FILTER (lang(?labelLg2) = '" + Config.getLg2() + "') } \n"
			+ "FILTER(REGEX(STR(?family),'/familleDeNomenclatures/" + id + "')) . \n"
			+ "BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?labelLg1 ";	
	}
	
	  private FamiliesQueries() {
		    throw new IllegalStateException("Utility class");
	}

	
}