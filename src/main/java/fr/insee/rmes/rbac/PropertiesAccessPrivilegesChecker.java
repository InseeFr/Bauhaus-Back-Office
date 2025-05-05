package fr.insee.rmes.rbac;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.rbac.stamps.DefaultStampChecker;
import fr.insee.rmes.rbac.stamps.ObjectStampChecker;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service(value = "propertiesAccessPrivilegesChecker")
public class PropertiesAccessPrivilegesChecker implements AccessPrivilegesChecker {

    private final RbacFetcher fetcher;
    private final UserDecoder decoder;

    public PropertiesAccessPrivilegesChecker(RbacFetcher fetcher, UserDecoder decoder) {
        this.fetcher = fetcher;
        this.decoder = decoder;
    }

    @Override
    public boolean hasAccess(String module, String privilege, Object principal) throws RmesException {
        var user = this.decoder.fromPrincipal(principal);
        return user.map(u -> extracted(module, privilege, u)).orElse(false);

    }

    private boolean extracted(String moduleIdentifer, String privilegeIdentifier, User user) {
        var module = RBAC.Module.valueOf(moduleIdentifer);
        var privilege = RBAC.Privilege.valueOf(privilegeIdentifier);
        var moduleAccessPrivileges = findModuleAccessPrivileges(user, module);

        if(moduleAccessPrivileges.isEmpty()){
            return false;
        }

        var privilegeAndStrategy = findStrategyByPrivilege(privilege, moduleAccessPrivileges);

        return authorizeFromStrategy(module, privilegeAndStrategy, user);
    }

    private boolean authorizeFromStrategy(RBAC.Module module, Optional<ModuleAccessPrivileges.Privilege> privilegeAndStrategy, User user) {
        return privilegeAndStrategy.map(value -> switch (value.strategy()) {
            case ALL -> true;
            case STAMP -> getObjectStampChecker(module).getStamps().contains(user.getStamp());
            case NONE -> false;
        }).orElse(false);
    }

    private Optional<ModuleAccessPrivileges.Privilege> findStrategyByPrivilege(RBAC.Privilege privilege, Optional<ModuleAccessPrivileges> moduleAccessPrivileges) {
        return moduleAccessPrivileges.get().privileges().stream().filter(p -> p.privilege().equals(privilege)).findFirst();
    }

    private Optional<ModuleAccessPrivileges> findModuleAccessPrivileges(User user, RBAC.Module module) {
        var privileges = fetcher.computePrivileges(user.roles());
        return privileges.stream().filter(p -> p.application().equals(module)).findFirst();
    }

    private ObjectStampChecker getObjectStampChecker(RBAC.Module module) {
        return switch (module) {
            default -> new DefaultStampChecker();
        };
    }
}
