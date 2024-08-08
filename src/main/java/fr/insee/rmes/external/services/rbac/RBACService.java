package fr.insee.rmes.external.services.rbac;


import java.util.List;

public interface RBACService {

    ApplicationAccessPrivileges computeRbac(List<String> roles);

}
