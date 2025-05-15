package fr.insee.rmes.config.auth.security;

import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import static java.util.Objects.requireNonNull;

public class SecurityExpressionRootForBauhaus implements MethodSecurityExpressionOperations, SecurityExpressionOperations {

    private final MethodSecurityExpressionOperations methodSecurityExpressionOperations;
    private final SecurityExpressionRoot methodSecurityExpressionRoot;


    private SecurityExpressionRootForBauhaus(
            MethodSecurityExpressionOperations methodSecurityExpressionOperations,
            StampFromPrincipal stampFromPrincipal) {
        this.methodSecurityExpressionRoot = (SecurityExpressionRoot) methodSecurityExpressionOperations;
        this.methodSecurityExpressionOperations = methodSecurityExpressionOperations;
    }

    public static MethodSecurityExpressionOperations enrich(MethodSecurityExpressionOperations methodSecurityExpressionOperations, StampFromPrincipal stampFromPrincipal) {
        return new SecurityExpressionRootForBauhaus(requireNonNull(methodSecurityExpressionOperations),  requireNonNull(stampFromPrincipal));
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
    public boolean hasPermission(Object module, Object permission) {
        return this.methodSecurityExpressionRoot.hasPermission(module,permission);
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




/*
    private boolean isManagerForCodesListId(String codesListId) {
        return userHasStampWichManageResource(codesListId, this.stampAuthorizationChecker::isCodesListManagerWithStamp);
    }*/


}
