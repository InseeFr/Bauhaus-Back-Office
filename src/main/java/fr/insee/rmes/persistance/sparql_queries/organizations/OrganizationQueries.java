package fr.insee.rmes.persistance.sparql_queries.organizations;

import fr.insee.rmes.config.Config;

public class OrganizationQueries {

	public static String organizationQuery(String identifier) {
		return "SELECT  ?labelLg1 ?labelLg2 ?altLabel ?type ?motherOrganization ?linkedTo ?seeAlso \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/organisations> { \n"
				//id
				+ "?organization dcterms:identifier '"+ identifier +"' . \n"

				//labels
				+ "OPTIONAL { ?organization skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "')} \n"
				+ "OPTIONAL {?organization skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') }\n"
				+ "OPTIONAL {?organization skos:altLabel ?altLabel .} \n"

				//type (exclude org:Organization and org:OrganizationUnit)
				+ "OPTIONAL {?organization rdf:type ?type . \n"
				+ "FILTER (!strstarts(str(?type),str(org:))) } \n"

				//links
				+ "OPTIONAL {?organization org:unitOf ?motherOrganizationUri ."
				+ "?motherOrganizationUri  dcterms:identifier ?motherOrganization .} \n"
				+ "OPTIONAL {?organization org:linkedTo ?linkedToUri ."
				+ "?linkedToUri  dcterms:identifier ?linkedTo .} \n"

				//seeAlso
				+ "OPTIONAL {?organization rdfs:seeAlso ?seeAlso .} \n"

				+ "}} \n" ;
	}

	public static String organizationsQuery() {
		return "SELECT DISTINCT ?id ?label ?altLabel \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/organisations> { \n"
				//id
				+ "?organization dcterms:identifier ?id . \n"

				//labels
				+ "OPTIONAL { ?organization skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "')} \n"
				+ "OPTIONAL {?organization skos:altLabel ?altLabel .} \n"

				+ "}} \n" 
				+ "GROUP BY ?id ?label ?altLabel \n"
				+ "ORDER BY ?label ";
	}
	
	public static String getUriById(String identifier) {
		return "SELECT  ?uri \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/organisations> { \n"
				+ "?uri dcterms:identifier '"+ identifier +"' . \n"

				+ "}} \n" ;
	}
	


}
