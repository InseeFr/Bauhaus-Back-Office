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
		String[] classificationsIds = id.split("-");
		String firstId = classificationsIds[0];
		String secondId = classificationsIds[1];
		return "SELECT ?id ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 \n"
				+ "?firstClasslabelLg1 ?firstClasslabelLg2 ?idFirstClass \n"
				+ "?secondClasslabelLg1 ?secondClasslabelLg2 ?idSecondClass \n"
				+ "WHERE { \n"
				+ "?correspondence rdf:type xkos:Correspondence . \n"
				+ "FILTER(REGEX(STR(?correspondence),'/codes/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id) \n"
				+ "?correspondence skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "OPTIONAL {?correspondence skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') } . \n"
				+ "OPTIONAL {?correspondence skos:description ?descriptionLg1 . \n"
				+ "FILTER (lang(?descriptionLg1) = '" + Config.LG1 + "') } . \n"
				+ "OPTIONAL {?correspondence dc:description ?descriptionLg2 . \n"
				+ "FILTER (lang(?descriptionLg2) = '" + Config.LG2 + "') } . \n"
				// First classification
				+ "?correspondence xkos:compares ?firstClassURI . \n"
				+ "FILTER(REGEX(STR(?firstClassURI),'/codes/" + firstId + "/')) . \n"
				+ "BIND('" + firstId + "' as ?idFirstClass) . \n"
				+ "?firstClassURI skos:prefLabel ?firstClasslabelLg1 . \n"
				+ "FILTER (lang(?firstClasslabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?firstClassURI skos:prefLabel ?firstClasslabelLg2 . \n"
				+ "FILTER (lang(?firstClasslabelLg2) = '" + Config.LG2 + "') }  . \n"
				// Second classification
				+ "?correspondence xkos:compares ?secondClassURI . \n"
				+ "FILTER(REGEX(STR(?secondClassURI),'/codes/" + secondId + "/')) . \n"
				+ "BIND('" + secondId + "' as ?idSecondClass) . \n"
				+ "?secondClassURI skos:prefLabel ?secondClasslabelLg1 . \n"
				+ "FILTER (lang(?secondClasslabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?secondClassURI skos:prefLabel ?secondClasslabelLg2 . \n"
				+ "FILTER (lang(?secondClasslabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "}";
	}
	
	public static String correspondenceAssociationsQuery(String correspondenceId) {
		return "SELECT ?id ?sourceLabelLg1 ?sourceLabelLg2 ?sourceId \n"
				+ "?targetLabelLg1 ?targetLabelLg2 ?targetId \n"
				+ "WHERE { \n"
				+ "?correspondence xkos:madeOf ?association . \n"
				+ "FILTER(REGEX(STR(?correspondence),'/codes/" + correspondenceId + "')) . \n"
				+ "BIND(STRAFTER(STR(?correspondence),'/association/') AS ?id) . \n"
				+ "?association xkos:sourceConcept ?source . \n"
				+ "?source skos:prefLabel ?sourceLabelLg1 . \n"
				+ "FILTER (lang(?sourceLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?source skos:prefLabel ?sourceLabelLg2 . \n"
				+ "FILTER (lang(?sourceLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "?association xkos:targetConcept ?target . \n"
				+ "?target skos:prefLabel ?targetLabelLg1 . \n"
				+ "FILTER (lang(?targetLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?target skos:prefLabel ?targetLabelLg2 . \n"
				+ "FILTER (lang(?targetLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "}";
	}

}
