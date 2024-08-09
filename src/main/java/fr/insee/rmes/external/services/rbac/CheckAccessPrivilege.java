package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

import java.util.Map;

public record CheckAccessPrivilege(ApplicationAccessPrivileges applicationAccessPrivileges, StampChecker stampChecker) {

    public AccessPrivilegeChecker isGranted(RBAC.Privilege privilege) {
        return new AccessPrivilegeChecker(applicationAccessPrivileges, stampChecker, privilege);
    }
}
