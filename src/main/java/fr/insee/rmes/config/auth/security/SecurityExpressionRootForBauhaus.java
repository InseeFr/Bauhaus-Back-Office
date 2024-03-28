package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.config.auth.roles.Roles;
import fr.insee.rmes.config.auth.user.Stamp;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SecurityExpressionRootForBauhaus implements MethodSecurityExpressionOperations, SecurityExpressionOperations {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExpressionRootForBauhaus.class);

    private final MethodSecurityExpressionOperations methodSecurityExpressionOperations;
    private final StampAuthorizationChecker stampAuthorizationChecker;
    private final StampFromPrincipal stampFromPrincipal;
    private final SecurityExpressionRoot methodSecurityExpressionRoot;

    private SecurityExpressionRootForBauhaus(MethodSecurityExpressionOperations methodSecurityExpressionOperations, StampAuthorizationChecker stampAuthorizationChecker, StampFromPrincipal stampFromPrincipal) {
        this.methodSecurityExpressionRoot = (SecurityExpressionRoot) methodSecurityExpressionOperations;
        this.methodSecurityExpressionOperations = methodSecurityExpressionOperations;
        this.stampAuthorizationChecker = stampAuthorizationChecker;
        this.stampFromPrincipal = stampFromPrincipal;
    }

    public static MethodSecurityExpressionOperations enrich(MethodSecurityExpressionOperations methodSecurityExpressionOperations, StampAuthorizationChecker stampAuthorizationChecker, StampFromPrincipal stampFromPrincipal) {
        return new SecurityExpressionRootForBauhaus(requireNonNull(methodSecurityExpressionOperations), requireNonNull(stampAuthorizationChecker), requireNonNull(stampFromPrincipal));
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
        return this.methodSecurityExpressionRoot.hasPermission(target,permission);
    }

    @Override
    public boolean hasPermission(Object targetId, String targetType, Object permission) {
        return this.methodSecurityExpressionRoot.hasPermission(targetId,targetType,permission);
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

    public boolean isDatasetContributorWithStamp(String datasetId){
        logger.trace("Check if {} is contributor for dataset {}", methodSecurityExpressionRoot.getPrincipal(), datasetId);
        return isDatasetContributor() && isManagerForDatasetId(datasetId);
    }

    public boolean isDistributionContributorWithStamp(String distributionId){
        logger.trace("Check if {} is contributor for distribution {}", methodSecurityExpressionRoot.getPrincipal(), distributionId);
        return isDatasetContributor() && isManagerForDistributionId(distributionId);
    }

    public boolean isContributorOfSerie(String seriesId) {
        logger.trace("Check if {} is contributor for serie {}", methodSecurityExpressionRoot.getPrincipal(), seriesId);
        return hasRole(Roles.SERIES_CONTRIBUTOR) && isManagerForSerieId(seriesId);
    }

//    for PUT
    public boolean isContributorOfCodesList(String codesListId){
        logger.trace("Check if {} is contributor for codes list {}", methodSecurityExpressionRoot.getPrincipal(), codesListId);
        return hasRole(Roles.CODESLIST_CONTRIBUTOR) && isManagerForCodesListId(codesListId);
    }

//    for POST
    public boolean isCodesListContributor(String contributorString) {

        logger.trace("Check if {} can create the codes list", methodSecurityExpressionRoot.getPrincipal());
        JSONObject contrib = new JSONObject(contributorString);
        if(!contrib.has("contributor")){
            return false;
        }
        String codesListContributor = contrib.getString("contributor");
        Optional<String> timbreUtilisateur=getStamp();
        boolean timbreOK = false;
        if (timbreUtilisateur.isPresent()) {
            timbreOK = codesListContributor.equals(timbreUtilisateur.get());
        }
        return hasRole(Roles.CODESLIST_CONTRIBUTOR) && timbreOK;
    }

//for DELETE
    public boolean isContributorOfUnpublishedCodesList(String codesListId) {
        logger.trace("Check if {} is contributor for codes list {} and give validation status", methodSecurityExpressionRoot.getPrincipal(), codesListId);
        return hasRole(Roles.CODESLIST_CONTRIBUTOR) && isManagerDeleteForUnpublishedCodesListId(codesListId);
    }
    private boolean isManagerForSerieId(String seriesId) {
        return getStamp().map(stamp -> this.stampAuthorizationChecker.isSeriesManagerWithStamp(requireNonNull(seriesId), stamp)).orElse(false);
    }

    private boolean isManagerForCodesListId(String codesListId) {
        return getStamp().map(stamp -> this.stampAuthorizationChecker.isCodesListManagerWithStamp(requireNonNull(codesListId), stamp)).orElse(false);
    }

    private boolean isManagerForDatasetId(String datasetId) {
        return getStamp().map(stamp -> this.stampAuthorizationChecker.isDatasetManagerWithStamp(requireNonNull(datasetId), stamp)).orElse(false);
    }
    private boolean isManagerForDistributionId(String distributionId) {
        return getStamp().map(stamp -> this.stampAuthorizationChecker.isDistributionManagerWithStamp(requireNonNull(distributionId), stamp)).orElse(false);
    }

    public boolean isManagerDeleteForUnpublishedCodesListId(String codesListId) {
        return getStamp().map(stamp -> this.stampAuthorizationChecker.isUnpublishedCodesListManagerWithStamp(requireNonNull(codesListId), stamp)).orElse(false);
    }

    private Optional<String> getStamp() {
        return this.stampFromPrincipal.findStamp(methodSecurityExpressionRoot.getPrincipal()).map(Stamp::stamp);
    }

}
