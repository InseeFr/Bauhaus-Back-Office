package fr.insee.rmes.persistance.sparql_queries.structures;

import fr.insee.rmes.bauhaus_services.rdfUtils.RdfUtils;
import fr.insee.rmes.config.Config;

public class DSDQueries {

	public static String getDSDs() {
		return "SELECT ?id ?label \n"
				+ "WHERE { GRAPH <" + RdfUtils.dsdGraph() + "> { \n"
				+ "?dsd a qb:DataStructureDefinition . \n"
				+ "?dsd dcterms:identifier ?id . \n"
				+ "?dsd rdfs:label ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "}}";
	}
	
	public static String getDSDById(String dsdId) {
		return "SELECT ?id ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 \n"
				+ "WHERE { GRAPH <" + RdfUtils.dsdGraph() + "> { \n"
				+ "?dsd a qb:DataStructureDefinition . \n"
				+ "?dsd dcterms:identifier '" + dsdId + "' . \n"
				+ "BIND('" + dsdId + "' as ?id) . \n"
				+ "?dsd rdfs:label ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "OPTIONAL{?dsd rdfs:label ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} \n"
				+ "OPTIONAL{?dsd dc:description ?descriptionLg1 . \n"
				+ "FILTER (lang(?descriptionLg1) = '" + Config.LG1 + "')} \n"
				+ "OPTIONAL{?dsd dc:description ?descriptionLg2 . \n"
				+ "FILTER (lang(?descriptionLg2) = '" + Config.LG2 + "')} \n"
				+ "}}";
	}
	
	public static String getDSDComponents(String dsdId) {
		return "SELECT ?id ?label ?type \n"
				+ "WHERE { GRAPH <" + RdfUtils.dsdGraph() + "> { \n"
				+ "?dsd dcterms:identifier '" + dsdId + "' . \n"
				+ "?dsd qb:component ?node . \n"
				+ "?node ?type ?component . \n"
				+ "?component dcterms:identifier ?id . \n"
				+ "?component rdfs:label ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "}}";
	}
	
	public static String getDSDDetailedComponents(String dsdId) {
		return "SELECT ?id ?type ?labelLg1 ?labelLg2 ?concept ?attachment ?range ?codeList ?isCoded \n"
				+ "WHERE { \n"
				// Maybe unnecessary but ensures uniqueness
				+ "?dsd dcterms:identifier '" + dsdId + "' . \n"
				+ "?dsd qb:component ?node . \n"
				+ "?node ?type ?component . \n"
				+ "?component dcterms:identifier ?id . \n"
				// attachment
				+ "OPTIONAL{?node qb:componentAttachment ?attachment} . \n"
				// label
				+ "OPTIONAL{?component rdfs:label ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "')} \n"
				+ "OPTIONAL{?component rdfs:label ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} \n"
				// concept
				+ "OPTIONAL{?component qb:concept ?concept} . \n"
				// range
				+ "OPTIONAL{?component rdfs:range ?range} . \n"
				// codeList
				+ "OPTIONAL{?component qb:codeList ?codeList} \n"
				+ "}";
	}
	
	public static String getDSDComponentById(String dsdId, String componentId) {
		return "SELECT ?id ?type ?labelLg1 ?labelLg2 ?concept ?attachment ?range ?codeList ?isCoded  \n"
				+ "WHERE { \n"
				// Maybe unnecessary but ensures uniqueness
				+ "?dsd dcterms:identifier '" + dsdId + "' . \n"
				+ "?dsd qb:component ?node . \n"
				+ "?node ?type ?component . \n"
				+ "?component dcterms:identifier '" + componentId + "' . \n"
				+ "BIND('" + componentId + "' as ?id) . \n"
				// attachment
				+ "OPTIONAL{?node qb:componentAttachment ?attachment} . \n"
				// label
				+ "OPTIONAL{?component rdfs:label ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "')} \n"
				+ "OPTIONAL{?component rdfs:label ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} \n"
				// concept
				+ "OPTIONAL{?component qb:concept ?concept} . \n"
				// range
				+ "OPTIONAL{?component rdfs:range ?range} . \n"
				// codeList
				+ "OPTIONAL{?component qb:codeList ?codeList} \n"
				+ "}";
	}

}