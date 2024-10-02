package fr.insee.rmes.config.auth;

import fr.insee.rmes.external.services.rbac.RBACService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessControlConfiguration {

    @Bean
    public RBACService rbacService(RBACConfiguration rbacConfiguration){
        return new RBACService(rbacConfiguration.toRBAC());
    }
}
