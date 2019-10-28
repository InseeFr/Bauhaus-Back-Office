package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.FreeMarkerUtils;

public class IndicatorsQueries {

	static Map<String,Object> params ;

	private static StringBuilder variables;
	private static StringBuilder whereClause;

	/*
	 * Requests from .flth files
	 */
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", Config.LG1);
		params.put("LG2", Config.LG2);
	}
	
	private static String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/indicators/", fileName, params);
	}
	
	public static String getPublicationState(String id) throws RmesException{
		if (params==null) {initParams();}
		params.put("id", id);
		return buildIndicatorRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	/*
	 * Requests wirtten in strings
	 */
	
	public static String indicatorsQuery() {
		return "SELECT DISTINCT ?id ?label ?altLabel \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/produits> { \n"
				+ "?indic a insee:StatisticalIndicator . \n" 
				+ "?indic skos:prefLabel ?label . \n"
				+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
				+ "BIND(STRAFTER(STR(?indic),'/produits/indicateur/') AS ?id) . \n"
				+ "OPTIONAL{?indic skos:altLabel ?altLabel . "
				+ "FILTER (lang(?altLabel) = '" + Config.LG1 + "') } \n "
				+ "}} \n" 
				+ "GROUP BY ?id ?label ?altLabel \n"
				+ "ORDER BY ?label ";
	}


	public static String indicatorsQueryForSearch() {
		return indicatorFullObjectQuery(null, false);
	}


	public static String indicatorQuery(String id) {
		return indicatorFullObjectQuery(id, true);
	}

	private static String indicatorFullObjectQuery(String id, boolean withLimit){
		variables=null;
		whereClause=null;
		getSimpleAttr(id);
		getCodesLists();
		getOrganizations();
		getSimsId();
		getValidationState();

		String query =  "SELECT "
				+ variables.toString()
				+ " WHERE {  \n"
				+ whereClause.toString()
				+ "} \n"
				+ (withLimit ? "LIMIT 1" : "");

		return query;
	}

	public static String indicatorLinks(String id, URI linkPredicate) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n"
				+ "WHERE { \n" 
				+ "?indic <"+linkPredicate+"> ?uriLinked . \n"
				+ "?uriLinked skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n"
				+ "?uriLinked rdf:type ?typeOfObject . \n"
				+ "BIND(REPLACE( STR(?uriLinked) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"
				
				+ "FILTER(STRENDS(STR(?indic),'/produits/indicateur/" + id + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?labelLg1";
	}
	
	public static String getMultipleOrganizations(String idIndicator, URI linkPredicate) {
		return "SELECT ?id ?labelLg1 ?labelLg2\n"
				+ "WHERE { \n" 
				+"?indicator <"+linkPredicate+"> ?uri . \n"
				+ "?uri dcterms:identifier  ?id . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n"
				
				+ "FILTER(STRENDS(STR(?indicator),'/produits/indicateur/" + idIndicator + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?id";
	}

	private static void getSimpleAttr(String id) {
		if(id != null) addClauseToWhereClause(" FILTER(STRENDS(STR(?indic),'/produits/indicateur/" + id+ "')) . \n" );
		else {
			addClauseToWhereClause("?indic a insee:StatisticalIndicator .");
			addClauseToWhereClause("BIND(STRAFTER(STR(?indic),'/produits/indicateur/') AS ?id) . ");
		}

		addVariableToList("?id ?prefLabelLg1 ?prefLabelLg2 ");
		addClauseToWhereClause( "?indic skos:prefLabel ?prefLabelLg1 \n");
		addClauseToWhereClause( "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') \n ");
		addClauseToWhereClause( "OPTIONAL{?indic skos:prefLabel ?prefLabelLg2 \n");
		addClauseToWhereClause( "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } \n ");
		


		addVariableToList(" ?altLabelLg1 ?altLabelLg2 ");
		addOptionalClause("skos:altLabel", "?altLabel");

		addVariableToList(" ?abstractLg1 ?abstractLg2 ");
		addOptionalClause("dcterms:abstract", "?abstract");

		addVariableToList(" ?historyNoteLg1 ?historyNoteLg2 ");
		addOptionalClause("skos:historyNote", "?historyNote");



	}

	private static void addOptionalClause(String predicate, String variableName){
		addClauseToWhereClause( "OPTIONAL{?indic "+predicate+" "+variableName + "Lg1 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg1) = '" + Config.LG1 + "') } \n ");
		addClauseToWhereClause( "OPTIONAL{?indic "+predicate+" "+variableName + "Lg2 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg2) = '" + Config.LG2 + "') } \n ");
	}

	private static void getCodesLists() {
		addVariableToList(" ?accrualPeriodicityCode ?accrualPeriodicityList ");
		addClauseToWhereClause( "OPTIONAL {?indic dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n"
				+ "}   \n" );
	}

	private static void getOrganizations() {
		addVariableToList(" ?creator ?gestionnaire ");
		addClauseToWhereClause(
				"OPTIONAL {?indic dcterms:creator ?uriCreator . \n"
						+ "?uriCreator dcterms:identifier  ?creator . \n"
						+ "}   \n");
		addClauseToWhereClause(
				"OPTIONAL {?indic insee:gestionnaire ?uriGestionnaire . \n"
						+ "?uriGestionnaire dcterms:identifier  ?gestionnaire . \n"
						+ "}   \n");
	}
	
	private static void getSimsId() {
		addVariableToList(" ?idSims ");
		addClauseToWhereClause("OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport ."
				+ " ?report sdmx-mm:target ?indic "
				+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n"
				+ "} \n");
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
	
	private static void getValidationState() {
		addVariableToList(" ?validationState ");
		addClauseToWhereClause(
				"OPTIONAL {?indic insee:validationState ?validationState . \n"
						+ "}   \n");
	}
	
	public static String lastID() {
		return "SELECT ?id \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/produits> { \n"
				+ "?uri ?b ?c .\n "
				+ "BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"
				+ "BIND(SUBSTR( ?id , 2 ) AS ?intid) . \n"
				+ "FILTER regex(STR(?uri),'/produits/indicateur/') . \n"
				+ "}} \n"
				+ "ORDER BY DESC(xsd:integer(?intid)) \n"
				+ "LIMIT 1";
	}	

	public static String checkIfExists(String id) {
		return "ASK \n"
				+ "WHERE  \n"
				+ "{ graph <http://rdf.insee.fr/graphes/produits>    \n"
				+ "{?uri ?b ?c .\n "
				+ "FILTER(STRENDS(STR(?uri),'/produits/indicateur/" + id + "')) . }\n"
				+ "}";
		  	
	}

	

}
