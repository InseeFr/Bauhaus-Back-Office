package fr.insee.rmes.persistance.sparql_queries;

import fr.insee.rmes.config.Config;

public class GenericQueries {
	
	protected static Config config;

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		GenericQueries.config = config;
	}

}
