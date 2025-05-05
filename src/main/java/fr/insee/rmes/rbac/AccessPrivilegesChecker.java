package fr.insee.rmes.rbac;

import fr.insee.rmes.exceptions.RmesException;

public interface AccessPrivilegesChecker {

    boolean hasAccess(String module, String privilege, Object user) throws RmesException;
}
