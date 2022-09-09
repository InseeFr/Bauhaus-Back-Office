package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.Config;

public class AuthType {

	public static String getAuthType(Config config) {
		if (config.getEnv().equals("pre-prod") || config.getEnv().equals("prod") || config.getEnv().equals("PROD")) return "OpenIDConnectAuth";
		else return "NoAuthImpl";
	}
	
	private AuthType() {
		    throw new IllegalStateException("Utility class");
	}

	
}
