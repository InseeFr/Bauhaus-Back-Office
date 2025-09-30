package fr.insee.rmes.rbac;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rbac.stamps.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service(value = "propertiesAccessPrivilegesChecker")
public class PropertiesAccessPrivilegesChecker implements AccessPrivilegesChecker {

    private final RbacFetcher fetcher;
    private final UserDecoder decoder;
    private final RepositoryGestion repositoryGestion;
    private final Environment env;

    public PropertiesAccessPrivilegesChecker(RbacFetcher fetcher, UserDecoder decoder, RepositoryGestion repositoryGestion, Environment env) {
        this.fetcher = fetcher;
        this.decoder = decoder;
        this.repositoryGestion = repositoryGestion;
        this.env = env;
    }

    @Override
    public boolean hasAccess(String module, String privilege, String id, Object principal) throws RmesException {
        var user = this.decoder.fromPrincipal(principal);
        return user.map(u -> hasAcccess(module, privilege, id, u)).orElse(false);

    }

    private boolean hasAcccess(String moduleIdentifer, String privilegeIdentifier, String id, User user) {
        var module = RBAC.Module.valueOf(moduleIdentifer);
        var privilege = RBAC.Privilege.valueOf(privilegeIdentifier);
        var moduleAccessPrivileges = findModuleAccessPrivileges(user, module);

        var privilegeAndStrategy = findStrategyByPrivilege(privilege, moduleAccessPrivileges);

        return authorizeFromStrategy(module, privilegeAndStrategy, id, user);
    }

    private boolean authorizeFromStrategy(RBAC.Module module, Optional<ModuleAccessPrivileges.Privilege> privilegeAndStrategy, String id, User user) {
        return privilegeAndStrategy.map(value -> switch (value.strategy()) {
            case ALL -> true;
            case STAMP -> {
                var stamps = getObjectStampChecker(module).getStamps(id);
                yield stamps.isEmpty() || stamps.contains(user.getStamp());
            }
            case NONE -> false;
        }).orElse(false);
    }

    private Optional<ModuleAccessPrivileges.Privilege> findStrategyByPrivilege(RBAC.Privilege privilege, Optional<ModuleAccessPrivileges> moduleAccessPrivileges) {
        return moduleAccessPrivileges.orElse(new ModuleAccessPrivileges(RBAC.Module.UNKNOWN, Set.of())).privileges().stream().filter(p -> p.privilege().equals(privilege)).findFirst();
    }

    private Optional<ModuleAccessPrivileges> findModuleAccessPrivileges(User user, RBAC.Module module) {
        var privileges = fetcher.computePrivileges(user.roles());
        return privileges.stream().filter(p -> p.application().equals(module)).findFirst();
    }

    private ObjectStampChecker getObjectStampChecker(RBAC.Module module) {
        return switch (module) {
            case OPERATION_SERIES -> new OperationSeriesStampChecker(this.repositoryGestion);
            case STRUCTURE_STRUCTURE -> new StructureStructureStampChecker(this.repositoryGestion);
            case STRUCTURE_COMPONENT -> new StructureComponentStampChecker(this.repositoryGestion);
            case DATASET_DATASET -> new DatasetDatasetStampChecker(this.env, this.repositoryGestion);
            case DATASET_DISTRIBUTION -> new DatasetDistributionStampChecker(this.repositoryGestion);
            default -> new DefaultStampChecker();
        };
    }
}
