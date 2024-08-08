package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.rbac.RBAC;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RBACServiceImpl implements RBACService {

    private final RBACConfiguration configuration;

    public RBACServiceImpl(RBACConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ApplicationAccessPrivileges computeRbac(List<RBACConfiguration.RoleName> roles) {

        return roles.stream()
                .map(configuration::accessPrivilegesForRole)
                .reduce(ApplicationAccessPrivileges::merge)
                .orElse(ApplicationAccessPrivileges.NO_PRIVILEGE);

        if (roles.isEmpty()) {
            return new ApplicationAccessPrivileges(Collections.emptyMap());
        }

        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> rbac = configuration.getRbac();

        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> results = new HashMap<>();

        for (String role : roles) {
            Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> rolePrivileges = rbac.get(role);
            if (rolePrivileges != null) {
                mergePrivileges(results, rolePrivileges);
            }
        }

        return new ApplicationAccessPrivileges(results);
    }

    private void mergePrivileges(Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> target,
                                 Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> source) {
        for (Map.Entry<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> entry : source.entrySet()) {
            RBAC.Module app = entry.getKey();
            Map<RBAC.Privilege, RBAC.Strategy> sourcePrivileges = entry.getValue();

            target.merge(app, new HashMap<>(sourcePrivileges), (targetPrivileges, newPrivileges) -> {
                for (Map.Entry<RBAC.Privilege, RBAC.Strategy> privilegeEntry : newPrivileges.entrySet()) {
                    RBAC.Privilege privilege = privilegeEntry.getKey();
                    RBAC.Strategy strategy = privilegeEntry.getValue();

                    targetPrivileges.merge(privilege, strategy, (existingStrategy, newStrategy) -> {
                        if (existingStrategy == RBAC.Strategy.ALL || newStrategy == RBAC.Strategy.ALL) {
                            return RBAC.Strategy.ALL;
                        }
                        return existingStrategy;
                    });
                }
                return targetPrivileges;
            });
        }
    }


}
