package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DomainAccessPrivilegesChecker implements AccessPrivilegesCheckerService {
    static final Logger logger = LoggerFactory.getLogger(DomainAccessPrivilegesChecker.class);

    private final RbacFetcher fetcher;
    private final UserDecoder decoder;
    private final StampChecker infrastructureStampChecker;
    public DomainAccessPrivilegesChecker(RbacFetcher fetcher, UserDecoder decoder, StampChecker infrastructureStampChecker) {
        this.fetcher = fetcher;
        this.decoder = decoder;
        this.infrastructureStampChecker = infrastructureStampChecker;
    }

    @Override
    public boolean hasAccess(String module, String privilege, String id, Object principal) throws MissingUserInformationException {
        var user = this.decoder.fromPrincipal(principal);
        return user.map(u -> hasAcccess(module, privilege, id, u)).orElse(false);

    }

    private boolean hasAcccess(String moduleIdentifer, String privilegeIdentifier, String id, User user) {
        RBAC.Privilege privilege;
        RBAC.Module module;

        try {
            privilege = RBAC.Privilege.valueOf(privilegeIdentifier);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid privilege identifier: {}", privilegeIdentifier, e);
            return false;
        }

        if(Source.INSEE.equals(user.source()) && privilege.equals(RBAC.Privilege.READ)){
            return true;
        }

        try {
            module = RBAC.Module.valueOf(moduleIdentifer);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid module identifier: {}", moduleIdentifer, e);
            return false;
        }

        var moduleAccessPrivileges = findModuleAccessPrivileges(user, module);

        var privilegeAndStrategy = findStrategyByPrivilege(privilege, moduleAccessPrivileges);

        return privilegeAndStrategy.map((privilegeAndStrategyValue) -> this.authorizeFromStrategy(module, privilegeAndStrategyValue, id, user)).orElse(false);
    }

    private boolean authorizeFromStrategy(RBAC.Module module, ModuleAccessPrivileges.Privilege privilegeAndStrategy, String id, User user) {
        return switch (privilegeAndStrategy.strategy()) {
            case ALL -> {

                var privilege = privilegeAndStrategy.privilege();
                 if (user.getStamps().isEmpty() && (
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
                if(user.getStamps().isEmpty()){
                    yield false;
                }

                List<String> stamps = null;
                try {
                    stamps = getStamps(module, id);
                } catch (StampFetchException | UnsupportedModuleException e) {
                    logger.error("Could not fetch stamp for %s on the resource %s inside the module %s. So, this user is not allowed to do this action %s".formatted(user, id, module, privilegeAndStrategy.privilege()), e);
                    yield false;
                }
                yield stamps.isEmpty() || stamps.stream().anyMatch(user.getStamps()::contains);
            }
            case NONE -> false;
        };
    }

    private Optional<ModuleAccessPrivileges.Privilege> findStrategyByPrivilege(RBAC.Privilege privilege, Optional<ModuleAccessPrivileges> moduleAccessPrivileges) {
        return moduleAccessPrivileges.orElse(new ModuleAccessPrivileges(RBAC.Module.UNKNOWN, Set.of())).privileges().stream().filter(p -> p.privilege().equals(privilege)).findFirst();
    }

    private Optional<ModuleAccessPrivileges> findModuleAccessPrivileges(User user, RBAC.Module module) {
        var privileges = fetcher.computePrivileges(user.roles());
        return privileges.stream().filter(p -> p.application().equals(module)).findFirst();
    }

    private List<String> getStamps(RBAC.Module module, String id) throws StampFetchException, UnsupportedModuleException {
        return switch (module) {
            case OPERATION_SERIES ->  this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, id);

            case STRUCTURE_STRUCTURE -> this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, id);
            case STRUCTURE_COMPONENT -> this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.STRUCTURE_COMPONENT, id);

            case DATASET_DATASET -> this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.DATASET_DATASET, id);
            case DATASET_DISTRIBUTION -> this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, id);
            default -> Collections.emptyList();
        };
    }
}
