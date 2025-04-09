package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.rbac.RBACConfiguration;
import fr.insee.rmes.rbac.RBAC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RBACServiceImpl implements RBACService {

    private final RBACConfiguration configuration;

    public RBACServiceImpl(RBACConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> computeRbac(List<String> roles) {
        /*if(roles.isEmpty()){
            return Map.of();
        }

        Map<String, Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>>> rbac = configuration.getRbac();

        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> results = new HashMap<>();

        for (String role : roles) {
            Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> rolePrivileges = rbac.get(role);
            if (rolePrivileges != null) {
                mergePrivileges(results, rolePrivileges);
            }
        }

        return results;*/
        return Map.of();
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
