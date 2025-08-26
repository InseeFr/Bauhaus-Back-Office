package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class ClassifFamiliesQueries extends GenericQueries{
	public static String familiesQuery() throws RmesException {
		Map params = new HashMap();
		params.put("GRAPH", config.getClassifFamiliesGraph());
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest("classifications/families/", "getFamilies.ftlh", params);
	}
	
	public static String familyQuery(String id) {
		return "SELECT ?prefLabelLg1 \n"
			+ "WHERE { GRAPH<"+ config.getClassifFamiliesGraph() + "> { \n"
			+ "?family skos:prefLabel ?prefLabelLg1 . \n"
			+ "FILTER (lang(?prefLabelLg1) = '" + config.getLg1() + "') \n"
			+ "FILTER(REGEX(STR(?family),'/familleDeNomenclatures/" + id + "')) } \n"
			+ "} \n";	
	}
	
	public static String familyMembersQuery(String id) {
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