package fr.insee.rmes.persistance.sparql_queries;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

public class GenericQueries {
	
	protected static Config config;

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		GenericQueries.config = config;
	}
	
	public static String getAllGraphs() throws RmesException {
		return FreeMarkerUtils.buildRequest("","getAllGraphs.ftlh", null);	
	}
		

}
