package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.model.rbac.ApplicationAccessPrivileges;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.model.rbac.Privilege;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public record CheckAccessPrivilegeForUser(ApplicationAccessPrivileges applicationAccessPrivileges, User user, AuthorizationChecker authorizationChecker, AtomicReference<Privilege> privilege, AtomicReference<Module> module) {

    private static final Logger log = LoggerFactory.getLogger(CheckAccessPrivilegeForUser.class);

    public CheckAccessPrivilegeForUser(ApplicationAccessPrivileges applicationAccessPrivileges, User user, AuthorizationChecker authorizationChecker) {
        this(applicationAccessPrivileges, user, authorizationChecker, new AtomicReference<>(), new AtomicReference<>());
    }

    public CheckAccessPrivilegeForUser isGranted(Privilege privilege) {
        withPrivilege(privilege);
        return this;
    }

    private void withPrivilege(Privilege privilege) {
        this.privilege.set(privilege);
    }

    public CheckAccessPrivilegeForUser on(Module module) {
        withModule(module);
        return this;
    }

    private void withModule(Module module) {
        this.module.set(module);
    }

    public boolean withId(String id) {
        return check(Optional.ofNullable(id), this::checkStampFor);
    }

    private boolean check(Optional<String> id, Predicate<String> idChecker) {
        var strategy = applicationAccessPrivileges.privilegesForModule(module.get()).strategyFor(privilege.get());
        if (strategy.isEmpty()){
            log.atDebug().log(()->debugAccess()+" : no privilege found for "+ user.roles());
            return false;
        }
        if (strategy.get().isAllStampAuthorized()){
            log.atDebug().log(()->debugAccess()+" : ALL privilege found");
            return true;
        }
        boolean authorized = id.isPresent() && idChecker.test(id.get());
        log.atDebug().log(()->debugAccess()+" : STAMP privilege found : "+user.stamp()+" "+(authorized?"":"un")+"authorized for id"+ id);
        return authorized;
    }

    public boolean whateverIdIs() {
        Predicate<String> noIdCheck = ignored -> false;
        return check(Optional.empty(), noIdCheck);
    }

    private String debugAccess() {
        return "Check access for " + user + " for " + privilege + " in " + module;
    }

    private boolean checkStampFor(String id) {
        return id != null && authorizationChecker.userStampIsAuthorizedForResource(module.get(), id, user.stamp());
    }

}
