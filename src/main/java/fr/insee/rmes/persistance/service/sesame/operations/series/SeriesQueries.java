package fr.insee.rmes.persistance.service.sesame.operations.series;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.FreeMarkerUtils;

public class SeriesQueries {

	private static StringBuilder variables;
	private static StringBuilder whereClause;
	static Map<String,Object> params ;

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
		variables=null;
		whereClause=null;
		getSimpleAttr(id);
		getCodesLists();
		getSingleOrganizations();

		return "SELECT "
		+ variables.toString()
		+ " WHERE {  \n"
		+ whereClause.toString()
		+ "} \n"
		+ "LIMIT 1";
	}

	public static String seriesLinks(String idSeries, URI linkPredicate) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n"
				+ "WHERE { \n" 
				+ "?series <"+linkPredicate+"> ?uriLinked . \n"
				+ "?uriLinked skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n"
				+ "?uriLinked rdf:type ?typeOfObject . \n"
				+ "BIND(REPLACE( STR(?uriLinked) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?labelLg1";
	}



	public static String getOperations(String idSeries) {
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
				+ " FROM <http://rdf.insee.fr/graphes/operations> \n"
				+ "WHERE { \n" 

				+ "?series dcterms:hasPart ?uri . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ "BIND(STRAFTER(STR(?uri),'/operations/operation/') AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
	}

	public static String getGeneratedWith(String idSeries) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n"
				+ "WHERE { \n" 

				+ "?uri prov:wasGeneratedBy ?series . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
				+ "?uri rdf:type ?typeOfObject . \n"
				
				+ "BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"

				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"
				+ "}"
				+ " ORDER BY ?id"
				;
	}




	private static void getSimpleAttr(String id) {
		
		addClauseToWhereClause(" FILTER(STRENDS(STR(?series),'/operations/serie/" + id+ "')) . \n" );
		
		addVariableToList(" ?prefLabelLg1 ?prefLabelLg2 ");
		addClauseToWhereClause( "?series skos:prefLabel ?prefLabelLg1 \n");
		addClauseToWhereClause( "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "')  \n ");
		addClauseToWhereClause( "OPTIONAL{?series skos:prefLabel ?prefLabelLg2 \n");
		addClauseToWhereClause( "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } \n ");
		
		addVariableToList(" ?altLabelLg1 ?altLabelLg2 ");
		addOptionalClause("skos:altLabel", "?altLabel");

		addVariableToList(" ?abstractLg1 ?abstractLg2 ");
		addOptionalClause("dcterms:abstract", "?abstract");

		addVariableToList(" ?historyNoteLg1 ?historyNoteLg2 ");
		addOptionalClause("skos:historyNote", "?historyNote");



	}

	private static void addOptionalClause(String predicate, String variableName){
		addClauseToWhereClause( "OPTIONAL{?series "+predicate+" "+variableName + "Lg1 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg1) = '" + Config.LG1 + "') } \n ");
		addClauseToWhereClause( "OPTIONAL{?series "+predicate+" "+variableName + "Lg2 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg2) = '" + Config.LG2 + "') } \n ");
	}

	private static void getCodesLists() {
		addVariableToList(" ?typeCode ?typeList ");
		addClauseToWhereClause( "OPTIONAL {?series dcterms:type ?type . \n"
				+ "?type skos:notation ?typeCode . \n"
				+ "?type skos:inScheme ?typeCodeList . \n"
				+ "?typeCodeList skos:notation ?typeList . \n"
				+ "}   \n" );

		addVariableToList(" ?accrualPeriodicityCode ?accrualPeriodicityList ");
		addClauseToWhereClause( "OPTIONAL {?series dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n"
				+ "}   \n" );
	}

	private static void getSingleOrganizations() {
		addVariableToList(" ?creator ");
		addClauseToWhereClause(
				"OPTIONAL {?series dcterms:creator ?uriCreator . \n"
						+ "?uriCreator dcterms:identifier  ?creator . \n"
						+ "}   \n");
		addVariableToList(" ?gestionnaire  ");
		addClauseToWhereClause(  
				"OPTIONAL {?series insee:gestionnaire ?uriGestionnaire . \n"
						+ "?uriGestionnaire dcterms:identifier  ?gestionnaire . \n"
						+ "}   \n");
	}
	
	public static String getMultipleOrganizations(String idSeries, URI linkPredicate) {
		return "SELECT ?id ?labelLg1 ?labelLg2\n"
				+ "WHERE { \n" 
				+"?series <"+linkPredicate+"> ?uri . \n"
				+ "?uri dcterms:identifier  ?id . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n"
				
				+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?id";
	}
	

	public static String getFamily(String idSeries) {
	
		return "SELECT ?id ?labelLg1 ?labelLg2 \n"
		+ " FROM <http://rdf.insee.fr/graphes/operations> \n"
		+ "WHERE { \n" 

		+ "?family dcterms:hasPart ?series . \n"
		+ "?family skos:prefLabel ?labelLg1 . \n"
		+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
		+ "?family skos:prefLabel ?labelLg2 . \n"
		+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') . \n"
		+ "BIND(STRAFTER(STR(?family),'/famille/') AS ?id) . \n" 

		+ "FILTER(STRENDS(STR(?series),'/operations/serie/" + idSeries + "')) . \n"
		+ "}"
		
		;
	}


	private static void addVariableToList(String variable) {
		if (variables == null){
			variables = new StringBuilder();
		}
		variables.append(variable);
	}

	private static void addClauseToWhereClause(String clause) {
		if (whereClause == null){
			whereClause = new StringBuilder();
		}
		whereClause.append(clause);
	}

	/**
	 * @param uriSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String checkIfSeriesExists(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put("uriSeries", uriSeries);
		return buildSeriesRequest("checkIfSeriesExistsQuery.flth", params);	
	}
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}
	
	private static String buildSeriesRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
	}



}
