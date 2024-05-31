package fr.insee.rmes.config.auth;

import fr.insee.rmes.model.RBAC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@ConfigurationProperties()
public class RBACConfiguration {

    private Map<String, Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>>> rbac;

    public RBACConfiguration() {
    }

    public Map<String, Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>>> getRbac() {
        return rbac;
    }

    public void setRbac(Map<String, Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>>> rbac) {
        this.rbac = rbac;
    }
}
