package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public record RBACServiceImpl(Map<RBACConfiguration.RoleName, ApplicationAccessPrivileges> applicationAccessPrivilegesByRoles, StampChecker stampChecker) implements RBACService {

    @Autowired
    public RBACServiceImpl(RBACConfiguration configuration, StampChecker stampChecker) {
        this(configuration.toMapOfApplicationAccessPrivilegesByRoles(), stampChecker);
    }

    @Override
    public CheckAccessPrivilege computeRbac(List<RBACConfiguration.RoleName> roles) {
        return new CheckAccessPrivilege(roles.stream()
                .map(applicationAccessPrivilegesByRoles::get)
                .reduce(ApplicationAccessPrivileges::merge)
                .orElse(ApplicationAccessPrivileges.NO_PRIVILEGE),
                this.stampChecker);
    }
}
