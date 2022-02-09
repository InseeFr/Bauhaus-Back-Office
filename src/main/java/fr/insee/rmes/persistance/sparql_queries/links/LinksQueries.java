package fr.insee.rmes.persistance.sparql_queries.links;

import org.eclipse.rdf4j.model.IRI;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;

public class LinksQueries {
	
	public static String getLinksToDelete(IRI conceptURI) {
		return "SELECT ?concept \n"
				+ "WHERE { GRAPH <"+Config.CONCEPTS_GRAPH+"> { \n"
				+ "?concept ?b <" + RdfUtils.toString(conceptURI) + "> \n"
				+ " }}";
	}
	
	  private LinksQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
