package fr.insee.rmes.persistance.sparql_queries.operations.families;

import java.util.Map;

import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

public class OpFamiliesQueries extends GenericQueries{

	static Map<String,Object> params ;

	public static String familiesSearchQuery() {

		return "SELECT DISTINCT ?id ?prefLabelLg1 ?prefLabelLg2 (group_concat(?abstractL1;separator=' || ') as ?abstractLg1) ?abstractLg2 \n"
				+ "WHERE { \n"
				+ "   GRAPH <"+config.getOperationsGraph()+"> { \n"				
				+ "?family a insee:StatisticalOperationFamily . \n"
				+ "?family skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + config.getLg1() + "') \n"
				+ "        OPTIONAL {?family skos:prefLabel ?prefLabelLg2 .\n"
				+ "FILTER (lang(?prefLabelLg2) = '" + config.getLg2() + "') } . \n"
				+ "        OPTIONAL {?family dcterms:abstract ?abstractL1 .\n"
				+ "FILTER (lang(?abstractL1) = '" + config.getLg1() + "') } .\n"
				+ "OPTIONAL {?family dcterms:abstract ?abstractLg2 .\n"
				+ "FILTER (lang(?abstractLg2) = '" + config.getLg2() + "') } .  \n"
				+ "BIND(STRAFTER(STR(?family),'/operations/famille/') AS ?id) . \n"
				+ "} \n"
				+ "} \n"
				+ "GROUP BY ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractL1 ?abstractLg2 \n"
				+ "ORDER BY ?prefLabelLg1 ";
	}



	public static String familiesQuery() {
		return "SELECT DISTINCT ?id ?label  \n"
				+ "WHERE { GRAPH <"+config.getOperationsGraph()+"> { \n"
				+ "?family a insee:StatisticalOperationFamily . \n" 
				+ "?family skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + config.getLg1() + "') \n"
				+ "BIND(STRAFTER(STR(?family),'/operations/famille/') AS ?id) . \n"
				+ "}} \n" 
				+ "GROUP BY ?id ?label \n"
				+ "ORDER BY ?label ";
	}


	public static String familyQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 ?validationState ?created ?modified\n"
				+ "WHERE { GRAPH <"+config.getOperationsGraph()+"> { \n"
				+ "?family skos:prefLabel ?prefLabelLg1 . \n" 
				+ "FILTER(STRENDS(STR(?family),'/operations/famille/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?family),'/famille/') AS ?id) . \n" 

				+ "FILTER (lang(?prefLabelLg1) = '"	+ config.getLg1() + "') . \n" 
				+ "OPTIONAL {?family skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + config.getLg2() + "') } . \n" 
				+ "OPTIONAL {?family insee:validationState ?validationState} . \n"
				+ "OPTIONAL { ?family dcterms:created ?created } . \n"
				+ "OPTIONAL { ?family dcterms:modified ?modified } . \n"
				+ "OPTIONAL {?family dcterms:abstract ?abstractLg1 . \n"
				+ "FILTER (lang(?abstractLg1) = '" + config.getLg1() + "') } . \n" 
				+ "OPTIONAL {?family dcterms:abstract ?abstractLg2 . \n"
				+ "FILTER (lang(?abstractLg2) = '" + config.getLg2() + "') } . \n" 

				+ "}} \n"
				+ "LIMIT 1";
	}

	public static String getSeries(String idFamily) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <"+config.getOperationsGraph()+"> \n"
				+ "WHERE { \n" 

				+ "?family dcterms:hasPart ?uri . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') . \n"
				+ "BIND(STRAFTER(STR(?uri),'/operations/serie/') AS ?id) . \n"


				+ "FILTER(STRENDS(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
	}

	public static String getSubjects(String idFamily) {
		return "SELECT  ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <"+config.getOperationsGraph()+"> \n"
				+ "WHERE { \n" 

				+ "?family dcterms:subject ?subjectUri . \n"
				+ "?subjectUri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "?subjectUri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') . \n"
				
				+ "?subjectUri skos:notation ?id . \n"

				+ "FILTER(STRENDS(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
				+ "}"
				+ " ORDER BY ?subjectUri"
				;
	}
	
	  private OpFamiliesQueries() {
		    throw new IllegalStateException("Utility class");
	}


}