package fr.insee.rmes.external_services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

import java.util.List;
import java.util.Map;

public interface RBACService {

    Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> computeRbac(List<String> roles);
}
