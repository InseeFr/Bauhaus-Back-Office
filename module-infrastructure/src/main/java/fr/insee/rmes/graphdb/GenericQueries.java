package fr.insee.rmes.graphdb;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;

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
