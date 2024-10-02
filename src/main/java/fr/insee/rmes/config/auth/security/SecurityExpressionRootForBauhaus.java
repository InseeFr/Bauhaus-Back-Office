package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesRuntimeBadRequestException;
import fr.insee.rmes.external.services.rbac.CheckAccessPrivilegeForUser;
import fr.insee.rmes.external.services.rbac.AuthorizationChecker;
import fr.insee.rmes.external.services.rbac.RBACService;
import fr.insee.rmes.model.rbac.Module;
import fr.insee.rmes.model.rbac.Privilege;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

public class SecurityExpressionRootForBauhaus implements MethodSecurityExpressionOperations, SecurityExpressionOperations {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExpressionRootForBauhaus.class);

    private final MethodSecurityExpressionOperations methodSecurityExpressionOperations;
    private final SecurityExpressionRoot methodSecurityExpressionRoot;
    private final RBACService rbacService;
    private final UserDecoder userDecoder;
    private final AuthorizationChecker authorizationChecker;


    public SecurityExpressionRootForBauhaus(MethodSecurityExpressionOperations methodSecurityExpressionOperations, RBACService rbacService, UserDecoder userDecoder, AuthorizationChecker authorizationChecker) {
        this.methodSecurityExpressionRoot = (SecurityExpressionRoot) methodSecurityExpressionOperations;
        this.methodSecurityExpressionOperations = methodSecurityExpressionOperations;
        this.rbacService = rbacService;
        this.userDecoder = userDecoder;
        this.authorizationChecker = authorizationChecker;
    }

    public static MethodSecurityExpressionOperations enrich(MethodSecurityExpressionOperations methodSecurityExpressionOperations, RBACService rbacService, UserDecoder userDecoder, AuthorizationChecker authorizationChecker) {
        return new SecurityExpressionRootForBauhaus(requireNonNull(methodSecurityExpressionOperations), requireNonNull(rbacService), requireNonNull(userDecoder),  requireNonNull(authorizationChecker));
    }

    @Override
    public Authentication getAuthentication() {
        return this.methodSecurityExpressionRoot.getAuthentication();
    }

    @Override
    public boolean hasAuthority(String authority) {
        return this.methodSecurityExpressionRoot.hasAuthority(authority);
    }

    @Override
    public boolean hasAnyAuthority(String... authorities) {
        return this.methodSecurityExpressionRoot.hasAnyAuthority(authorities);
    }

    @Override
    public boolean hasRole(String role) {
        return this.methodSecurityExpressionRoot.hasRole(role);
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        return this.methodSecurityExpressionRoot.hasAnyRole(roles);
    }

    @Override
    public boolean permitAll() {
        return this.methodSecurityExpressionRoot.permitAll();
    }

    @Override
    public boolean denyAll() {
        return this.methodSecurityExpressionRoot.denyAll();
    }

    @Override
    public boolean isAnonymous() {
        return this.methodSecurityExpressionRoot.isAnonymous();
    }

    @Override
    public boolean isAuthenticated() {
        return this.methodSecurityExpressionRoot.isAuthenticated();
    }

    @Override
    public boolean isRememberMe() {
        return this.methodSecurityExpressionRoot.isRememberMe();
    }

    @Override
    public boolean isFullyAuthenticated() {
        return this.methodSecurityExpressionRoot.isFullyAuthenticated();
    }

    @Override
    public boolean hasPermission(Object target, Object permission) {
        return this.methodSecurityExpressionRoot.hasPermission(target, permission);
    }

    @Override
    public boolean hasPermission(Object targetId, String targetType, Object permission) {
        return this.methodSecurityExpressionRoot.hasPermission(targetId, targetType, permission);
    }

    @Override
    public void setFilterObject(Object filterObject) {
        methodSecurityExpressionOperations.setFilterObject(filterObject);
    }

    @Override
    public Object getFilterObject() {
        return methodSecurityExpressionOperations.getFilterObject();
    }

    @Override
    public void setReturnObject(Object returnObject) {
        methodSecurityExpressionOperations.setReturnObject(returnObject);
    }

    @Override
    public Object getReturnObject() {
        return methodSecurityExpressionOperations.getReturnObject();
    }

    @Override
    public Object getThis() {
        return methodSecurityExpressionOperations.getThis();
    }

    public boolean isAdmin() {
        logger.trace("Check if {} is admin", methodSecurityExpressionRoot.getPrincipal());
        return hasRole(Roles.ADMIN);
    }

    public boolean isDatasetContributor() {
        logger.trace("Check if {} is dataset contributor", methodSecurityExpressionRoot.getPrincipal());
        return hasRole(Roles.DATASET_CONTRIBUTOR);
    }

    public boolean isDatasetContributorWithStamp(String datasetId) {
        logger.trace("Check if {} is contributor for dataset {}", methodSecurityExpressionRoot.getPrincipal(), datasetId);
        return isDatasetContributor() && isManagerForDatasetId(datasetId);
    }

    public boolean isDistributionContributorWithStamp(String distributionId) {
        logger.trace("Check if {} is contributor for distribution {}", methodSecurityExpressionRoot.getPrincipal(), distributionId);
        return isDatasetContributor() && isManagerForDistributionId(distributionId);
    }

    public boolean isContributorOfSerie(String seriesId) {
        logger.trace("Check if {} is contributor for serie {}", methodSecurityExpressionRoot.getPrincipal(), seriesId);
        return hasRole(Roles.SERIES_CONTRIBUTOR) && isManagerForSerieId(seriesId);
    }

    //    for PUT and DELETE CodesList
    public boolean isContributorOfCodesList(String codesListId) {
        logger.trace("Check if {} is contributor for codes list {}", methodSecurityExpressionRoot.getPrincipal(), codesListId);
        return hasRole(Roles.CODESLIST_CONTRIBUTOR) && isManagerForCodesListId(codesListId);
    }

    //    for POST CodesList
    public boolean isCodesListContributor(String body) {
        logger.trace("Check if {} can create the codes list", methodSecurityExpressionRoot.getPrincipal());
        return hasRole(Roles.CODESLIST_CONTRIBUTOR) && checkStampIsContributor(body);
    }

    private boolean checkStampIsContributor(String body) {
        Optional<Stamp> stamp = getStamp();
        return stamp.isPresent() && stamp.get().stamp().equalsIgnoreCase(extractContributorStampFromBody(body));
    }

    private static @Nullable String extractContributorStampFromBody(String body) {
        return (new JSONObject(body)).optString("contributor");
    }

    //for PUT and DELETE structure
    public boolean isStructureContributor(String structureId) {
        logger.trace("Check if {} is contributor for structure {}", methodSecurityExpressionRoot.getPrincipal(), structureId);
        return hasRole(Roles.STRUCTURES_CONTRIBUTOR) && isManagerForStructureId(structureId);
    }

    //  for POST structure or component
    public boolean isStructureAndComponentContributor(String body) {
        logger.trace("Check if {} can create the structure or component", methodSecurityExpressionRoot.getPrincipal());
        return hasRole(Roles.STRUCTURES_CONTRIBUTOR) && checkStampIsContributor(body);
    }


    //for PUT and DELETE component
    public boolean isComponentContributor(String componentId) {
        logger.trace("Check if {} is contributor for component {}", methodSecurityExpressionRoot.getPrincipal(), componentId);
        return hasRole(Roles.STRUCTURES_CONTRIBUTOR) && isManagerForComponentId(componentId);
    }

    private boolean userHasStampWichManageResource(String resourceId, BiPredicate<String, Stamp> stampIsManager) {
        if (resourceId == null) {
            throw new RmesRuntimeBadRequestException("id must be not null");
        }
        var stamp = getStamp();
        return stamp.isPresent() && stampIsManager.test(resourceId, stamp.get());
    }


    private boolean isManagerForSerieId(String seriesId) {
        return userHasStampWichManageResource(seriesId, this.authorizationChecker::isSeriesManagerWithStamp);
    }

    private boolean isManagerForCodesListId(String codesListId) {
        return userHasStampWichManageResource(codesListId, this.authorizationChecker::isCodesListManagerWithStamp);
    }

    private boolean isManagerForDatasetId(String datasetId) {
        return userHasStampWichManageResource(datasetId, this.authorizationChecker::isDatasetManagerWithStamp);
    }

    private boolean isManagerForDistributionId(String distributionId) {
        return userHasStampWichManageResource(distributionId, this.authorizationChecker::isDistributionManagerWithStamp);
    }

    private boolean isManagerForStructureId(String structureId) {
        return userHasStampWichManageResource(structureId, this.authorizationChecker::isStructureManagerWithStamp);
    }

    private boolean isManagerForComponentId(String componentId) {
        return userHasStampWichManageResource(componentId, this.authorizationChecker::isComponentManagerWithStamp);
    }

    private Optional<Stamp> getStamp() {
        return getUser().map(User::stamp);
    }

    private Optional<User> getUser() {
        Object principal = methodSecurityExpressionRoot.getPrincipal();
        try {
            return userDecoder.fromPrincipal(principal);
        } catch (RmesException e) {
            logger.error("Unable to convert principal " + principal + " to User", e);
            return Optional.empty();
        }
    }


    public boolean canUpdateSerie(String serieId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.UPDATE).on(Module.SERIE).withId(serieId);
    }

    public boolean canDeleteDataset(String datasetId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.DELETE).on(Module.DATASET).withId(datasetId);
    }

    public boolean canUpdateDataset(String datasetId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.UPDATE).on(Module.DATASET).withId(datasetId);
    }

    public boolean canCreateDataset(String datasetId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.CREATE).on(Module.DATASET).withId(datasetId);
    }

    public boolean canCreateDataset() {
        return getAccessPrivilegesForUser().isGranted(Privilege.CREATE).on(Module.DATASET).whateverIdIs();
    }

    public boolean canPublishDataset(String datasetId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.PUBLISH).on(Module.DATASET).withId(datasetId);
    }

    public boolean canReadDataset(String datasetId) {
        return getAccessPrivilegesForUser().isGranted(Privilege.READ).on(Module.DATASET).withId(datasetId);
    }

    public boolean canReadAllDataset(){
        return getAccessPrivilegesForUser().isGranted(Privilege.READ).on(Module.DATASET).withId(null);
    }

    private CheckAccessPrivilegeForUser getAccessPrivilegesForUser() {
        return new CheckAccessPrivilegeForUser(rbacService.computeRbac(this.getAuthentication().getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(SecurityExpressionRootForBauhaus::toRoleName)
                .toList()
        ),
                getUser().orElse(User.EMPTY_USER),
                this.authorizationChecker
        );
    }

    private static RBACConfiguration.RoleName toRoleName(GrantedAuthority grantedAuthority) {
        return new RBACConfiguration.RoleName(grantedAuthority.getAuthority());
    }

}