package fr.insee.rmes.persistance.service.sesame.classifications.classifications;

import fr.insee.rmes.config.Config;

public class ClassificationsQueries {
	
	public static String classificationsQuery() {
		return "SELECT DISTINCT ?id ?label \n"
			+ "WHERE { \n"
			+ "?classification rdf:type skos:ConceptScheme . \n"
			+ "FILTER(regex(str(?classification),'/codes/')) \n"
			+ "?classification skos:prefLabel ?label . \n"
			+ "FILTER (lang(?label) = '" + Config.LG1 + "') \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?label ";	
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
			+ "FILTER (lang(?prefLabelLg1) = '" + Config.LG1 + "') . \n"
			+ "OPTIONAL {?classification skos:prefLabel ?prefLabelLg2 . \n"
			+ "FILTER (lang(?prefLabelLg2) = '" + Config.LG2 + "') } . \n"
			// AltLabel
			+ "{OPTIONAL{ \n"
			+ "SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE { \n"
			+ "?classification skos:altLabel ?altLg1 . \n"
			+ "FILTER (lang(?altLg1) = '" + Config.LG1 + "')  . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "')) . \n"
			+ "}}} \n"
			+ "{OPTIONAL{ \n"
			+ "SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE { \n"
			+ "?classification skos:altLabel ?altLg2 . \n"
			+ "FILTER (lang(?altLg2) = '" + Config.LG2 + "')  . \n"
			+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "')) . \n"
			+ "}}} \n"
			// Remarque lg1
			+ "OPTIONAL {?classification skos:scopeNote ?scopeLg1 . \n"
			+ "?scopeLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
			+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
			+ "} . \n"
			// Remarque Lg2
			+ "OPTIONAL {?classification skos:scopeNote ?scopeLg2 . \n"
			+ "?scopeLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
			+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
			+ "} . \n"
			// ChangeNote lg1
			+ "OPTIONAL {?classification skos:changeNote ?changeLg1 . \n"
			+ "?changeLg1 dcterms:language '" + Config.LG1 + "'^^xsd:language . \n"
			+ "?changeLg1 evoc:noteLiteral ?changeNoteLg1 . \n"
			+ "} . \n"
			// ChangeNote Lg2
			+ "OPTIONAL {?classification skos:changeNote ?changeLg2 . \n"
			+ "?changeLg2 dcterms:language '" + Config.LG2 + "'^^xsd:language . \n"
			+ "?changeLg2 evoc:noteLiteral ?changeNoteLg2 . \n"
			+ "} . \n"
			// Description Lg1
			+ "OPTIONAL {?classification dc:description ?descriptionLg1  . \n"
			+ "FILTER (lang(?descriptionLg1) = '" + Config.LG1 + "') } \n "
			// Description Lg1
			+ "OPTIONAL {?classification dc:description ?descriptionLg2  . \n"
			+ "FILTER (lang(?descriptionLg2) = '" + Config.LG2 + "') } \n "
			// BelongsTo
			+ "OPTIONAL {?classification xkos:belongsTo ?seriesURI . \n"
			+ "?seriesURI skos:prefLabel ?seriesLg1 . \n"
			+ "FILTER (lang(?seriesLg1) = '" + Config.LG1 + "')  . \n"
			+ "BIND(STRAFTER(STR(?seriesURI),'/codes/serieDeNomenclatures/') AS ?idSeries) } . \n"
			+ "OPTIONAL {?classification xkos:belongsTo ?seriesURI . \n"
			+ "?seriesURI skos:prefLabel ?seriesLg2 . \n"
			+ "FILTER (lang(?seriesLg2) = '" + Config.LG2 + "') }  . \n"
			// After
			+ "OPTIONAL {?classification xkos:after ?afterURI . \n"
			+ "?afterURI skos:prefLabel ?afterLg1 . \n"
			+ "FILTER (lang(?afterLg1) = '" + Config.LG1 + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?afterURI),'/codes/'), '/') AS ?idAfter) } . \n"
			+ "OPTIONAL {?classification xkos:after ?afterURI . \n"
			+ "?afterURI skos:prefLabel ?afterLg2 . \n"
			+ "FILTER (lang(?afterLg2) = '" + Config.LG2 + "') }  . \n"
			// Before
			+ "OPTIONAL {?classification xkos:before ?beforeURI . \n"
			+ "?beforeURI skos:prefLabel ?beforeLg1 . \n"
			+ "FILTER (lang(?beforeLg1) = '" + Config.LG1 + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?beforeURI),'/codes/'), '/') AS ?idBefore) } . \n"
			+ "OPTIONAL {?classification xkos:before ?beforeURI . \n"
			+ "?beforeURI skos:prefLabel ?beforeLg2 . \n"
			+ "FILTER (lang(?beforeLg2) = '" + Config.LG2 + "') }  . \n"
			// Variant
			+ "OPTIONAL {?classification xkos:variant ?variantURI . \n"
			+ "?variantURI skos:prefLabel ?variantLg1 . \n"
			+ "FILTER (lang(?variantLg1) = '" + Config.LG1 + "')  . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?variantURI),'/codes/'), '/') AS ?idVariant) } . \n"
			+ "OPTIONAL {?classification xkos:variant ?variantURI . \n"
			+ "?variantURI skos:prefLabel ?variantLg2 . \n"
			+ "FILTER (lang(?variantLg2) = '" + Config.LG2 + "') }  . \n"
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
	
	public static String classificationLevelsQuery(String id) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?depth \n"
				+ "WHERE { \n"
				+ "?classification rdf:type skos:ConceptScheme . \n"
				+ "FILTER(REGEX(STR(?classification),'/codes/" + id + "/')) . \n"
				+ "?classification xkos:levels/rdf:rest*/rdf:first ?levelURI . \n"
				+ "BIND(STRAFTER(STR(?levelURI),'/" + id + "/') AS ?id) \n"
				+ "?levelURI skos:prefLabel ?labelLg1 . \n"
				+ "FILTER (lang(?labelLg1) = '" + Config.LG1 + "') \n"
				+ "OPTIONAL {?levelURI skos:prefLabel ?labelLg2 . \n"
				+ "FILTER (lang(?labelLg2) = '" + Config.LG2 + "') } \n"
				+ "?levelURI xkos:depth ?depth . \n"
				+ "} \n"
				+ "ORDER BY ?depth ";	
	}
	
}
