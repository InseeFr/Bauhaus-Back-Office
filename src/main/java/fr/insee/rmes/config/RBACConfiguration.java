package fr.insee.rmes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

enum application {
    concept,
    collection,
    family,
    serie,
    operation,
    indicator,
    sims,
    classification
}

enum rbac {
    create,
    read,
    update,
    delete,
    publish,
    validate
}

enum right {
    ALL, STAMP
}

@Component
@ConfigurationProperties()
public class RBACConfiguration {



    private Map<String, Map<application, Map<rbac, right>>> rbac;

    public RBACConfiguration() {
        System.out.println("lol");
    }

    public Map<String, Map<application, Map<rbac, right>>> getRbac() {
        return rbac;
    }

    public void setRbac(Map<String, Map<application, Map<rbac, right>>> rbac) {
        this.rbac = rbac;
    }
}
