package fr.insee.rmes.persistance.sparqlQueries.concepts;

import fr.insee.rmes.config.Config;

public class CollectionsQueries {
	
	public static String collectionsQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { \n"
			+ "?collection rdf:type skos:Collection . \n"
			+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
			+ "?collection dcterms:title ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') } \n"
			+ "ORDER BY ?label ";	
	}
	
	public static String collectionsDashboardQuery() {
		return "SELECT ?id ?label ?created ?modified ?isValidated ?creator \n"
			+ "(count(?member) AS ?nbMembers) \n"
			+ "WHERE { \n"
			+ "?collection rdf:type skos:Collection . \n"
			+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
			+ "?collection dcterms:title ?label . \n"
			+ "?collection dcterms:created ?created . \n"
			+ "OPTIONAL {?collection dcterms:modified ?modified} . \n"
			+ "?collection insee:isValidated ?isValidated \n"
			+ "OPTIONAL {?collection dc:creator ?creator} . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "')"
			+ "?collection skos:member ?member . \n"
			+ " } \n"
			+ "GROUP BY ?id ?label ?created ?modified ?isValidated ?creator \n"
			+ "ORDER BY ?label";	
	}
	
	public static String collectionsToValidateQuery() {
		return "SELECT DISTINCT ?id ?label ?creator \n"
			+ "WHERE { \n"
			+ "?collection rdf:type skos:Collection . \n"
			+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
			+ "?collection dcterms:title ?label . \n"
			+ "?collection dc:creator ?creator . \n"
			+ "?collection insee:isValidated 'false'^^xsd:boolean . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') } \n"
			+ "ORDER BY ?label ";	
	}
	
	public static String collectionQuery(String id) { 
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?created ?modified ?descriptionLg1 ?descriptionLg2 \n"
				+ "?isValidated ?creator ?contributor \n"
				+ "WHERE { \n"
				+ "?collection rdf:type skos:Collection . \n"
				+ "FILTER(STRENDS(STR(?collection),'/concepts/definitions/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) . \n"
				+ "?collection dcterms:title ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?collection dcterms:title ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "')} . \n"
				+ "?collection dcterms:created ?created . \n"
				+ "OPTIONAL {?collection dcterms:modified ?modified} . \n"
				+ "OPTIONAL {?collection dcterms:description ?descriptionLg1 ."
				+ "FILTER (lang(?descriptionLg1) = '" + Config.LG1 + "') } \n"
				+ "OPTIONAL {?collection dcterms:description ?descriptionLg2 ."
				+ "FILTER (lang(?descriptionLg2) = '" + Config.LG2 + "') } . \n"
				+ "?collection insee:isValidated ?isValidated \n"
				+ "OPTIONAL {?collection dc:creator ?creator} . \n"
				+ "?collection dc:contributor ?contributor . \n"
				+ "} \n";
	}
	
	public static String collectionMembersQuery(String id) { 
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 \n"
				+ "WHERE { \n"
				+ "?collection rdf:type skos:Collection . \n"
				+ "FILTER(STRENDS(STR(?collection),'/concepts/definitions/" + id + "')) . \n"
				+ "?collection skos:member ?member . \n"
				+ "?member skos:notation ?id . \n"
				+ "?member skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?member skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "')} \n"
				+ "}";
	}
	
	public static String isCollectionExist(String id) {
		return "ASK { \n"
				+ "?collection ?b ?c . \n"
				+ "FILTER(STRENDS(STR(?collection),'/concepts/definitions/" + id + "')) . \n"
				+ "}";
	}

}
