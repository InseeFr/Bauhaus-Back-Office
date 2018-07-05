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
				+ "?idFirstClass ?firstClassLabelLg1 ?firstClassLabelLg2 ?firstAltLabelLg1 ?firstAltLabelLg2 \n"
				+ "?idSecondClass ?secondClassLabelLg1 ?secondClassLabelLg2 ?secondAltLabelLg1 ?secondAltLabelLg2 \n"
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
				+ "?firstClassURI skos:prefLabel ?firstClassLabelLg1 . \n"
				+ "FILTER (lang(?firstClassLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?firstClassURI skos:prefLabel ?firstClassLabelLg2 . \n"
				+ "FILTER (lang(?firstClassLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "OPTIONAL {?firstClassURI skos:altLabel ?firstAltLabelLg1 . \n"
				+ "FILTER (lang(?firstAltLabelLg1) = '" + Config.LG1 + "') } . \n"
				+ "OPTIONAL {?firstClassURI skos:altLabel ?firstAltLabelLg2 . \n"
				+ "FILTER (lang(?firstAltLabelLg2) = '" + Config.LG2 + "') } . \n"
				// Second classification
				+ "?correspondence xkos:compares ?secondClassURI . \n"
				+ "FILTER(REGEX(STR(?secondClassURI),'/codes/" + secondId + "/')) . \n"
				+ "BIND('" + secondId + "' as ?idSecondClass) . \n"
				+ "?secondClassURI skos:prefLabel ?secondClassLabelLg1 . \n"
				+ "FILTER (lang(?secondClassLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?secondClassURI skos:prefLabel ?secondClassLabelLg2 . \n"
				+ "FILTER (lang(?secondClassLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "OPTIONAL {?secondClassURI skos:altLabel ?secondAltLabelLg1 . \n"
				+ "FILTER (lang(?secondAltLabelLg1) = '" + Config.LG1 + "') } . \n"
				+ "OPTIONAL {?secondClassURI skos:altLabel ?secondAltLabelLg2 . \n"
				+ "FILTER (lang(?secondAltLabelLg2) = '" + Config.LG2 + "') } . \n"
				+ "}"
				+ "LIMIT 1";
	}
	
	public static String correspondenceAssociationsQuery(String correspondenceId) {
		return "SELECT ?id ?sourceLabelLg1 ?sourceLabelLg2 ?sourceId \n"
				+ "?targetLabelLg1 ?targetLabelLg2 ?targetId \n"
				+ "WHERE { \n"
				+ "?correspondence xkos:madeOf ?association . \n"
				+ "FILTER(REGEX(STR(?correspondence),'/codes/" + correspondenceId + "')) . \n"
				+ "BIND(STRAFTER(STR(?association),'/association/') AS ?id) . \n"
				+ "?association xkos:sourceConcept ?source . \n"
				+ "?source skos:notation ?sourceId . \n"
				+ "?source skos:prefLabel ?sourceLabelLg1 . \n"
				+ "FILTER (lang(?sourceLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?source skos:prefLabel ?sourceLabelLg2 . \n"
				+ "FILTER (lang(?sourceLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "?association xkos:targetConcept ?target . \n"
				+ "?target skos:notation ?targetId . \n"
				+ "?target skos:prefLabel ?targetLabelLg1 . \n"
				+ "FILTER (lang(?targetLabelLg1) = '" + Config.LG1 + "')  . \n"
				+ "OPTIONAL {?target skos:prefLabel ?targetLabelLg2 . \n"
				+ "FILTER (lang(?targetLabelLg2) = '" + Config.LG2 + "') }  . \n"
				+ "}";
	}

}
