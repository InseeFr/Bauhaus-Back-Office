package fr.insee.rmes.external.services.rbac;


import org.springframework.stereotype.Service;

import java.util.List;

public interface RBACService {

    AccessPrivileges computeRbac(List<String> roles);

}
