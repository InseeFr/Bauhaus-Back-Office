package fr.insee.rmes.persistance.sparql_queries.operations.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

import java.util.HashMap;
import java.util.Map;

public class OperationsQueries {
	static Map<String,Object> params ;

	private static void initParams() {
		params = new HashMap<>();
		params.put("OPERATIONS_GRAPH",Config.OPERATIONS_GRAPH);
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}

	private static String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
	}

	public static String operationsQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
				+ "?operation a insee:StatisticalOperation . \n" 
				+ "?operation skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) . \n"
				+ "OPTIONAL{?operation skos:altLabel ?altLabelLg1 . "
				+ "FILTER (lang(?altLabelLg1) = '" + Config.LG1 + "')}\n" 
				+ "}} \n" 
				+ "GROUP BY ?id ?label \n"
				+ "ORDER BY ?label ";
	}

	public static String operationQuery(String id){
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 ?idSims ?validationState ?created ?modified \n"
				+ "WHERE { "
					+ "GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
					+ "?operation skos:prefLabel ?prefLabelLg1 . \n" 
					+ "FILTER(STRENDS(STR(?operation),'/operations/operation/" + id+ "')) . \n" 
					+ "BIND(STRAFTER(STR(?operation),'/operation/') AS ?id) . \n" 
	
					+ "FILTER (lang(?prefLabelLg1) = '"	+ Config.LG1 + "') . \n" 
					+ "OPTIONAL {?operation skos:prefLabel ?prefLabelLg2 . \n"
					+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n" 
	
					+ "OPTIONAL {?operation skos:altLabel ?altLabelLg1 . \n"
					+ "FILTER (lang(?altLabelLg1) = '" + Config.LG1 + "') } . \n" 
					+ "OPTIONAL {?operation skos:altLabel ?altLabelLg2 . \n"
					+ "FILTER (lang(?altLabelLg2) = '" + Config.LG2 + "') } . \n" 
					+ "}"
					+ "OPTIONAL { ?operation dcterms:created ?created } . \n"
					+ "OPTIONAL { ?operation dcterms:modified ?modified } . \n"
					+ "OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport ."
					+ " ?report sdmx-mm:target ?operation "
					+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n"
					+ "} \n"
					
					+ "OPTIONAL {?operation insee:validationState ?validationState . \n"
					+ "} \n"
				+ "} \n"
		+ "LIMIT 1";
	}
	
	public static String seriesQuery(String idOperation) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ "WHERE { GRAPH <"+Config.OPERATIONS_GRAPH+"> { \n"
		+ "FILTER(STRENDS(STR(?operation),'/operations/operation/" + idOperation+ "')) . \n" 

		+ "?seriesUri dcterms:hasPart ?operation . \n"
		+ "?seriesUri skos:prefLabel ?labelLg1 . \n"
		+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
		+ "?seriesUri skos:prefLabel ?labelLg2 . \n"
		+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
		+ "BIND(STRAFTER(STR(?seriesUri),'/serie/') AS ?id) . \n" 


		+ "}} \n"
		+ "LIMIT 1";
	}

	public static String operationsWithoutSimsQuery(String idSeries) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 \n"
				+ "WHERE {  \n"
				+ "?operation a insee:StatisticalOperation . \n" 
				+ "?series dcterms:hasPart ?operation \n "
				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries+ "')) . \n" 
				
				+ "?operation skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "?operation skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') \n"
					
				+ "BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) . \n"
				+ "FILTER NOT EXISTS { ?documentation sdmx-mm:target ?operation }"
				
				+ "} \n" 
				+ "GROUP BY ?id ?labelLg1 ?labelLg2 \n"
				+ "ORDER BY ?labelLg1 ";
	}

	public static String operationsWithSimsQuery(String idSeries) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?idSims \n"
				+ "WHERE {  \n"
				+ "?operation a insee:StatisticalOperation . \n"
				+ "?series dcterms:hasPart ?operation \n "
				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries+ "')) . \n"

				+ "?operation skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "?operation skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') \n"

				+ "BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) . \n"
				+ "?report rdf:type sdmx-mm:MetadataReport ."
				+ " ?report sdmx-mm:target ?operation "
				+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n"
				+ "} \n"
				+ "GROUP BY ?id ?labelLg1 ?labelLg2 ?idSims \n"
				+ "ORDER BY ?labelLg1 ";
	}
	
	  private OperationsQueries() {
		    throw new IllegalStateException("Utility class");
	}

	public static String seriesWithSimsQuery(String idFamily) throws RmesException {
		if (params==null) {initParams();}
		params.put("ID_FAMILY", idFamily);
		return buildIndicatorRequest("getSeriesWithSimsQuery.ftlh", params);
	}
}
