package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class OperationSeriesQueries extends GenericQueries{

	private OperationSeriesQueries() {
		throw new IllegalStateException("Utility class");
	}

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
		params.put("TYPE", "insee:StatisticalOperationSeries");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public static String oneSeriesQuery(String id, boolean seriesRichTextNexStructure) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("SERIES_RICH_TEXT_NEXT_STRUCTURE", seriesRichTextNexStructure);

		return FreeMarkerUtils.buildRequest("operations/series/", "getSeriesById.ftlh", params);
	}

	public static String getSeriesForSearch(String stamp) throws RmesException {
			Map<String, Object> params = initParams();
			params.put("stamp", stamp ==null ? "" : stamp);
			return buildSeriesRequest("getSeriesForAdvancedSearchQuery.ftlh", params);	
	}

  //////////////////////////
 //   Using .flth files  //
//////////////////////////
	
	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put(ORGANIZATIONS_GRAPH, config.getOrganizationsGraph());
		params.put(ORG_INSEE_GRAPH, config.getOrgInseeGraph());
		params.put(PRODUCTS_GRAPH, config.getProductsGraph());
		return params;
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
		Map<String, Object> params = initParams();
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesFamilyQuery.ftlh", params);	
	}
	
	
	/**
	 * @param uriSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsBySeriesUri(String uriSeries) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("getSeriesCreatorsByUriQuery.ftlh", params);	
	}
	
	public static String checkIfSeriesHasSims(String uriSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("checkIfSeriesHasSimsQuery.ftlh", params);	
	}
	
	
	public static String checkIfSeriesHasOperation(String uriSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_SERIES, uriSeries);
		return buildSeriesRequest("checkIfSeriesHasOperationQuery.ftlh", params);	
	}
	
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getCreatorsById(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesCreatorsByIdQuery.ftlh", params);	
	}
	
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getGeneratedWith(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesGeneratedWithQuery.ftlh", params);
	}
	
	/**
	 * @param idSeries
	 * @return String
	 * @throws RmesException
	 */	
	public static String getOperations(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(ID_SERIES, idSeries);
		return buildSeriesRequest("getSeriesOperationsQuery.ftlh", params);	
	}
	
	/**
	 * @param idSeries, linkPredicate
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesLinks(String idSeries, IRI linkPredicate, String resultType) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(ID_SERIES, idSeries);
		params.put(LINK_PREDICATE, linkPredicate);
		if(Constants.ORGANIZATIONS.equals(resultType)) {
			return buildSeriesRequest("getSeriesOrganizationsLinksQuery.ftlh", params);	
		}
		return buildSeriesRequest("getSeriesLinksQuery.ftlh", params);
	}
	
	/**
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesWithSimsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put("withSims", "true");
		return buildSeriesRequest("getSeriesQuery.ftlh", params);	
	}
	
	/**
	 * @param stamp
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesWithStampQuery(Set<Stamp> stamps, boolean isAdmin) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(STAMP, stamps);
		params.put("ADMIN", isAdmin);
		return buildSeriesRequest("getSeriesWithStampQuery.ftlh", params);
	}

	/**
	 * @return String
	 * @throws RmesException
	 */	
	public static String seriesQuery() throws RmesException {
		Map<String, Object> params = initParams();
		params.put("withSims", "false");
		return buildSeriesRequest("getSeriesQuery.ftlh", params);	
	}

	public static String checkIfSeriesExists(List<String> iris) throws RmesException {
		Map<String, Object> params = initParams();
		params.put(URI_SERIES, iris);
		return buildSeriesRequest("checkIfSeriesExists.ftlh", params);
	}

	public static String getPublishedOperationsForSeries(String iri) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("SERIES_IRI", iri);
		return buildSeriesRequest("getPublishedOperationsForSeries.ftlh", params);
	}
}
