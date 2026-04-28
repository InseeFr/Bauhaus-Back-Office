package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationFamiliesQueries {

	private final Config config;

	public ClassificationFamiliesQueries(Config config) {
		this.config = config;
	}

	public String familiesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("GRAPH", config.getClassifFamiliesGraph());
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest("classifications/families/", "getFamilies.ftlh", params);
	}

	public String familyQuery(String id) {
		return "SELECT ?prefLabelLg1 \n"
			+ "WHERE { GRAPH<"+ config.getClassifFamiliesGraph() + "> { \n"
			+ "?family skos:prefLabel ?prefLabelLg1 . \n"
			+ "FILTER (lang(?prefLabelLg1) = '" + config.getLg1() + "') \n"
			+ "FILTER(REGEX(STR(?family),'/familleDeNomenclatures/" + id + "')) } \n"
			+ "} \n";
	}

	public String familyMembersQuery(String id) {
		return "SELECT DISTINCT ?id ?labelLg1 ?labelLg2 \n"
			+ "WHERE { \n"
			+ "?series xkos:belongsTo ?family . \n"
			+ "?series skos:prefLabel ?labelLg1 . \n"
			+ "FILTER (lang(?labelLg1) = '" + config.getLg1() + "') \n"
			+ "OPTIONAL {?series skos:prefLabel ?labelLg2 . \n"
			+ "FILTER (lang(?labelLg2) = '" + config.getLg2() + "') } \n"
			+ "FILTER(REGEX(STR(?family),'/familleDeNomenclatures/" + id + "')) . \n"
			+ "BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) \n"
			+ "} \n"
			+ "ORDER BY ?labelLg1 ";
	}
}
