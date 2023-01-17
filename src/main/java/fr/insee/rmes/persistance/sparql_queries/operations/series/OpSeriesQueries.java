package fr.insee.rmes.persistance.sparql_queries.operations.series;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;

public class OpSeriesQueries extends GenericQueries{

	private OpSeriesQueries() {
		throw new IllegalStateException("Utility class");
	}

	private static StringBuilder variables;
	private static StringBuilder whereClause;
	static Map<String, Object> params;
	
	private static final String ID_SERIES = "ID_SERIES";
	private static final String PRODUCTS_GRAPH = "PRODUCTS_GRAPH";
	private static final String STAMP = "STAMP";
	private static final String URI_SERIES = "URI_SERIES";
	private static final String ORGANIZATIONS_GRAPH = "ORGANIZATIONS_GRAPH";
	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	private static final String ORG_INSEE_GRAPH = "ORG_INSEE_GRAPH";
	private static final String LINK_PREDICATE = "LINK_PREDICATE";

	public static String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/operations/serie/");
		params.put("TYPE", "StatisticalOperationSeries");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public static String oneSeriesQuery(String id) {
		variables = null;
		whereClause = null;
		getSimpleAttr(id);
		getCodesLists();
		getValidationState();

		return "SELECT " + variables.toString() + " WHERE {  \n" + whereClause.toString() + "} \n" + "LIMIT 1";
	}

	public static String getSeriesForSearch(String stamp) throws RmesException {
			if (params==null) {initParams();}
			params.put("stamp", stamp ==null ? "" : stamp);
			return buildSeriesRequest("getSeriesForAdvancedSearchQuery.ftlh", params);	
	}

	private static void getSimpleAttr(String id) {

		if (id != null) {
			addClauseToWhereClause(" FILTER(STRENDS(STR(?series),'/operations/serie/" + id + "')) . \n");
		} else {
			addClauseToWhereClause("?series a insee:StatisticalOperationSeries .");
			addClauseToWhereClause("BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . ");
		}

		addVariableToList("?id ?prefLabelLg1 ?prefLabelLg2 ?created ?modified");
		addClauseToWhereClause("?series skos:prefLabel ?prefLabelLg1 \n");
		addClauseToWhereClause( "OPTIONAL { ?series dcterms:created ?created } .  \n ");
		addClauseToWhereClause( "OPTIONAL { ?series dcterms:modified ?modified } .  \n ");

		addClauseToWhereClause("FILTER (lang(?prefLabelLg1) = '" + config.getLg1() + "')  \n ");
		addClauseToWhereClause("OPTIONAL{?series skos:prefLabel ?prefLabelLg2 \n");
		addClauseToWhereClause("FILTER (lang(?prefLabelLg2) = '" + config.getLg2() + "') } \n ");



		addVariableToList(" ?altLabelLg1 ?altLabelLg2 ");
		addOptionalClause("skos:altLabel", "?altLabel");

		addVariableToList(" ?abstractLg1 ?abstractLg2 ");
		addOptionalClause("dcterms:abstract", "?abstract");

		addVariableToList(" ?historyNoteLg1 ?historyNoteLg2 ");
		addOptionalClause("skos:historyNote", "?historyNote");

		addVariableToList(" ?idSims ");
		addGetSimsId();
	}

	private static void addOptionalClause(String predicate, String variableName) {
		addClauseToWhereClause("OPTIONAL{?series " + predicate + " " + variableName + "Lg1 \n");
		addClauseToWhereClause("FILTER (lang(" + variableName + "Lg1) = '" + config.getLg1() + "') } \n ");
		addClauseToWhereClause("OPTIONAL{?series " + predicate + " " + variableName + "Lg2 \n");
		addClauseToWhereClause("FILTER (lang(" + variableName + "Lg2) = '" + config.getLg2() + "') } \n ");
	}

	private static void addGetSimsId() {
		addClauseToWhereClause(
				"OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport ." + " ?report sdmx-mm:target ?series "
						+ " BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n" + "} \n");
	}

	private static void getCodesLists() {
		addVariableToList(" ?typeCode ?typeList ");
		addClauseToWhereClause("OPTIONAL {?series dcterms:type ?type . \n" + "?type skos:notation ?typeCode . \n"
				+ "?type skos:inScheme ?typeCodeList . \n" + "?typeCodeList skos:notation ?typeList . \n" + "}   \n");

		addVariableToList(" ?accrualPeriodicityCode ?accrualPeriodicityList ");
		addClauseToWhereClause("OPTIONAL {?series dcterms:accrualPeriodicity ?accrualPeriodicity . \n"
				+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \n"
				+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \n"
				+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \n" + "}   \n");
	}


	private static void getValidationState() {
		addVariableToList(" ?validationState ");
		addClauseToWhereClause("OPTIONAL {?series insee:validationState ?validationState . \n" + "}   \n");
	}

	private static void addVariableToList(String variable) {
		if (variables == null) {
			variables = new StringBuilder();
		}
		variables.append(variable);
	}

	private static void addClauseToWhereClause(String clause) {
		if (whereClause == null) {
			whereClause = new StringBuilder();
		}
		whereClause.append(clause);
	}
	
	
  //////////////////////////
 //   Using .flth files  //
//////////////////////////
	
	private static void initParams() {
		params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORG_INSEE_GRAPH, config.getOrgInseeGraph());
	}
	
	private static String buildSeriesRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getFamily(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesFamilyQuery.ftlh", params);	
	}
	
	
	/**
	 * @param uriSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsBySeriesUri(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("getSeriesCreatorsByUriQuery.ftlh", params);	
	}
	
	public static String checkIfSeriesHasSims(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("checkIfSeriesHasSimsQuery.ftlh", params);	
	}
	
	
	public static String checkIfSeriesHasOperation(String uriSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("checkIfSeriesHasOperationQuery.ftlh", params);	
	}
	
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsById(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesCreatorsByIdQuery.ftlh", params);	
	}
	
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getGeneratedWith(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesGeneratedWithQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getOperations(String idSeries) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		params.put(PRODUCTS_GRAPH, config.getProductsGraph());
		return buildSeriesRequest("getSeriesOperationsQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries, linkPredicate
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesLinks(String idSeries, IRI linkPredicate, String resultType) throws RmesException {
		if (params==null) {initParams();}
		params.put(ID_SERIES, idSeries);
		params.put(LINK_PREDICATE, linkPredicate);
		if(Constants.ORGANIZATIONS.equals(resultType)) {
			return buildSeriesRequest("getSeriesOrganizationsLinksQuery.ftlh", params);	
		}
		params.put(PRODUCTS_GRAPH, config.getProductsGraph());		
		return buildSeriesRequest("getSeriesLinksQuery.ftlh", params);	
	}
	
	/**
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesWithSimsQuery() throws RmesException {
		if (params==null) {initParams();}
		params.put("withSims", "true");
		return buildSeriesRequest("getSeriesQuery.ftlh", params);	
	}
	
	/**
	 * @param stamp
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesWithStampQuery(String stamp, boolean isAdmin) throws RmesException {
		if (params==null) {initParams();}
		params.put(STAMP, stamp);
		params.put("ADMIN", isAdmin);
		return buildSeriesRequest("getSeriesWithStampQuery.ftlh", params);
	}
	
	
	/**
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesQuery() throws RmesException {
		if (params==null) {initParams();}
		params.put("withSims", "false");
		return buildSeriesRequest("getSeriesQuery.ftlh", params);	
	}


}
