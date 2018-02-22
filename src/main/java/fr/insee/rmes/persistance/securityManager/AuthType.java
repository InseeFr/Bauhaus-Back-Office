package fr.insee.rmes.persistance.securityManager;

public enum AuthType {
	
	NO_AUTH("NoAuthImpl"),
	FAKE_AUTH("FakeAuthImpl"),
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
