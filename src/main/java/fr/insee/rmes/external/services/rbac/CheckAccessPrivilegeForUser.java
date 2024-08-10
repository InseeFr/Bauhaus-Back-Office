package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.model.rbac.RBAC;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public record CheckAccessPrivilegeForUser(ApplicationAccessPrivileges applicationAccessPrivileges, User user, StampChecker stampChecker, AtomicReference<RBAC.Privilege> privilege, AtomicReference<RBAC.Module> module) {

    private static final Logger log = LoggerFactory.getLogger(CheckAccessPrivilegeForUser.class);

    public CheckAccessPrivilegeForUser(ApplicationAccessPrivileges applicationAccessPrivileges, User user, StampChecker stampChecker) {
        this(applicationAccessPrivileges, user, stampChecker, new AtomicReference<>(), new AtomicReference<>());
    }

    public CheckAccessPrivilegeForUser isGranted(RBAC.Privilege privilege) {
        withPrivilege(privilege);
        return this;
    }

    private void withPrivilege(RBAC.Privilege privilege) {
        this.privilege.set(privilege);
    }

    public CheckAccessPrivilegeForUser on(RBAC.Module module) {
        withModule(module);
        return this;
    }

    private void withModule(RBAC.Module module) {
        this.module.set(module);
    }

    public boolean withId(String id) {
        var strategy = applicationAccessPrivileges.privilegesForModule(module.get()).strategyFor(privilege.get());
        if (strategy.isEmpty()){
            log.atDebug().log(()->debugAccess()+" : no privilege found for "+ user.roles());
            return false;
        }
        if (strategy.get().isAllStampAuthorized()){
            log.atDebug().log(()->debugAccess()+" : ALL privilege found");
            return true;
        }
        boolean authorized = checkStampFor(id);
        log.atDebug().log(()->debugAccess()+" : STAMP privilege found : "+user.stamp()+" "+(authorized?"":"un")+"authorized for id"+id);
        return authorized;
    }

    private @NotNull String debugAccess() {
        return "Check access for " + user + " for " + privilege + " in " + module;
    }

    private boolean checkStampFor(String id) {
        return id != null && stampChecker.userStampIsAuthorizedForResource(module.get(), id, user.stamp());
    }
}
