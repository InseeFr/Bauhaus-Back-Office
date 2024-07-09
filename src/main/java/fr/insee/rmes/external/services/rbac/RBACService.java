package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.model.rbac.RBAC;

import java.util.List;
import java.util.Map;

public interface RBACService {

    Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> computeRbac(List<String> roles);
}
