package fr.insee.rmes.rbac;

import fr.insee.rmes.onion.domain.exceptions.RmesException;

public interface AccessPrivilegesChecker {

    boolean hasAccess(String module, String privilege, String id, Object user) throws RmesException;
}
