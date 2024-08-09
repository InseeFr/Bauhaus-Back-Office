package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

public record AccessPrivilegeChecker(ApplicationAccessPrivileges applicationAccessPrivileges, StampChecker stampChecker, RBAC.Privilege privilege, RBAC.Module module) {

    public AccessPrivilegeChecker(ApplicationAccessPrivileges applicationAccessPrivileges, StampChecker stampChecker, RBAC.Privilege privilege) {
        this(applicationAccessPrivileges, stampChecker, privilege, null);
    }

    public AccessPrivilegeChecker on(RBAC.Module module) {
        return withModule(module);
    }

    private AccessPrivilegeChecker withModule(RBAC.Module module) {
        return new AccessPrivilegeChecker(this.applicationAccessPrivileges, this.stampChecker, this.privilege, module);
    }

    public boolean withId(String id) {
        var strategy = applicationAccessPrivileges.privilegesForModule(module).strategyFor(privilege);
        if (strategy.isEmpty()){
            return false;
        }
        return strategy.get().isAllStampAuthorized() || checkStampFor(id);
    }

    private boolean checkStampFor(String id) {
        return id != null && stampChecker.userStampIsAuthorizedForResource(module, id);
    }


}
