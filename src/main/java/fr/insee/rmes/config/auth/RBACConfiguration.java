package fr.insee.rmes.config.auth;

import fr.insee.rmes.external.services.rbac.ApplicationAccessPrivileges;
import fr.insee.rmes.model.rbac.RBAC;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Map;
import java.util.stream.Collectors;


@ConfigurationProperties("rbac")
public record RBACConfiguration (Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> config){

     public Map<RoleName, ApplicationAccessPrivileges> toMapOfApplicationAccessPrivilegesByRoles() {
        return config.entrySet().stream()
                .collect(Collectors.toMap(entry->new RoleName(entry.getKey()),
                        entry -> ApplicationAccessPrivileges.of(entry.getValue()),
                        ApplicationAccessPrivileges::merge
                        ));
    }

    public record RoleName(String role) {}
}
