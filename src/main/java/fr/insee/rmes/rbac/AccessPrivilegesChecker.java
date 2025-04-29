package fr.insee.rmes.rbac;

public interface AccessPrivilegesChecker {

    boolean hasAccess(RBAC.Module module, RBAC.Privilege privilege);
}
