package fr.insee.rmes.persistance.sparql_queries.operations.indicators;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.Map;

public class IndicatorsQueries extends GenericQueries{


	private static String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException  {
		return FreeMarkerUtils.buildRequest("operations/indicators/", fileName, params);
	}

	public static String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("OPERATIONS_GRAPH", config.getProductsGraph());
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
		params.put("PRODUCTS_GRAPH",config.getProductsGraph());
		params.put(Constants.ID, id);
		return buildIndicatorRequest("getPublicationStatusQuery.ftlh", params);	
	}
	
	public static String indicatorsQuery() throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("PRODUCTS_GRAPH",config.getProductsGraph());
		params.put("PRODUCT_BASE_URI",config.getProductsBaseUri());
		return buildIndicatorRequest("getIndicators.ftlh", params);
	}

	public static String indicatorsQueryForSearch() {
	return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 (group_concat(?altLabelLang1;separator=' || ') as ?altLabelLg1) ?altLabelLg2  ?abstractLg1 ?abstractLg2  "
			+ "?historyNoteLg1 ?historyNoteLg2  ?accrualPeriodicityCode ?accrualPeriodicityList  ?publishers  ?idSims  ?validationState  "
			+ "WHERE {  \r\n" 
			+ "?indic a insee:StatisticalIndicator ."
			+ "BIND(STRAFTER(STR(?indic),'/"+config.getProductsBaseUri()+"/') AS ?id) . "
					+ "?indic skos:prefLabel ?prefLabelLg1 \r\n" 
			+ "FILTER (lang(?prefLabelLg1) = 'fr') \r\n" 
			+ " OPTIONAL{?indic skos:prefLabel ?prefLabelLg2 \r\n" 
			+ "FILTER (lang(?prefLabelLg2) = 'en') } \r\n" 
			+ " OPTIONAL{?indic skos:altLabel ?altLabelLang1 \r\n" 
			+ "FILTER (lang(?altLabelLang1) = 'fr') } \r\n" 
			+ " OPTIONAL{?indic skos:altLabel ?altLabelLg2 \r\n" 
			+ "FILTER (lang(?altLabelLg2) = 'en') } \r\n" 
			+ " OPTIONAL{?indic dcterms:abstract ?abstractLg1 \r\n" 
			+ "FILTER (lang(?abstractLg1) = 'fr') } \r\n" 
			+ " OPTIONAL{?indic dcterms:abstract ?abstractLg2 \r\n" 
			+ "FILTER (lang(?abstractLg2) = 'en') } \r\n" 
			+ " OPTIONAL{?indic skos:historyNote ?historyNoteLg1 \r\n" 
			+ "FILTER (lang(?historyNoteLg1) = 'fr') } \r\n" 
			+ " OPTIONAL{?indic skos:historyNote ?historyNoteLg2 \r\n" 
			+ "FILTER (lang(?historyNoteLg2) = 'en') } \r\n" 
			+ " OPTIONAL {?indic dcterms:accrualPeriodicity ?accrualPeriodicity . \r\n" 
			+ "?accrualPeriodicity skos:notation ?accrualPeriodicityCode . \r\n" 
			+ "?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . \r\n" 
			+ "?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . \r\n" 
			+ "}   \r\n" 
			+ "OPTIONAL {?indic dcterms:publisher ?uriPublisher . \r\n" 
			+ "?uriPublisher dcterms:identifier  ?publishers . \r\n" 
			+ "}   \r\n" 
			+ "OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport . ?report sdmx-mm:target ?indic  BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . \r\n" 
			+ "} \r\n" 
			+ "OPTIONAL {?indic insee:validationState ?validationState . \r\n" 
			+ "}   \r\n" 
			+ "} \r\n" 
			+ "GROUP BY ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLang1 ?altLabelLg2 ?abstractLg1 ?abstractLg2 ?historyNoteLg1 ?historyNoteLg2  "
			+ "?accrualPeriodicityCode ?accrualPeriodicityList  ?publishers  ?idSims  ?validationState \n";	
		
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

	public static String getCreatorsById(String id) {
		return "SELECT ?creators\n"
				+ "WHERE { GRAPH <"+config.getProductsGraph()+"> { \n"
				+ "?indic a insee:StatisticalIndicator . \n"  
				+" FILTER(STRENDS(STR(?indic),'/"+config.getProductsBaseUri()+"/" + id+ "')) . \n" 
				+"?indic dc:creator ?creators . \n"
				+ "} }"
				;
	}
	
	public static String getPublishersById(String id) {
		return "SELECT ?publishers\n"
				+ "WHERE { GRAPH <"+config.getProductsGraph()+"> { \n"
				+ "?indic a insee:StatisticalIndicator . \n"  
				+" FILTER(STRENDS(STR(?indic),'/"+config.getProductsBaseUri()+"/" + id+ "')) . \n" 
				+"?indic dcterms:publisher ?publishers . \n"
				+ "} }"
				;
	}	

	public static String indicatorLinks(String id, IRI linkPredicate) {
		return "SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 \n"
				+ "WHERE { \n" 
				+ "?indic <"+linkPredicate+"> ?uriLinked . \n"
				+ "?uriLinked skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "')} . \n"
				+ "?uriLinked rdf:type ?typeOfObject . \n"
				+ "BIND(REPLACE( STR(?uriLinked) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . \n"
				
				+ "FILTER(STRENDS(STR(?indic),'/"+config.getProductsBaseUri()+"/" + id + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?labelLg1";
	}
	
	public static String getMultipleOrganizations(String idIndicator, IRI linkPredicate) {
		return "SELECT ?id ?labelLg1 ?labelLg2\n"
				+ "WHERE { \n" 
				+"?indicator <"+linkPredicate+"> ?uri . \n"
				+ "?uri dcterms:identifier  ?id . \n"
				+ "?uri skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') . \n"
				+ "OPTIONAL {?uri skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "')} . \n"
				
				+ "FILTER(STRENDS(STR(?indicator),'/"+config.getProductsBaseUri()+"/" + idIndicator + "')) . \n"

				+ "} \n"
				+ "ORDER BY ?id";
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

	  private IndicatorsQueries() {
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
