package fr.insee.rmes.config.auth.user;

import java.util.List;

public record User(String id, List<String> roles, String stamp) {

	public static final User FAKE_USER=new User("fakeUser",List.of("ROLE_offline_access", "Administrateur_RMESGNCS", "ROLE_uma_authorization"), "fakeStampForDvAndQf");
	public static final User EMPTY_USER = new User();

	private User() {
		this(null, List.of(), "");
	}

	public boolean hasRole(String role) {
		return roles.contains(role);
	}
}
