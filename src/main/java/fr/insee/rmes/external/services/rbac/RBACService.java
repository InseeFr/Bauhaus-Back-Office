package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.rbac.ApplicationAccessPrivileges;
import fr.insee.rmes.model.rbac.RBAC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public record RBACService(RBAC rbac){

    private static final Logger log = LoggerFactory.getLogger(RBACService.class);

    public ApplicationAccessPrivileges computeRbac(List<RBACConfiguration.RoleName> roles) {
        ApplicationAccessPrivileges applicationAccessPrivileges = roles.stream()
                .map(this::getApplicationAccessPrivileges)
                .reduce(ApplicationAccessPrivileges::merge)
                .orElse(ApplicationAccessPrivileges.NO_PRIVILEGE);
        log.atTrace().log(()->"Privileges computed for roles "+roles+" : "+applicationAccessPrivileges);
        return applicationAccessPrivileges;
    }

    private ApplicationAccessPrivileges getApplicationAccessPrivileges(RBACConfiguration.RoleName roleName) {
        return rbac.applicationAccessPrivilegesByRoles().getOrDefault(roleName, ApplicationAccessPrivileges.NO_PRIVILEGE);
    }
}
