package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

public interface StampChecker {
    boolean userStampIsAuthorizedForResource(RBAC.Module module, String id);
}
