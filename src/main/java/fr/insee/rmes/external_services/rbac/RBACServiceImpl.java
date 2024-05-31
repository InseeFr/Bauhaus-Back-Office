package fr.insee.rmes.external_services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.RBAC;
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
    public Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> computeRbac(List<String> roles) {
        if(roles.isEmpty()){
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

        return results;
    }

    private void mergePrivileges(Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> target,
                                 Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> source) {
        for (Map.Entry<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> entry : source.entrySet()) {
            RBAC.APPLICATION app = entry.getKey();
            Map<RBAC.PRIVILEGE, RBAC.STRATEGY> sourcePrivileges = entry.getValue();

            target.merge(app, new HashMap<>(sourcePrivileges), (targetPrivileges, newPrivileges) -> {
                for (Map.Entry<RBAC.PRIVILEGE, RBAC.STRATEGY> privilegeEntry : newPrivileges.entrySet()) {
                    RBAC.PRIVILEGE privilege = privilegeEntry.getKey();
                    RBAC.STRATEGY strategy = privilegeEntry.getValue();

                    targetPrivileges.merge(privilege, strategy, (existingStrategy, newStrategy) -> {
                        if (existingStrategy == RBAC.STRATEGY.ALL || newStrategy == RBAC.STRATEGY.ALL) {
                            return RBAC.STRATEGY.ALL;
                        }
                        return existingStrategy;
                    });
                }
                return targetPrivileges;
            });
        }
    }
}
