package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.rbac.RBAC;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RBACServiceImplTest {

    @Autowired
    RBACServiceImpl rbacService;

    @MockBean
    RBACConfiguration configuration;

    @Test
    void shouldReturnAnEmptyMapIfMissingRoles() {
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> values = rbacService.computeRbac(List.of());
        assertTrue(values.isEmpty());
    }

    @Test
    void shouldReturnAnEmptyMapIfUnknownRole() {
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> values = rbacService.computeRbac(List.of("UNKNOWN"));
        assertTrue(values.isEmpty());
    }
}