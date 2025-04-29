package fr.insee.rmes.rbac;

import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.rbac.stamps.DefaultStampChecker;
import fr.insee.rmes.rbac.stamps.ObjectStampChecker;
import org.springframework.stereotype.Service;

@Service
public class PropertiesAccessPrivilegesChecker implements AccessPrivilegesChecker {

    private final RbacFetcher fetcher;
    private final UserProvider userProvider;

    public PropertiesAccessPrivilegesChecker(RbacFetcher fetcher, UserProvider userProvider) {
        this.fetcher = fetcher;
        this.userProvider = userProvider;
    }

    @Override
    public boolean hasAccess(RBAC.Module module, RBAC.Privilege privilege) {
        var user = userProvider.findUserDefaultToEmpty();
        var privileges = fetcher.computePrivileges(user.roles());
        var moduleAccessPrivileges = privileges.stream().filter(p -> p.application().equals(module)).findFirst();

        if(moduleAccessPrivileges.isEmpty()){
            return false;
        }

        var privilegeAndStrategy = moduleAccessPrivileges.get().privileges().stream().filter(p -> p.privilege().equals(privilege)).findFirst();

        return privilegeAndStrategy.map(value -> switch (value.strategy()) {
            case ALL -> true;
            case STAMP -> getObjectStampChecker(module).getStamps().contains(user.getStamp());
            case NONE -> false;
        }).orElse(false);

    }

    private ObjectStampChecker getObjectStampChecker(RBAC.Module module) {
        return switch (module) {
            default -> new DefaultStampChecker();
        };
    }
}
