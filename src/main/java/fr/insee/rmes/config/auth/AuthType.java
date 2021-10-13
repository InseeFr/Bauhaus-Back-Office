package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.Config;

public class AuthType {

	public static String getAuthType() {
		if (Config.ENV.equals("qf")) return "BasicAuthImpl";
		else if (Config.ENV.equals("pre-prod") || Config.ENV.equals("prod")) return "OpenIDConnectAuth";
		else return "NoAuthImpl";
	}
	
	private AuthType() {
		    throw new IllegalStateException("Utility class");
	}

	
}
