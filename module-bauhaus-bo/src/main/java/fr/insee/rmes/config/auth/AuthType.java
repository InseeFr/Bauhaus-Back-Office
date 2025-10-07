package fr.insee.rmes.config.auth;

public class AuthType {

	public static String getAuthType(String env) {
		if (env.equals("pre-prod") || env.equals("prod") || env.equals("PROD")) return "OpenIDConnectAuth";
		else return "NoAuthImpl";
	}
	
	private AuthType() {
		    throw new IllegalStateException("Utility class");
	}

	
}
