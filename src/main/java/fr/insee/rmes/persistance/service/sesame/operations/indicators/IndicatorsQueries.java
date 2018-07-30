package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import fr.insee.rmes.config.Config;

public class IndicatorsQueries {


	private static StringBuilder variables;
	private static StringBuilder whereClause;

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


	public static String indicatorQuery(String id) {
		variables=null;
		whereClause=null;
		getSimpleAttr(id);
		getCodesLists();
		getOrganizations();

		return "SELECT "
		+ variables.toString()
		+ " WHERE {  \n"
		+ whereClause.toString()
		+ "} \n"
		+ "LIMIT 1";
	}

	public static String indicatorLinks(String id) {
		return "SELECT ?uriLinked ?typeOfLink ?labelLg1 ?labelLg2 \n"
				+ "WHERE { \n" 

				+ "{?indic dcterms:replaces ?uriLinked . \n"
				+ "BIND('replaces' AS ?typeOfLink)} \n"
				+ "UNION"
				+ "{?indic dcterms:isReplacedBy ?uriLinked . \n"
				+ "BIND('isReplacedBy' AS ?typeOfLink)} \n"
				+ "UNION"
				+ "{?indic rdfs:seeAlso ?uriLinked . \n"
				+ "BIND('seeAlso' AS ?typeOfLink)} \n"
				+ "UNION"
				+ "{?indic prov:wasGeneratedBy ?uriLinked . \n"
				+ "BIND('wasGeneratedBy' AS ?typeOfLink)} \n"

				+ "?uriLinked skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') . \n"
				+ "OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "')} . \n"

			+ "FILTER(REGEX(STR(?indic),'/produits/indicateur/" + id + "')) . \n"


				+ "} \n"
				+ "ORDER BY ?typeOfLink";
	}

	private static void getSimpleAttr(String id) {
		addVariableToList(" ?id ");

		addVariableToList(" ?prefLabelLg1 ?prefLabelLg2 ");
		addOptionalClause("skos:prefLabel", "?prefLabel");

		addVariableToList(" ?altLabelLg1 ?altLabelLg2 ");
		addOptionalClause("skos:altLabel", "?altLabel");

		addVariableToList(" ?abstractLg1 ?abstractLg2 ");
		addOptionalClause("dcterms:abstract", "?abstract");

		addVariableToList(" ?historyNoteLg1 ?historyNoteLg2 ");
		addOptionalClause("skos:historyNote", "?historyNote");

		addClauseToWhereClause(" FILTER(REGEX(STR(?indic),'/produits/indicateur/" + id+ "')) . \n" 
				+ "BIND(STRAFTER(STR(?indic),'/indicateur/') AS ?id) . \n" );

	}

	private static void addOptionalClause(String predicate, String variableName){
		addClauseToWhereClause( "OPTIONAL{?indic "+predicate+" "+variableName + "Lg1 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg1) = '" + Config.LG1 + "') } \n ");
		addClauseToWhereClause( "OPTIONAL{?indic "+predicate+" "+variableName + "Lg2 \n");
		addClauseToWhereClause( "FILTER (lang("+variableName + "Lg2) = '" + Config.LG2 + "') } \n ");
	}

	private static void getCodesLists() {//TODO
		addVariableToList(" ?accrualPeriodicityCode ?accrualPeriodicityList ");
		addClauseToWhereClause( "OPTIONAL {?indic dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n"
				+ "}   \n" );
	}

	private static void getOrganizations() {
		addVariableToList(" ?creator ");
		addClauseToWhereClause(
				"OPTIONAL {?indic dcterms:creator ?uriCreator . \n"
						+ "?uriCreator dcterms:identifier  ?creator . \n"
						+ "}   \n");
		addVariableToList(" ?stakeHolder  ");
		addClauseToWhereClause(  
				"OPTIONAL {?indic insee:stakeHolder ?uriStakeHolder . \n"
						+ "?uriStakeHolder dcterms:identifier  ?stakeHolder . \n"
						+ "}   \n");
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





}
