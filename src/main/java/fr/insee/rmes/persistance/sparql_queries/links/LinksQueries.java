package fr.insee.rmes.persistance.sparql_queries.links;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;

public class LinksQueries extends GenericQueries{
	
	public static String getLinksToDelete(IRI conceptURI) {
		return "SELECT ?concept \n"
				+ "WHERE { GRAPH <"+config.getConceptsGraph()+"> { \n"
				+ "?concept ?b <" + RdfUtils.toString(conceptURI) + "> \n"
				+ " }}";
	}
	
	  private LinksQueries() {
		    throw new IllegalStateException("Utility class");
	}


}
