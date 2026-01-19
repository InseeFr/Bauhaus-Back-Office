package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.users.domain.exceptions.UnknownApplicationException;
import fr.insee.rmes.modules.users.domain.exceptions.UnknownRoleException;
import fr.insee.rmes.modules.users.domain.model.AllModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesRbacFetcherTest {

    @Mock
    private RBACConfiguration rbacConfiguration;

    private PropertiesRbacFetcher rbacFetcher;

    @BeforeEach
    void setUp() {
        // Set up a default empty set that can be overridden in individual tests
        lenient().when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of());
        // Note: Individual tests must call createRbacFetcher() after setting up their specific mocks
    }

    private void createRbacFetcher() {
        rbacFetcher = new PropertiesRbacFetcher(rbacConfiguration);
    }

    @Test
    void should_get_privileges_by_role() {
        var readPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(readPrivilege));
        var operationPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(createPrivilege));

        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivileges, operationPrivileges)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.getPrivilegesByRole("ADMIN");

        assertThat(result).hasSize(2);
        assertThat(result).contains(conceptPrivileges, operationPrivileges);
    }

    @Test
    void should_throw_exception_when_role_not_found() {
        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of());
        createRbacFetcher();

        assertThatThrownBy(() -> rbacFetcher.getPrivilegesByRole("UNKNOWN_ROLE"))
            .isInstanceOf(UnknownRoleException.class)
            .hasMessageContaining("UNKNOWN_ROLE");
    }

    @Test
    void should_get_application_privileges_by_role() {
        var readPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivileges = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(readPrivilege, createPrivilege)
        );

        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivileges)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole));
        createRbacFetcher();

        Set<ModuleAccessPrivileges.Privilege> result = rbacFetcher.getApplicationPrivilegesByRole(
            "ADMIN",
            RBAC.Module.CONCEPT_CONCEPT
        );

        assertThat(result).hasSize(2);
        assertThat(result).contains(readPrivilege, createPrivilege);
    }

    @Test
    void should_throw_exception_when_application_not_found() {
        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of());
        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivileges)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole));
        createRbacFetcher();

        assertThatThrownBy(() -> rbacFetcher.getApplicationPrivilegesByRole("ADMIN", RBAC.Module.OPERATION_SERIES))
            .isInstanceOf(UnknownApplicationException.class);
    }

    @Test
    void should_get_application_action_strategy_by_role() {
        var readPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(readPrivilege));
        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivileges)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole));
        createRbacFetcher();

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
            List.of("ADMIN"),
            RBAC.Module.CONCEPT_CONCEPT,
            RBAC.Privilege.READ
        );

        assertThat(result).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void should_return_none_strategy_when_role_not_found_in_get_application_action_strategy_by_role() {
        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of());
        createRbacFetcher();

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
            List.of("UNKNOWN_ROLE"),
            RBAC.Module.CONCEPT_CONCEPT,
            RBAC.Privilege.READ
        );

        assertThat(result).isEqualTo(RBAC.Strategy.NONE);
    }

    @Test
    void should_return_min_strategy_when_multiple_roles() {
        var readAllPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var readStampPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP);

        var conceptPrivilegesAdmin = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(readAllPrivilege));
        var conceptPrivilegesUser = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(readStampPrivilege));

        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivilegesAdmin)
        );
        var userRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("USER"),
            Set.of(conceptPrivilegesUser)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole, userRole));
        createRbacFetcher();

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
            List.of("ADMIN", "USER"),
            RBAC.Module.CONCEPT_CONCEPT,
            RBAC.Privilege.READ
        );

        // ALL (0) < STAMP (1), so minimum should be ALL
        assertThat(result).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void should_compute_privileges() {
        var readPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(readPrivilege));
        var operationPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(createPrivilege));

        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivileges, operationPrivileges)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN"));

        assertThat(result).hasSize(2);
        assertThat(result.stream()
            .anyMatch(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT)))
            .isTrue();
        assertThat(result.stream()
            .anyMatch(mp -> mp.application().equals(RBAC.Module.OPERATION_SERIES)))
            .isTrue();
    }

    @Test
    void should_compute_privileges_for_multiple_roles() {
        var readAllPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var readStampPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP);
        var createPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivilegesAdmin = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(readAllPrivilege, createPrivilege)
        );
        var conceptPrivilegesUser = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(readStampPrivilege)
        );

        var adminRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("ADMIN"),
            Set.of(conceptPrivilegesAdmin)
        );
        var userRole = new AllModuleAccessPrivileges(
            new AllModuleAccessPrivileges.RoleName("USER"),
            Set.of(conceptPrivilegesUser)
        );

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(adminRole, userRole));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN", "USER"));

        assertThat(result).hasSize(1);
        ModuleAccessPrivileges conceptResult = result.stream()
            .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
            .findFirst()
            .get();

        // Should have READ with ALL strategy (minimum) and CREATE with STAMP
        assertThat(conceptResult.privileges()).hasSize(2);
    }

}
