package fr.insee.rmes.persistance.service.sesame.utils;

public class QueryUtils {
	
	public static final String PREFIXES =
			"PREFIX dcterms:<http://purl.org/dc/terms/> \n"
			+ "PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#> \n"
			+ "PREFIX evoc:<http://eurovoc.europa.eu/schema#> \n"
			+ "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> \n"
			+ "PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#> \n"
			+ "PREFIX dc:<http://purl.org/dc/elements/1.1/> \n"
			+ "PREFIX insee:<http://rdf.insee.fr/def/base#> \n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX pav:<http://purl.org/pav/> \n"
			+ "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
			+ "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> \n\n" ;
	
	/**
	 * Hack to fix Sparql groupconcat which returns an empty object in array
	 * instead of empty array
	 * @param res
	 * @return
	 */
	public static String correctEmptyGroupConcat(String res) {
		if(res.equals("[{\"altLabel\":\"\"}]")) return "[]";
		return res;
	}

}
