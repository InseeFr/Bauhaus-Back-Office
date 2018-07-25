package fr.insee.rmes.persistance.service.sesame.operations.series;

import fr.insee.rmes.config.Config;

public class SeriesQueries {

	public static String seriesQuery() {
		return "SELECT DISTINCT ?id ?label ?altLabel \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/operations> { \n"
				+ "?series a insee:StatisticalOperationSeries . \n" 
				+ "?series skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . \n"
				+ "OPTIONAL{?series skos:altLabel ?altLabel . "
				+ "FILTER (lang(?altLabel) = '" + Config.LG1 + "') } \n "
				+ "}} \n" 
				+ "GROUP BY ?id ?label ?altLabel \n"
				+ "ORDER BY ?label ";
	}

	public static String oneSeriesQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 "
				+ "?typeCode ?typeList ?accrualPeriodicityCode ?accrualPeriodicityList "
				+ "?creator ?contributor "
				+ "?motherFamily ?motherFamilyLabelLg1 ?motherFamilyLabelLg2\n"
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

			+ "OPTIONAL {?series dcterms:creator ?uriCreator . \n"
			+ "?uriCreator dcterms:identifier  ?creator . \n"
			+ "}   \n"
			+ "OPTIONAL {?series dcterms:contributor ?uriContributor . \n"
			+ "?uriContributor dcterms:identifier  ?contributor . \n"
			+ "}   \n"

				+ "?motherFamily dcterms:hasPart ?series . \n"
				+ "?motherFamily skos:prefLabel ?motherFamilyLabelLg1 . \n"
				+ "FILTER (lang(?motherFamilyLabelLg1) = '" + Config.LG1 + "') . \n"
				+ "?motherFamily skos:prefLabel ?motherFamilyLabelLg2 . \n"
				+ "FILTER (lang(?motherFamilyLabelLg2) = '" + Config.LG2 + "') . \n"

				+ "} \n"
				+ "LIMIT 1";
	}


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


}
