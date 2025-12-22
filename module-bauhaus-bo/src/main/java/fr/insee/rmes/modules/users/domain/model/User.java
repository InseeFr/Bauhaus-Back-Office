package fr.insee.rmes.modules.users.domain.model;

import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.auth.Source;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record User(String id, List<String> roles, Set<Stamp> stamps, Source source) {


	public static final User EMPTY_USER = new User();
    private User() {
        this(null, List.of(), Set.of(new Stamp("")), null);
    }

    public User(String id, List<String> roles, Set<String> stamps){
        this(id, roles, stamps.stream().map(Stamp::new).collect(Collectors.toSet()), null);
    }

    public User(String id, List<String> roles, Set<String> stamps, String source){
        this(id, roles, stamps.stream().map(Stamp::new).collect(Collectors.toSet()), Source.fromValue(source));
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public Set<String> getStamps(){
        return stamps.stream().map(Stamp::stamp).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", roles=" + roles +
                ", stamp=" + stamps +
                ", source=" + source +
                '}';
    }
}
