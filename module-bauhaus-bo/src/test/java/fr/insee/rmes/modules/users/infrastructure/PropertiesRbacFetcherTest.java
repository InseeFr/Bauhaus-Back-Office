package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.domain.auth.Source;
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

import java.util.Arrays;
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

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN"), null);

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

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN", "USER"), null);

        assertThat(result).hasSize(1);
        ModuleAccessPrivileges conceptResult = result.stream()
            .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
            .findFirst()
            .get();

        // Should have READ with ALL strategy (minimum) and CREATE with STAMP
        assertThat(conceptResult.privileges()).hasSize(2);
    }

    @Test
    void should_add_read_all_for_all_non_unknown_modules_when_source_is_insee() {
        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of());
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of(), Source.INSEE);

        long expectedModuleCount = Arrays.stream(RBAC.Module.values())
                .filter(m -> m != RBAC.Module.UNKNOWN)
                .count();
        assertThat(result).hasSize((int) expectedModuleCount);
        assertThat(result).allSatisfy(mp ->
                assertThat(mp.privileges()).contains(
                        new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)
                )
        );
    }

    @Test
    void should_merge_read_stamp_and_read_all_to_read_all_when_two_roles_conflict() {
        var conceptForStampRole = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));
        var conceptForAllRole = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)));

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_A"), Set.of(conceptForStampRole)),
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_B"), Set.of(conceptForAllRole))
        ));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ROLE_A", "ROLE_B"), null);

        var conceptResult = result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst().orElseThrow();
        assertThat(conceptResult.privileges()).containsExactly(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)
        );
    }

    @Test
    void should_merge_read_none_and_read_stamp_to_read_stamp_when_two_roles_conflict() {
        var conceptForNoneRole = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.NONE)));
        var conceptForStampRole = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_A"), Set.of(conceptForNoneRole)),
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_B"), Set.of(conceptForStampRole))
        ));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ROLE_A", "ROLE_B"), null);

        var conceptResult = result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst().orElseThrow();
        assertThat(conceptResult.privileges()).containsExactly(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)
        );
    }

    @Test
    void should_keep_strategy_unchanged_when_both_roles_have_same_strategy() {
        var conceptForRoleA = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));
        var conceptForRoleB = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_A"), Set.of(conceptForRoleA)),
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_B"), Set.of(conceptForRoleB))
        ));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ROLE_A", "ROLE_B"), null);

        var conceptResult = result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst().orElseThrow();
        assertThat(conceptResult.privileges()).containsExactly(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)
        );
    }

    @Test
    void should_override_existing_read_stamp_with_read_all_when_source_is_insee() {
        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_A"), Set.of(conceptPrivileges))
        ));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ROLE_A"), Source.INSEE);

        var conceptResult = result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst().orElseThrow();
        assertThat(conceptResult.privileges()).contains(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)
        );
    }

    @Test
    void should_preserve_non_read_privileges_from_roles_when_source_is_insee() {
        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP),
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE)
        ));

        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(
                new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName("ROLE_A"), Set.of(conceptPrivileges))
        ));
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ROLE_A"), Source.INSEE);

        var conceptResult = result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst().orElseThrow();
        assertThat(conceptResult.privileges()).contains(
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL),
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP),
                new ModuleAccessPrivileges.Privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE)
        );
    }

    @Test
    void should_not_add_read_privilege_for_unknown_module_when_source_is_insee() {
        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of());
        createRbacFetcher();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of(), Source.INSEE);

        assertThat(result).noneMatch(mp -> mp.application().equals(RBAC.Module.UNKNOWN));
    }

}
