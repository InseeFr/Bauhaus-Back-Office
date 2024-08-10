package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public record RBACServiceImpl(Map<RBACConfiguration.RoleName, ApplicationAccessPrivileges> applicationAccessPrivilegesByRoles) implements RBACService {

    private static final Logger log = LoggerFactory.getLogger(RBACServiceImpl.class);

    @Autowired
    public RBACServiceImpl(RBACConfiguration configuration) {
        this(configuration.toMapOfApplicationAccessPrivilegesByRoles());
    }

    @Override
    public ApplicationAccessPrivileges computeRbac(List<RBACConfiguration.RoleName> roles) {
        ApplicationAccessPrivileges applicationAccessPrivileges = roles.stream()
                .map(this::getApplicationAccessPrivileges)
                .reduce(ApplicationAccessPrivileges::merge)
                .orElse(ApplicationAccessPrivileges.NO_PRIVILEGE);
        log.atTrace().log(()->"Privileges computed for roles "+roles+" : "+applicationAccessPrivileges);
        return applicationAccessPrivileges;
    }

    private ApplicationAccessPrivileges getApplicationAccessPrivileges(RBACConfiguration.RoleName roleName) {
        return applicationAccessPrivilegesByRoles.getOrDefault(roleName, ApplicationAccessPrivileges.NO_PRIVILEGE);
    }
}
