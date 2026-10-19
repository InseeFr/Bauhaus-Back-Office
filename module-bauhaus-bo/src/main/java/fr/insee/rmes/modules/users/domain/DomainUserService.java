package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.Set;

public class DomainUserService implements UserService {
    private final UserDecoder userDecoder;
    private final RbacFetcher rbacFetcher;

    public DomainUserService(UserDecoder userDecoder, RbacFetcher rbacFetcher) {
        this.userDecoder = userDecoder;
        this.rbacFetcher = rbacFetcher;
    }

    @Override
    public Set<Stamp> findStampsFrom(Object principal) throws MissingUserInformationException {
        return this.userDecoder.fromPrincipal(principal).map(User::stamps).orElse(Set.of());
    }

    @Override
    public User getUser(Object principal) throws MissingUserInformationException {
        return userDecoder.fromPrincipal(principal).get();
    }

    @Override
    public Set<ModuleAccessPrivileges> computePrivileges(Object principal) throws MissingUserInformationException {
        var user = this.getUser(principal);
        return rbacFetcher.computePrivileges(user.roles(), user.source());
    }

    @Override
    public RBAC.Strategy computeStrategy(User user, RBAC.Module module, RBAC.Privilege privilege) {
        return rbacFetcher.computeStrategy(user.roles(), user.source(), module, privilege);
    }
}
