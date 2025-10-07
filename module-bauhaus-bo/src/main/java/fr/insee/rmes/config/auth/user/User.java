package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.infrastructure.rbac.Roles;
import fr.insee.rmes.domain.model.Stamp;

import java.util.List;

public record User(String id, List<String> roles, Stamp stamp, Source source) {


	public static final User FAKE_USER = new User("fakeUser", List.of(Roles.ADMIN), new Stamp("fakeStampForDvAndQf"), Source.INSEE);
	public static final User EMPTY_USER = new User();
    private User() {
        this(null, List.of(), new Stamp(""), null);
    }

    public User(String id, List<String> roles, String stamp){
        this(id, roles, new Stamp(stamp), null);
    }

    public User(String id, List<String> roles, String stamp, String source){
        this(id, roles, new Stamp(stamp), Source.fromValue(source));
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public String getStamp(){
        return stamp.stamp();
    }
}
