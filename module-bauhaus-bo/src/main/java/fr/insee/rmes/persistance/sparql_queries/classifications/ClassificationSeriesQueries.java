package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationSeriesQueries {

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public ClassificationSeriesQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	public String seriesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", graphs.classifFamiliesGraph());
		params.put("LG1", languages.lg1());
		return FreeMarkerUtils.buildRequest("classifications/series/", "getSeries.ftlh", params);
	}

	public String oneSeriesQuery(String id) {
		return "SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 \n"
				+ "?scopeNoteLg1 ?scopeNoteLg2 ?subject ?publishers ?covers ?familyLg1 ?familyLg2 ?idFamily \n"
				+ "WHERE { GRAPH<"+ graphs.classifFamiliesGraph() + "> { \n"				+ "?series skos:prefLabel ?prefLabelLg1 . \n"
				+ "FILTER(REGEX(STR(?series),'/serieDeNomenclatures/" + id + "')) . \n"
				+ "BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) . \n"
				+ "FILTER (lang(?prefLabelLg1) = '" + languages.lg1() + "') . \n"
				+ "OPTIONAL {?series skos:prefLabel ?prefLabelLg2 . \n"
				+ "FILTER (lang(?prefLabelLg2) = '" + languages.lg2() + "') } . \n"
				+ "{OPTIONAL{ \n"
				+ "SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE { \n"
				+ "?series skos:altLabel ?altLg1 . \n"
				+ "FILTER (lang(?altLg1) = '" + languages.lg1() + "')  . \n"
				+ "FILTER(REGEX(STR(?series),'/codes/serieDeNomenclatures/" + id + "')) . \n"
				+ "}}} \n"
				+ "{OPTIONAL{ \n"
				+ "SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE { \n"
				+ "?series skos:altLabel ?altLg2 . \n"
				+ "FILTER (lang(?altLg2) = '" + languages.lg2() + "')  . \n"
				+ "FILTER(REGEX(STR(?series),'/codes/serieDeNomenclatures/" + id + "')) . \n"
				+ "}}} \n"
				+ "OPTIONAL {?series dc:subject ?subject } . \n"
				+ "OPTIONAL {?series dc:publisher ?publishers } . \n"
				+ "OPTIONAL {?series xkos:covers ?covers } . \n"
				// Remarque lg1
				+ "OPTIONAL {?series skos:scopeNote ?scopeLg1 . \n"
				+ "?scopeLg1 dcterms:language '" + languages.lg1() + "'^^xsd:language . \n"
				+ "?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 . \n"
				+ "} . \n"
				// Remarque Lg2
				+ "OPTIONAL {?series skos:scopeNote ?scopeLg2 . \n"
				+ "?scopeLg2 dcterms:language '" + languages.lg2() + "'^^xsd:language . \n"
				+ "?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 . \n"
				+ "} . \n"
				+ "OPTIONAL {?series xkos:belongsTo ?familyURI . \n"
				+ "?familyURI skos:prefLabel ?familyLg1 . \n"
				+ "FILTER (lang(?familyLg1) = '" + languages.lg1() + "')  . \n"
				+ "BIND(STRAFTER(STR(?familyURI),'/codes/familleDeNomenclatures/') AS ?idFamily) } . \n"
				+ "OPTIONAL {?series xkos:belongsTo ?familyURI . \n"
				+ "?familyURI skos:prefLabel ?familyLg2 . \n"
				+ "FILTER (lang(?familyLg2) = '" + languages.lg2() + "') }  . \n"
				+ "}} \n"
				+ "LIMIT 1";
	}

	public String seriesMembersQuery(String id) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 \n"
			+ "WHERE { \n"
			+ "?classification xkos:belongsTo ?series . \n"
			+ "?classification skos:prefLabel ?labelLg1 . \n"
			+ "FILTER (lang(?labelLg1) = '" + languages.lg1() + "') \n"
			+ "OPTIONAL {?classification skos:prefLabel ?labelLg2 . \n"
			+ "FILTER (lang(?labelLg2) = '" + languages.lg2() + "') } \n"
			+ "FILTER(REGEX(STR(?series),'/serieDeNomenclatures/" + id + "')) . \n"
			+ "BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?labelLg1 ";
	}
}