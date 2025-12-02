package fr.insee.rmes.rbac;

import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.port.serverside.UserDecoder;
import fr.insee.rmes.domain.auth.User;
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
        var privilege = RBAC.Privilege.valueOf(privilegeIdentifier);

        if(user.source().equals(Source.INSEE) && privilege.equals(RBAC.Privilege.READ)){
            return true;
        }

        var module = RBAC.Module.valueOf(moduleIdentifer);
        var moduleAccessPrivileges = findModuleAccessPrivileges(user, module);

        var privilegeAndStrategy = findStrategyByPrivilege(privilege, moduleAccessPrivileges);

        return authorizeFromStrategy(module, privilegeAndStrategy, id, user);
    }

    private boolean authorizeFromStrategy(RBAC.Module module, Optional<ModuleAccessPrivileges.Privilege> privilegeAndStrategy, String id, User user) {
        return privilegeAndStrategy.map(value -> switch (value.strategy()) {
            case ALL -> {
                var privilege = privilegeAndStrategy.map(ModuleAccessPrivileges.Privilege::privilege).get();
;                if ((user.stamp() == null || user.stamp().stamp() == null) && (
                    privilege.equals(RBAC.Privilege.CREATE) ||
                    privilege.equals(RBAC.Privilege.UPDATE) ||
                    privilege.equals(RBAC.Privilege.DELETE) ||
                    privilege.equals(RBAC.Privilege.PUBLISH) ||
                    privilege.equals(RBAC.Privilege.ADMINISTRATION)
                )) {
                    yield false;
                }
                yield true;
            }
            case STAMP -> {
                if(user.stamp() == null){
                    yield false;
                }

                var stamps = getObjectStampChecker(module).getStamps(id);
                yield stamps.isEmpty() || stamps.contains(user.stamp().stamp());
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
