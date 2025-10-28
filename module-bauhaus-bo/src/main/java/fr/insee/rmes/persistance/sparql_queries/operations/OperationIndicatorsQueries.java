package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class OperationIndicatorsQueries extends GenericQueries{


	public static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	public static final String PRODUCTS_GRAPH = "PRODUCTS_GRAPH";
	public static final String PRODUCT_BASE_URI = "PRODUCT_BASE_URI";

	private static String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/indicators/", fileName, params);
	}

	public static String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getProductsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/produits/indicateur/");
		params.put("TYPE", "insee:StatisticalIndicator");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public static String getPublicationState(String id) throws RmesException{
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(PRODUCTS_GRAPH,config.getProductsGraph());
		params.put(Constants.ID, id);
		return buildIndicatorRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	public static String indicatorsQuery() throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put(PRODUCTS_GRAPH,config.getProductsGraph());
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		return buildIndicatorRequest("getIndicators.ftlh", params);
	}

	public static String indicatorsQueryForSearch() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return buildIndicatorRequest("getIndicatorsQueryForSearch.ftlh", params);
	}

	public static String indicatorQuery(String id, boolean indicatorsRichTextNexStructure) throws RmesException {
		return indicatorFullObjectQuery(id, true, indicatorsRichTextNexStructure);
	}

	private static String indicatorFullObjectQuery(String id, boolean withLimit, boolean indicatorsRichTextNexStructure) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("WITH_LIMIT", withLimit);
		params.put("INDICATORS_RICH_TEXT_NEXT_STRUCTURE", indicatorsRichTextNexStructure);
		return buildIndicatorRequest("getIndicator.ftlh", params);
	}


	public static String getCreatorsById(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		params.put(OPERATIONS_GRAPH, config.getProductsGraph());
		params.put("ID", id);
		return buildIndicatorRequest("getCreatorsById.ftlh", params);
	}
	

	public static String getPublishersById(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		params.put(OPERATIONS_GRAPH, config.getProductsGraph());
		params.put("ID", id);
		return buildIndicatorRequest("getPublishersById.ftlh", params);
	}


	public static String indicatorLinks(String id, IRI linkPredicate) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", id);
		params.put("LINKPREDICATE", linkPredicate);
		return buildIndicatorRequest("getIndicatorLinks.ftlh", params);
	}


	public static String getMultipleOrganizations(String idIndicator, IRI linkPredicate) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(PRODUCT_BASE_URI,config.getProductsBaseUri());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("ID", idIndicator);
		params.put("LINKPREDICATE", linkPredicate);
		return buildIndicatorRequest("getMultipleOrganizations.ftlh", params);
	}


	public static String lastID() {
		return "SELECT ?id \n"
				+ "WHERE { GRAPH <"+config.getProductsGraph()+"> { \n"
				+ "?uri ?b ?c .\n "
				+ "BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"
				+ "BIND(SUBSTR( ?id , 2 ) AS ?intid) . \n"
				+ "FILTER regex(STR(?uri),'/"+config.getProductsBaseUri()+"/') . \n"
				+ "}} \n"
				+ "ORDER BY DESC(xsd:integer(?intid)) \n"
				+ "LIMIT 1";
	}	

	public static String checkIfExists(String id) {
		return "ASK \n"
				+ "WHERE  \n"
				+ "{ graph <"+config.getProductsGraph()+">    \n"
				+ "{?uri ?b ?c .\n "
				+ "FILTER(STRENDS(STR(?uri),'/"+config.getProductsBaseUri()+"/" + id + "')) . }\n"
				+ "}";
		  	
	}


	public static String getCreatorsByIndicatorUri(String uris) {
		return "SELECT ?creators { \n"
				+ "?indic dc:creator ?creators . \n" 
				+ "VALUES ?indic { " + uris + " } \n"
				+ "}";
	}

	  private OperationIndicatorsQueries() {
		    throw new IllegalStateException("Utility class");
	}


	public static String indicatorsWithSimsQuery() {
		//config.OPERATIONS_GRAPH
		return "SELECT DISTINCT ?labelLg1 ?idSims \n"
				+ "WHERE { \n"
				+ "?indic a insee:StatisticalIndicator . \n"
				+ "?indic skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') \n"
			 	+ "?report rdf:type sdmx-mm:MetadataReport . \n"
				+ "?report sdmx-mm:target ?indic \n"
				+ "BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \n"
				+ "} \n"
				+ "GROUP BY ?labelLg1 ?idSims \n"
				+ "ORDER BY ?labelLg1 ";
	}
}
