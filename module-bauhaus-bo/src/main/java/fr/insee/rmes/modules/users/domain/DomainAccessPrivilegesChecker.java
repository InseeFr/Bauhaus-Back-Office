package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.clientside.AccessPrivilegesCheckerService;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainAccessPrivilegesChecker implements AccessPrivilegesCheckerService {
    static final Logger logger = LoggerFactory.getLogger(DomainAccessPrivilegesChecker.class);

    private final RbacFetcher fetcher;
    private final UserDecoder decoder;
    private final StampChecker infrastructureStampChecker;

    public DomainAccessPrivilegesChecker(
            RbacFetcher fetcher, UserDecoder decoder, StampChecker infrastructureStampChecker) {
        this.fetcher = fetcher;
        this.decoder = decoder;
        this.infrastructureStampChecker = infrastructureStampChecker;
    }

    @Override
    public boolean hasAccess(String module, String privilege, String id, Object principal)
            throws MissingUserInformationException {
        logger.debug(
                "Checking access: module={}, privilege={}, id={}, principalType={}",
                module,
                privilege,
                id,
                principal == null ? null : principal.getClass().getName());
        var user = this.decoder.fromPrincipal(principal);
        if (user.isEmpty()) {
            logger.debug("Access denied: no user could be decoded from the principal");
            return false;
        }
        var granted = hasAcccess(module, privilege, id, user.get());
        logger.debug(
                "Access {} for user {} on module={}, privilege={}, id={}",
                granted ? "granted" : "denied",
                user.get().id(),
                module,
                privilege,
                id);
        return granted;
    }

    @Override
    public boolean hasCreateAccess(String module, Collection<String> owners, Object principal)
            throws MissingUserInformationException {
        var user = this.decoder.fromPrincipal(principal);
        if (user.isEmpty()) {
            logger.debug("Creation denied: no user could be decoded from the principal");
            return false;
        }
        var granted = hasCreateAccess(module, owners, user.get());
        logger.debug(
                "Creation {} for user {} on module={} with owners {}",
                granted ? "granted" : "denied",
                user.get().id(),
                module,
                owners);
        return granted;
    }

    /**
     * Le contrôle par timbre d'une création porte sur le corps de la requête : aucune ressource
     * n'existe encore pour relire ses propriétaires en base. Contrairement à une ressource
     * existante sans propriétaire, une création sans propriétaire est refusée : la ressource
     * échapperait ensuite à tout contrôle par timbre.
     */
    private boolean hasCreateAccess(String moduleIdentifier, Collection<String> owners, User user) {
        RBAC.Module module;
        try {
            module = RBAC.Module.valueOf(moduleIdentifier);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid module identifier: {}", moduleIdentifier, e);
            return false;
        }

        var privilegeAndStrategy =
                findStrategyByPrivilege(RBAC.Privilege.CREATE, findModuleAccessPrivileges(user, module));
        if (privilegeAndStrategy.isEmpty()) {
            logger.debug("Creation denied: roles {} grant no CREATE privilege on module {}", user.roles(), module);
            return false;
        }

        return switch (privilegeAndStrategy.get().strategy()) {
            case ALL -> !user.getStamps().isEmpty();
            case STAMP -> {
                var postedOwners = owners == null
                        ? List.<String>of()
                        : owners.stream().filter(Objects::nonNull).toList();
                if (postedOwners.isEmpty()) {
                    logger.debug("Creation denied: strategy STAMP but no owner in the request");
                    yield false;
                }
                var ownersStamps = infrastructureStampChecker.toStamps(postedOwners);
                logger.debug(
                        "Strategy STAMP: owners stamps {}, user {} has stamps {}",
                        ownersStamps,
                        user.id(),
                        user.getStamps());
                yield ownersStamps.stream().anyMatch(user.getStamps()::contains);
            }
            case NONE -> false;
        };
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

        try {
            module = RBAC.Module.valueOf(moduleIdentifer);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid module identifier: {}", moduleIdentifer, e);
            return false;
        }

        logger.debug(
                "User {} has roles {}, stamps {}, source {}", user.id(), user.roles(), user.getStamps(), user.source());

        var moduleAccessPrivileges = findModuleAccessPrivileges(user, module);
        if (moduleAccessPrivileges.isEmpty()) {
            logger.debug("Access denied: roles {} grant no privilege on module {}", user.roles(), module);
            return false;
        }
        logger.debug("Privileges of user {} on module {}: {}", user.id(), module, moduleAccessPrivileges.get());

        var privilegeAndStrategy = findStrategyByPrivilege(privilege, moduleAccessPrivileges);
        if (privilegeAndStrategy.isEmpty()) {
            logger.debug("Access denied: roles {} grant no {} privilege on module {}", user.roles(), privilege, module);
            return false;
        }
        logger.debug(
                "Strategy for privilege {} on module {}: {}",
                privilege,
                module,
                privilegeAndStrategy.get().strategy());

        return privilegeAndStrategy
                .map((privilegeAndStrategyValue) ->
                        this.authorizeFromStrategy(module, privilegeAndStrategyValue, id, user))
                .orElse(false);
    }

    private boolean authorizeFromStrategy(
            RBAC.Module module, ModuleAccessPrivileges.Privilege privilegeAndStrategy, String id, User user) {
        return switch (privilegeAndStrategy.strategy()) {
            case ALL -> {
                var privilege = privilegeAndStrategy.privilege();
                if (user.getStamps().isEmpty()
                        && (privilege.equals(RBAC.Privilege.CREATE)
                                || privilege.equals(RBAC.Privilege.UPDATE)
                                || privilege.equals(RBAC.Privilege.DELETE)
                                || privilege.equals(RBAC.Privilege.PUBLISH)
                                || privilege.equals(RBAC.Privilege.ADMINISTRATION))) {
                    logger.debug(
                            "Access denied: strategy ALL but user {} has no stamp, required for privilege {}",
                            user.id(),
                            privilege);
                    yield false;
                }

                yield true;
            }
            case STAMP -> {
                if (user.getStamps().isEmpty()) {
                    logger.debug("Access denied: strategy STAMP but user {} has no stamp", user.id());
                    yield false;
                }

                List<String> stamps = null;
                try {
                    stamps = getStamps(module, id);
                } catch (StampFetchException | UnsupportedModuleException e) {
                    logger.error(
                            "Could not fetch stamp for %s on the resource %s inside the module %s. So, this user is not allowed to do this action %s"
                                    .formatted(user, id, module, privilegeAndStrategy.privilege()),
                            e);
                    yield false;
                }
                logger.debug(
                        "Strategy STAMP: resource {} of module {} has stamps {}, user {} has stamps {}",
                        id,
                        module,
                        stamps,
                        user.id(),
                        user.getStamps());
                if (stamps.isEmpty()) {
                    logger.debug("Strategy STAMP: resource {} has no stamp, access granted", id);
                    yield true;
                }
                yield stamps.stream().anyMatch(user.getStamps()::contains);
            }
            case NONE -> {
                logger.debug("Access denied: strategy NONE");
                yield false;
            }
        };
    }

    private Optional<ModuleAccessPrivileges.Privilege> findStrategyByPrivilege(
            RBAC.Privilege privilege, Optional<ModuleAccessPrivileges> moduleAccessPrivileges) {
        return moduleAccessPrivileges
                .orElse(new ModuleAccessPrivileges(RBAC.Module.UNKNOWN, Set.of()))
                .privileges()
                .stream()
                .filter(p -> p.privilege().equals(privilege))
                .findFirst();
    }

    private Optional<ModuleAccessPrivileges> findModuleAccessPrivileges(User user, RBAC.Module module) {
        var privileges = fetcher.computePrivileges(user.roles(), user.source());
        return privileges.stream().filter(p -> p.application().equals(module)).findFirst();
    }

    private List<String> getStamps(RBAC.Module module, String id)
            throws StampFetchException, UnsupportedModuleException {
        return switch (module) {
            case OPERATION_FAMILY ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.OPERATION_FAMILY, id);
            case OPERATION_SERIES ->
                this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, id);
            case OPERATION_OPERATION ->
                this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.OPERATION_OPERATION, id);
            case OPERATION_INDICATOR ->
                this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.OPERATION_INDICATOR, id);
            case OPERATION_SIMS -> this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SIMS, id);
            case DDI_PHYSICALINSTANCE ->
                this.infrastructureStampChecker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, id);

            case STRUCTURE_STRUCTURE ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, id);
            case STRUCTURE_COMPONENT ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.STRUCTURE_COMPONENT, id);

            case CLASSIFICATION_CLASSIFICATION ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.CLASSIFICATION_CLASSIFICATION, id);

            case CODESLIST_CODESLIST ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.CODESLIST_CODESLIST, id);
            case CODESLIST_PARTIALCODESLIST ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.CODESLIST_PARTIALCODESLIST, id);

            case CONCEPT_CONCEPT ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.CONCEPT_CONCEPT, id);
            case CONCEPT_COLLECTION ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.CONCEPT_COLLECTION, id);

            case DATASET_DATASET ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.DATASET_DATASET, id);
            case DATASET_DISTRIBUTION ->
                this.infrastructureStampChecker.getContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, id);
            default -> Collections.emptyList();
        };
    }
}
