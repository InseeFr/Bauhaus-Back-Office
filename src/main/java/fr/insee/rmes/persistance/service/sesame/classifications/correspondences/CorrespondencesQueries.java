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
	
	public static String correspondenceQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?descriptionLg1 ?descriptionLg2 \n"
				+ "?firstClassPrefLabelLg1 ?firstClassPrefLabelLg2 ?idFirstClass \n"
				+ "?secondClassPrefLabelLg1 ?secondClassPrefLabelLg2 ?idSecondClass \n"
				+ "WHERE { \n"
				+ "?correspondence rdf:type xkos:Correspondence . \n"
				+ "FILTER(REGEX(STR(?correspondence),'/codes/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id) \n"
				+ "?correspondence skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') \n"
				+ "OPTIONAL {?correspondence skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n"
				+ "OPTIONAL {?correspondence skos:description ?descriptionLg1 . \n"
				+ "FILTER (lang(?descriptionLg1) = '" + Config.LG1 + "') } . \n"
				+ "OPTIONAL {?correspondence dc:description ?descriptionLg2 . \n"
				+ "FILTER (lang(?descriptionLg2) = '" + Config.LG2 + "') } . \n"
				// Second classification
				+ "?correspondence xkos:compares ?secondClassURI . \n"
				+ "?secondClassURI skos:prefLabel ?secondClassPrefLabelLg1 . \n"
				+ "FILTER (lang(?secondClassPrefLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "BIND(STRBEFORE(STRAFTER(STR(?correspondence),'/codes/'), '-') AS ?idSecondClass) \n"
				+ "OPTIONAL {?secondClassURI skos:prefLabel ?secondClassPrefLabelLg2 . \n"
				+ "FILTER (lang(?secondClassPrefLabelLg2) = '" + Config.LG2 + "') }  . \n"
				// First classification
				+ "?correspondence xkos:compares ?firstClassURI . \n"
				+ "?firstClassURI skos:prefLabel ?firstClassPrefLabelLg1 . \n"
				+ "FILTER (lang(?firstClassPrefLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "BIND(STRBEFORE(STRAFTER(STR(?correspondence),'/codes/'), '-') AS ?idFirstClass) \n"
				+ "OPTIONAL {?firstClassURI skos:prefLabel ?firstClassPrefLabelLg2 . \n"
				+ "FILTER (lang(?firstClassPrefLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "}";
	}

}
