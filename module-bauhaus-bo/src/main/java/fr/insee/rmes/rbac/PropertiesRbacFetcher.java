package fr.insee.rmes.rbac;

import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.domain.auth.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PropertiesRbacFetcher implements RbacFetcher {

    private final Set<AllModuleAccessPrivileges> allModulesAccessPrivileges;

    public PropertiesRbacFetcher(RBACConfiguration rbacConfiguration) {
        this.allModulesAccessPrivileges = rbacConfiguration.allModulesAccessPrivileges();
    }

    @Override
    public Set<ModuleAccessPrivileges> getPrivilegesByRole(String roleName) {
        return this.allModulesAccessPrivileges
                .stream()
                .filter(p -> p.roleName().role().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new UnknownRoleException(roleName))
                .privileges();
    }

    @Override
    public Set<ModuleAccessPrivileges.Privilege> getApplicationPrivilegesByRole(String roleName, RBAC.Module application) {
        var moduleAccessPrivileges = this.getPrivilegesByRole(roleName);
        return moduleAccessPrivileges
                .stream().
                filter(p -> p.application().equals(application))
                .findFirst()
                .orElseThrow(() -> new UnknownApplicationException(application, roleName))
                .privileges();
    }

    @Override
    public RBAC.Strategy getApplicationActionStrategyByRole(List<String> roles, RBAC.Module application, RBAC.Privilege privilege) {
        Set<RBAC.Strategy> strategies = roles.stream().map(r -> {
            try {
                var privileges = getApplicationPrivilegesByRole(r, application);
                return privileges
                        .stream()
                        .filter(p -> p.privilege().equals(privilege))
                        .findFirst()
                        .orElseThrow(() -> new UnknownPrivilegeException(privilege, r, application))
                        .strategy();
            } catch (UnknownApplicationException | UnknownRoleException exception){

                return RBAC.Strategy.NONE;
            }

        }).filter(Objects::nonNull).collect(Collectors.toSet());

        return Collections.min(strategies, Comparator.comparingInt(Enum::ordinal));
    }

    @Override
    public Set<ModuleAccessPrivileges> computePrivileges(User user) {
        var roles = user.roles();
        var source = user.source();

        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> result = new HashMap<>();

        if (Source.INSEE.equals(source)) {
            for (RBAC.Module module : RBAC.Module.values()) {
                if (module != RBAC.Module.UNKNOWN) {
                    result
                        .computeIfAbsent(module, k -> new HashMap<>())
                        .put(RBAC.Privilege.READ, RBAC.Strategy.ALL);
                }
            }
        }

        for (String role : roles) {
            try {
                Set<ModuleAccessPrivileges> modulePrivileges = getPrivilegesByRole(role);

                for (ModuleAccessPrivileges mp : modulePrivileges) {
                    RBAC.Module module = mp.application();
                    for (ModuleAccessPrivileges.Privilege p : mp.privileges()) {
                        RBAC.Privilege privilege = p.privilege();
                        RBAC.Strategy strategy = p.strategy();

                        result
                                .computeIfAbsent(module, k -> new HashMap<>())
                                .merge(privilege, strategy, (s1, s2) ->
                                        Collections.min(List.of(s1, s2), Comparator.comparingInt(Enum::ordinal))
                                );
                    }
                }
            } catch (UnknownRoleException exception){}

        }

        return result.entrySet().stream()
                .map(entry -> new ModuleAccessPrivileges(
                        entry.getKey(),
                        entry.getValue().entrySet().stream()
                                .map(e -> new ModuleAccessPrivileges.Privilege(e.getKey(), e.getValue()))
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toSet());
    }
}
