package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RBACServiceImplTest {

    @Autowired
    RBACServiceImpl rbacService;

    @Test
    void shouldReturnAnEmptyMapIfMissingRoles() {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of());
        assertThat(applicationAccessPrivileges).isEqualTo(ApplicationAccessPrivileges.NO_PRIVILEGE);
    }

    @Test
    void shouldReturnAnEmptyMapIfUnknownRole() {
        ApplicationAccessPrivileges applicationAccessPrivileges = rbacService.computeRbac(List.of(new RBACConfiguration.RoleName("UNKNOWN")));
        assertThat(applicationAccessPrivileges).isEqualTo(ApplicationAccessPrivileges.NO_PRIVILEGE);
    }
}