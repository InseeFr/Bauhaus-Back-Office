package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.rbac.RBACConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;


@AppSpringBootTest
@EnableConfigurationProperties(RBACConfiguration.class)
class RBACConfigurationTest {

    @Autowired
    private RBACConfiguration rbacConfiguration;

    @Test
    void testReadRbacConfig() {
        var privileges = rbacConfiguration.allModulesAccessPrivileges();
        assertThat(privileges).isNotNull();
    }
}