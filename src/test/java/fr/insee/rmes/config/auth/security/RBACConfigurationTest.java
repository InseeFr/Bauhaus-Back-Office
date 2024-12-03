package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.RBACConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = "spring.config.additional-location=classpath:rbac.yml")
@EnableConfigurationProperties(RBACConfiguration.class)
class RBACConfigurationTest {

    @Autowired
    private RBACConfiguration rbacConfiguration;

    @Test
    void testReadRbacConfig() {
        assertThat(rbacConfiguration.allModulesAccessPrivileges()).isNotNull();
    }
}