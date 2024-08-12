package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.RBACTest;
import fr.insee.rmes.config.auth.RBACConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


@RBACTest
class RBACConfigurationTest {

    @Autowired
    private RBACConfiguration rbacConfiguration;

    @Test
    void testReadRbacConfig() {
        assertThat(rbacConfiguration.toRBAC()).isNotNull();
    }
}