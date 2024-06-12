package fr.insee.rmes.external_services.rbac;

import fr.insee.rmes.config.auth.RBACConfiguration;
import fr.insee.rmes.model.rbac.RBAC;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RBACServiceImplTest {

    @Autowired
    RBACServiceImpl rbacService;

    @MockBean
    RBACConfiguration configuration;

    @Test
    void shouldReturnAnEmptyMapIfMissingRoles() {
        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> values = rbacService.computeRbac(List.of());
        assertTrue(values.isEmpty());
    }

    @Test
    void shouldReturnAnEmptyMapIfUnknownRole() {
        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> values = rbacService.computeRbac(List.of("UNKNOWN"));
        assertTrue(values.isEmpty());
    }

    @Test
    void shouldReturnDirectlyTheDataIfOnlyOneRole() {
        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> privileges = Map.of(
                RBAC.APPLICATION.concept, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.ALL, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.STAMP)
        );
        //when(configuration.getRbac()).thenReturn(Map.of("ADMIN", privileges));

        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> values = rbacService.computeRbac(List.of("ADMIN"));
        assertEquals(values, privileges);
    }

    @Test
    void shouldDoComputationIfMultipleRole() {
        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> adminPrivileges = Map.of(
                RBAC.APPLICATION.concept, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.ALL, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.STAMP),
                RBAC.APPLICATION.classification, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.STAMP, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.ALL)
        );

        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> userPrivileges = Map.of(
                RBAC.APPLICATION.concept, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.STAMP, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.STAMP),
                RBAC.APPLICATION.classification, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.ALL, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.STAMP)
        );

        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> expectedPrivileges = Map.of(
                RBAC.APPLICATION.concept, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.ALL, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.STAMP),
                RBAC.APPLICATION.classification, Map.of(RBAC.PRIVILEGE.read, RBAC.STRATEGY.ALL, RBAC.PRIVILEGE.delete, RBAC.STRATEGY.ALL)
        );

        //when(configuration.getRbac()).thenReturn(Map.of("ADMIN", adminPrivileges, "USER", userPrivileges));



        Map<RBAC.APPLICATION, Map<RBAC.PRIVILEGE, RBAC.STRATEGY>> values = rbacService.computeRbac(List.of("ADMIN", "USER"));
        assertEquals(expectedPrivileges, values);
    }
}