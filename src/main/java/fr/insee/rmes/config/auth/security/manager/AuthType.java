package fr.insee.rmes.config.auth.security.manager;

public enum AuthType {
	
	NO_AUTH("NoAuthImpl"),
	BASIC_AUTH("BasicAuthImpl"),
	OPEN_ID_CONNECT_AUTH("OpenIDConnectAuth");

	private String auth = "";

	private AuthType(String name) {
		this.auth = name;
	}

	public String getAuth() {
		return this.auth;
	}
}
