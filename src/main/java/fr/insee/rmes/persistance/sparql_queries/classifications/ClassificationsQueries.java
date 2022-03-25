package fr.insee.rmes.persistance.sparql_queries.classifications;

import java.util.HashMap;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class ClassificationsQueries {
	
	public static String classificationsQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { GRAPH ?graph { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "FILTER(regex(str(?classification),'/codes/')) \n"
			+ "?classification skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.getLg1() + "') \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "} \n"
			+ "FILTER(REGEX(STR(?graph), '/codes/') ) \n"
			+ "} \n"
			+ "ORDER BY ?label ";	
	}
	
	public static String getGraphUriById(String classifId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("classifId", classifId);
		return FreeMarkerUtils.buildRequest("classifications/", "getGraphUriById.ftlh", params);
	}
	
	public static String classificationQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 ?seriesLg1 ?seriesLg2 ?idSeries \n"
			+ "?afterLg1 ?afterLg2 ?idAfter ?beforeLg1 ?beforeLg2 ?idBefore ?variantLg1 ?variantLg2 ?idVariant \n"
			+ "?issued ?valid ?lastRefreshedOn ?additionalMaterial ?rights ?creator ?contributor \n"
			+ "?disseminationStatus ?legalMaterial ?homepage \n"
			+ "?scopeNoteLg1 ?scopeNoteLg2 ?changeNoteLg1 ?changeNoteLg2 ?descriptionLg1 ?descriptionLg2 \n"
			+ "WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "/')) . \n"
			// PrefLabel
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "?classification skos:prefLabel ?prefLabelLg1 . \n"
			+ "FILTER (lang(?prefLabelLg1) = '" + Config.getLg1() + "') . \n"
			+ "OPTIONAL {?classification skos:prefLabel ?prefLabelLg2 . \n"
			+ "FILTER (lang(?prefLabelLg2) = '" + Config.getLg2() + "') } . \n"
			// AltLabel
			+ "{OPTIONAL{ \n"
			+ "SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "?classification skos:altLabel ?altLg1 . \n"
			+ "FILTER (lang(?altLg1) = '" + Config.getLg1() + "')  . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "/')) . \n"
			+ "}}} \n"
			+ "{OPTIONAL{ \n"
			+ "SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "?classification skos:altLabel ?altLg2 . \n"
			+ "FILTER (lang(?altLg2) = '" + Config.getLg2() + "')  . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "/')) . \n"
			+ "}}} \n"
			// Remarque lg1
			+ "OPTIONAL {?classification skos:scopeNote ?scopeLg1 . \n"
			+ "?scopeLg1 dcterms:language '" + Config.getLg1() + "'^^xsd:language . \n"
			+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
			+ "} . \n"
			// Remarque Lg2
			+ "OPTIONAL {?classification skos:scopeNote ?scopeLg2 . \n"
			+ "?scopeLg2 dcterms:language '" + Config.getLg2() + "'^^xsd:language . \n"
			+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
			+ "} . \n"
			// ChangeNote lg1
			+ "OPTIONAL {?classification skos:changeNote ?changeLg1 . \n"
			+ "?changeLg1 dcterms:language '" + Config.getLg1() + "'^^xsd:language . \n"
			+ "?changeLg1 evoc:noteLiteral ?changeNoteLg1 . \n"
			+ "} . \n"
			// ChangeNote Lg2
			+ "OPTIONAL {?classification skos:changeNote ?changeLg2 . \n"
			+ "?changeLg2 dcterms:language '" + Config.getLg2() + "'^^xsd:language . \n"
			+ "?changeLg2 evoc:noteLiteral ?changeNoteLg2 . \n"
			+ "} . \n"
			// Description Lg1
			+ "OPTIONAL {?classification dc:description ?descriptionLg1  . \n"
			+ "FILTER (lang(?descriptionLg1) = '" + Config.getLg1() + "') } \n "
			// Description Lg1
			+ "OPTIONAL {?classification dc:description ?descriptionLg2  . \n"
			+ "FILTER (lang(?descriptionLg2) = '" + Config.getLg2() + "') } \n "
			// BelongsTo
			+ "OPTIONAL {?classification xkos:belongsTo ?seriesURI . \n"
			+ "?seriesURI skos:prefLabel ?seriesLg1 . \n"
			+ "FILTER (lang(?seriesLg1) = '" + Config.getLg1() + "')  . \n"
			+ "BIND(STRAFTER(STR(?seriesURI),'/codes/serieDeNomenclatures/') AS ?idSeries) } . \n"
			+ "OPTIONAL {?classification xkos:belongsTo ?seriesURI . \n"
			+ "?seriesURI skos:prefLabel ?seriesLg2 . \n"
			+ "FILTER (lang(?seriesLg2) = '" + Config.getLg2() + "') }  . \n"
			// After
			+ "OPTIONAL {?classification xkos:after ?afterURI . \n"
			+ "?afterURI skos:prefLabel ?afterLg1 . \n"
			+ "FILTER (lang(?afterLg1) = '" + Config.getLg1() + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?afterURI),'/codes/'), '/') AS ?idAfter) } . \n"
			+ "OPTIONAL {?classification xkos:after ?afterURI . \n"
			+ "?afterURI skos:prefLabel ?afterLg2 . \n"
			+ "FILTER (lang(?afterLg2) = '" + Config.getLg2() + "') }  . \n"
			// Before
			+ "OPTIONAL {?classification xkos:before ?beforeURI . \n"
			+ "?beforeURI skos:prefLabel ?beforeLg1 . \n"
			+ "FILTER (lang(?beforeLg1) = '" + Config.getLg1() + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?beforeURI),'/codes/'), '/') AS ?idBefore) } . \n"
			+ "OPTIONAL {?classification xkos:before ?beforeURI . \n"
			+ "?beforeURI skos:prefLabel ?beforeLg2 . \n"
			+ "FILTER (lang(?beforeLg2) = '" + Config.getLg2() + "') }  . \n"
			// Variant
			+ "OPTIONAL {?classification xkos:variant ?variantURI . \n"
			+ "?variantURI skos:prefLabel ?variantLg1 . \n"
			+ "FILTER (lang(?variantLg1) = '" + Config.getLg1() + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?variantURI),'/codes/'), '/') AS ?idVariant) } . \n"
			+ "OPTIONAL {?classification xkos:variant ?variantURI . \n"
			+ "?variantURI skos:prefLabel ?variantLg2 . \n"
			+ "FILTER (lang(?variantLg2) = '" + Config.getLg2() + "') }  . \n"
			+ "OPTIONAL {?classification dcterms:issued ?issued} . \n"
			+ "OPTIONAL {?classification dcterms:valid ?valid} . \n"
			+ "OPTIONAL {?classification pav:lastRefreshedOn ?lastRefreshedOn} . \n"
			+ "OPTIONAL {?classification insee:additionalMaterial ?additionalMaterial} . \n"
			+ "OPTIONAL {?classification dc:rights ?rights} . \n"
			+ "OPTIONAL {?classification dc:contributor ?contributor} . \n"
			+ "OPTIONAL {?classification dc:creator ?creator} . \n"
			+ "OPTIONAL {?classification insee:disseminationStatus ?disseminationStatus} . \n"
			+ "OPTIONAL {?classification insee:legalMaterial ?legalMaterial} . \n"
			+ "OPTIONAL {?classification foaf:homepage ?homepage} . \n"
			+ "} \n";
	}
	
	public static String classificationItemsQuery(String id) {
		return "SELECT ?id ?labelLg1 ?labelLg2 ?parent ?altlabelLg1 ?altlabelLg2 \n"
			+ "WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "/')) . \n"
			+ "?item skos:inScheme ?classification . \n"
			+ "?item skos:prefLabel ?labelLg1 . \n"
			+ "FILTER (lang(?labelLg1) = '" + Config.getLg1() + "') \n"
			+ "OPTIONAL{?item skos:prefLabel ?labelLg2 . \n"
			+ "FILTER (lang(?labelLg2) = '" + Config.getLg2() + "') } \n"
			+ "OPTIONAL{?item skos:altLabel ?altlabelLg1 . \n"
			+ "FILTER (lang(?altlabelLg1) = '" + Config.getLg1() + "') } \n"
			+ "OPTIONAL{?item skos:altLabel ?altlabelLg2 . \n"
			+ "FILTER (lang(?altlabelLg2) = '" + Config.getLg2() + "') } \n"
			+ "?item skos:notation ?id . \n"
			+ "OPTIONAL{?item skos:broader ?broader . \n"
			+ "?broader skos:notation ?parent} \n"
			+ "?level skos:member ?item . \n"
			+ "?level xkos:depth ?depth \n"
			+ "} \n"
			+ "ORDER BY ?depth ?id ";
	}
	
	
	private ClassificationsQueries() {
	    throw new IllegalStateException("Utility class");
	}

	
}
