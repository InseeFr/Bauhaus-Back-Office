package fr.insee.rmes.external.services.rbac;


import fr.insee.rmes.config.auth.RBACConfiguration;

import java.util.List;

public interface RBACService {

    ApplicationAccessPrivileges computeRbac(List<RBACConfiguration.RoleName> roles);
}
