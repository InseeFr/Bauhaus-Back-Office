package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.config.auth.roles.Roles;

import java.util.List;

public record User(String id, List<String> roles, Stamp stamp) {


	public static final User FAKE_USER = new User("fakeUser", List.of(Roles.ADMIN), "DG57-C003");
	public static final User EMPTY_USER = new User();
    private User() {
        this(null, List.of(), "");
    }

    public User(String id, List<String> roles, String stamp){
        this(id, roles, new Stamp(stamp));
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public String getStamp(){
        return stamp.stamp();
    }
}
