package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.Config;

public class AuthType {

	public static String getAuthType() {
		if (Config.getEnv().equals("qf")) return "BasicAuthImpl";
		else if (Config.getEnv().equals("pre-prod") || Config.getEnv().equals("prod") || Config.getEnv().equals("PROD")) return "OpenIDConnectAuth";
		else return "NoAuthImpl";
	}
	
	private AuthType() {
		    throw new IllegalStateException("Utility class");
	}

	
}
