package fr.insee.rmes.persistance.sparql_queries.links;

import org.eclipse.rdf4j.model.IRI;

public class LinksQueries {
	
	public static String getLinksToDelete(IRI conceptURI) {
		return "SELECT ?concept \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> { \n"
				+ "?concept ?b <" + conceptURI.toString() + "> \n"
				+ " }}";
	}

}
