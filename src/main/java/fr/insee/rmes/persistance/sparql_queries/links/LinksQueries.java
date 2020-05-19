package fr.insee.rmes.persistance.sparql_queries.links;

import org.openrdf.model.URI;

public class LinksQueries {
	
	public static String getLinksToDelete(URI conceptURI) {
		return "SELECT ?concept \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> { \n"
				+ "?concept ?b <" + conceptURI.toString() + "> \n"
				+ " }}";
	}

}
