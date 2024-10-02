package fr.insee.rmes.config.auth;

import fr.insee.rmes.model.rbac.*;
import fr.insee.rmes.model.rbac.Module;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@ConfigurationProperties("rbac")
public record RBACConfiguration (Map<String, Map<Module, Map<Privilege, Strategy>>> config){

     public RBAC toRBAC() {
        return new RBAC(config.entrySet().stream()
                .collect(Collectors.toMap(entry->new RoleName(entry.getKey()),
                        entry -> ApplicationAccessPrivileges.of(entry.getValue()),
                        ApplicationAccessPrivileges::merge
                        )));
    }

    public static List<RoleName> toRolesNames(List<String> roles) {
        return roles == null ? List.of() : roles.stream().filter(Objects::nonNull).map(RoleName::new).toList();
    }

    public record RoleName(String role) {
    }
}
