package fr.insee.rmes.rbac;

import fr.insee.rmes.domain.exceptions.RmesException;

public interface AccessPrivilegesChecker {

    boolean hasAccess(String module, String privilege, String id, Object user) throws RmesException;
}
