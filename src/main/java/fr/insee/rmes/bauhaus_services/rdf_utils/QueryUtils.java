package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class QueryUtils {

	public static final String PREFIXES =
            """
                    PREFIX dcterms:<http://purl.org/dc/terms/>\s
                    PREFIX xkos:<http://rdf-vocabulary.ddialliance.org/xkos#>\s
                    PREFIX evoc:<http://eurovoc.europa.eu/schema#>\s
                    PREFIX skos:<http://www.w3.org/2004/02/skos/core#>\s
                    PREFIX skosxl:<http://www.w3.org/2008/05/skos-xl#>\s
                    PREFIX dc:<http://purl.org/dc/elements/1.1/>\s
                    PREFIX insee:<http://rdf.insee.fr/def/base#>\s
                    PREFIX geo:<http://www.opengis.net/ont/geosparql#>\s
                    PREFIX igeo:<http://rdf.insee.fr/def/geo#>\s
                    PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\s
                    PREFIX pav:<http://purl.org/pav/>\s
                    PREFIX foaf:<http://xmlns.com/foaf/0.1/>\s
                    PREFIX org:<http://www.w3.org/ns/org#>\s
                    PREFIX prov:<http://www.w3.org/ns/prov#>\s
                    PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\s
                    PREFIX sdmx-mm:<http://www.w3.org/ns/sdmx-mm#>\s
                    PREFIX qb:<http://purl.org/linked-data/cube#>\s
                    PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\s
                    PREFIX dcat:<http://www.w3.org/ns/dcat#>\s
                    PREFIX adms: <http://www.w3.org/ns/adms#>\s
                    PREFIX dcmitype:<http://purl.org/dc/dcmitype/>\s
                    \s
                    """;


	/**
	 * Hack to fix Sparql groupconcat which returns an empty object in array
	 * instead of empty array
	 * @param res
	 * @return
	 */
	public static String correctEmptyGroupConcat(String res) {
		if(res.equals("[{\"altLabel\":\"\"}]")) {
			return "[]";
		}
		return res;
	}

	public static JSONArray transformRdfTypeInString(JSONArray jArray) {
		for (int i = 0; i < jArray.length(); i++) {
			JSONObject jsonObject = jArray.getJSONObject(i);
			if (jsonObject.has(Constants.TYPE_OF_OBJECT)) {
				String typeOfObject = jsonObject.getString(Constants.TYPE_OF_OBJECT);
				String type = ObjectType.getLabelType(SimpleValueFactory.getInstance().createIRI(typeOfObject));
				jsonObject.put("type", type);
				jsonObject.remove(Constants.TYPE_OF_OBJECT);
			}
		}
		return jArray;
	}

	private QueryUtils() {
		throw new IllegalStateException("Utility class");
	}


}
