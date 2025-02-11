package fr.insee.rmes.persistance.sparql_queries.organizations;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;

public class OrganizationQueries extends GenericQueries{

	public static String organizationQuery(String identifier) {
		return "SELECT  ?labelLg1 ?labelLg2 ?altLabel ?type ?motherOrganization ?linkedTo ?seeAlso \n"
				+ "FROM <"+config.getOrganizationsGraph()+"> \n "
				+ "FROM <"+config.getOrgInseeGraph()+"> \n "

				+ "WHERE { \n"
				//id
				+ "?organization dcterms:identifier '"+ identifier +"' . \n"

				//labels
				+ "OPTIONAL { ?organization skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "')} \n"
				+ "OPTIONAL {?organization skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') }\n"
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

				+ "} \n" ;
	}

	public static String organizationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
		params.put("ORGANIZATIONS_INSEE_GRAPH", config.getOrgInseeGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest("organizations/", "getOrganizations.ftlh", params);
	}

	public static String organizationsTwoLangsQuery() {
		return "SELECT DISTINCT ?id ?labelLg1  ?labelLg2  ?altLabel \n"
				+ "FROM <"+config.getOrganizationsGraph()+"> \n "
				+ "FROM <"+config.getOrgInseeGraph()+"> \n "

				+ "WHERE { \n"
				//id
				+ "?organization dcterms:identifier ?id . \n"

				//labels
				+ "OPTIONAL { ?organization skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "')} \n"
				+ "OPTIONAL { ?organization skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "')} \n"
				+ "OPTIONAL {?organization skos:altLabel ?altLabel .} \n"

				+ "} \n" 
				+ "GROUP BY ?id ?labelLg1 ?labelLg2 ?altLabel \n"
				+ "ORDER BY ?labelLg1 ";
	}

	public static String getUriById(String identifier) {
		return "SELECT  ?uri \n"
				+ "FROM <"+config.getOrganizationsGraph()+"> \n "
				+ "FROM <"+config.getOrgInseeGraph()+"> \n "

				+ "WHERE { \n"
				+ "?uri dcterms:identifier '"+ identifier +"' . \n"

				+ "} \n" ;
	}

	  private OrganizationQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
