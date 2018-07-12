package fr.insee.rmes.persistance.service.sesame.operations.series;

import fr.insee.rmes.config.Config;

public class SeriesQueries {

	public static String seriesQuery() {
		return "SELECT DISTINCT ?id ?label (group_concat(?altLab;separator=' || ') as ?altLabel) \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" 
				+ "?series skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . \n"
				+ "OPTIONAL{?series skos:altLabel ?altLab . } \n" 
				+ "}} \n" 
				+ "GROUP BY ?id ?label \n"
				+ "ORDER BY ?label ";
	}

	public static String oneSeriesQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 ?typeCode ?typeList ?accrualPeriodicityCode ?accrualPeriodicityList ?motherFamily ?motherFamilyLabelLg1 ?motherFamilyLabelLg2\n"
				+ "WHERE {  \n"
				+ "?series skos:prefLabel ?prefLabelLg1 . \n" 
				+ "FILTER(REGEX(STR(?series),'/operations/serie/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?series),'/serie/') AS ?id) . \n" 

				+ "FILTER (lang(?prefLabelLg1) = '"	+ Config.LG1 + "') . \n" 
				+ "OPTIONAL {?series skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n" 

				+ "OPTIONAL {?series dcterms:abstract ?abstractLg1 . \n"
				+ "FILTER (lang(?abstractLg1) = '" + Config.LG1 + "') } . \n" 
				+ "OPTIONAL {?series dcterms:abstract ?abstractLg2 . \n"
				+ "FILTER (lang(?abstractLg2) = '" + Config.LG2 + "') } . \n" 

				+ "OPTIONAL {?series dcterms:type ?type . \n"
				+ "?type skos:notation ?typeCode . \n"
				+ "?type skos:inScheme ?typeCodeList . \n"
				+ "?typeCodeList skos:notation ?typeList . \n"
				+ "}   \n"

				+ "OPTIONAL {?series dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n"
				+ "}   \n"

				+ "?motherFamily dcterms:hasPart ?series . \n"
				+ "?motherFamily skos:prefLabel ?motherFamilyLabelLg1 . \n"
				+ "FILTER (lang(?motherFamilyLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "?motherFamily skos:prefLabel ?motherFamilyLabelLg2 . \n"
				+ "FILTER (lang(?motherFamilyLabelLg2) = '" + Config.LG2 + "') . \n"

				+ "} \n"
				+ "LIMIT 1";
	}


	public static String altLabel(String id) {
		return "SELECT ?altLabel \n" + "WHERE { \n" 
				+ "?series skos:altLabel ?altLabel \n"
				+ "FILTER(REGEX(STR(?series),'/operations/serie/" + id + "')) . \n"
				+ "}";
	}

	public static String seriesNotesQuery(String idSeries) { 
		return 
				"SELECT ?historyNoteLg1 ?historyNoteLg2  \n"
				+ "WHERE { \n" 
				//historyNote Lg1
				+ "OPTIONAL {?series skos:historyNote ?hNLg1 . \n"
				+ "?historyNoteLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
				+ "?hNLg1 evoc:noteLiteral ?historyNoteLg1 . \n"	
				+ "} .  \n"
				//historyNote Lg2
				+ "OPTIONAL {?series skos:historyNote ?hNLg2 . \n"
				+ "?historyNoteLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
				+ "?hNLg1 evoc:noteLiteral ?historyNoteLg2 . \n"	
				+ "} .  \n"
				+ "FILTER(REGEX(STR(?series),'/operations/serie/" + idSeries + "')) . \n"
				+"} \n";
	}

	public static String seriesLinks(String id) {
		return "SELECT ?uriLinked ?typeOfLink ?prefLabelLg1 ?prefLabelLg2 \n"
				+ "WHERE { \n" 

				+ "{?series dcterms:replaces ?uriLinked . \n"
				+ "BIND('replaces' AS ?typeOfLink)} \n"
				+ "UNION"
				+ "{?series dcterms:isReplacedBy ?uriLinked . \n"
				+ "BIND('isReplacedBy' AS ?typeOfLink)} \n"
				+ "UNION"
				+ "{?series rdfs:seeAlso ?uriLinked . \n"
				+ "BIND('seeAlso' AS ?typeOfLink)} \n"

				+ "?uriLinked skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uriLinked skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "')} . \n"

			+ "FILTER(REGEX(STR(?series),'/operations/serie/" + id + "')) . \n"


				+ "} \n"
				+ "ORDER BY ?typeOfLink";
	}


	public static String getOperations(String idSeries) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <http://rdf.insee.fr/graphes/operations> \n"
				+ "WHERE { \n" 

				+ "?series dcterms:hasPart ?id . \n"
				+ "?id skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?id skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"

				+ "FILTER(REGEX(STR(?series),'/operations/serie/" + idSeries + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
	}



	//TODO organizations


	/*
	 *   PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
  PREFIX dcterms: <http://purl.org/dc/terms/>
  SELECT ?organization ?label ?typeOfLink
  FROM <http://rdf.insee.fr/graphes/operations>
  FROM <http://rdf.insee.fr/graphes/organisations>
  WHERE {
    {<http://id.insee.fr/operations/serie/s1022> dcterms:creator ?organization .
    ?organization skos:prefLabel ?label .
    FILTER (lang(?label) = 'fr')
    BIND('creator' AS ?typeOfLink)}
    UNION
    {<http://id.insee.fr/operations/serie/s1022> dcterms:contributor ?organization .
    ?organization skos:prefLabel ?label .
    FILTER (lang(?label) = 'fr')
    BIND('contributor' AS ?typeOfLink)}
  }
  ORDER BY ?organization
	 */

}
