package fr.insee.rmes.external.services.rbac;

import fr.insee.rmes.rbac.RBACConfiguration;
import fr.insee.rmes.rbac.RBAC;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RBACServiceImplTest {

    @Autowired
    RBACServiceImpl rbacService;

    @MockitoBean
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