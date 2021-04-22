package fr.insee.rmes.persistance.sparql_queries.links;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleIRI;

import fr.insee.rmes.config.Config;

public class LinksQueries {
	
	public static String getLinksToDelete(IRI conceptURI) {
		return "SELECT ?concept \n"
				+ "WHERE { GRAPH <"+Config.CONCEPTS_GRAPH+"> { \n"
				+ "?concept ?b <" + ((SimpleIRI)conceptURI).toString() + "> \n"
				+ " }}";
	}

}
