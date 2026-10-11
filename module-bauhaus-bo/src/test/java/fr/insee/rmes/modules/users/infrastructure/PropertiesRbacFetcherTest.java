package fr.insee.rmes.modules.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.insee.rmes.modules.users.domain.exceptions.UnknownApplicationException;
import fr.insee.rmes.modules.users.domain.exceptions.UnknownRoleException;
import fr.insee.rmes.modules.users.domain.model.AllModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Source;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

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

    private void givenRoles(AllModuleAccessPrivileges... roles) {
        when(rbacConfiguration.allModulesAccessPrivileges()).thenReturn(Set.of(roles));
        createRbacFetcher();
    }

    private static AllModuleAccessPrivileges role(String name, ModuleAccessPrivileges... modulePrivileges) {
        return new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(name), Set.of(modulePrivileges));
    }

    private static ModuleAccessPrivileges.Privilege privilege(RBAC.Privilege privilege, RBAC.Strategy strategy) {
        return new ModuleAccessPrivileges.Privilege(privilege, strategy);
    }

    private static ModuleAccessPrivileges conceptPrivileges(ModuleAccessPrivileges.Privilege... privileges) {
        return new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privileges));
    }

    private ModuleAccessPrivileges computedConceptPrivileges(List<String> roles, Source source) {
        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(roles, source);

        return result.stream()
                .filter(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void should_get_privileges_by_role() {
        var readPrivilege = privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivileges = conceptPrivileges(readPrivilege);
        var operationPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(createPrivilege));

        givenRoles(role("ADMIN", conceptPrivileges, operationPrivileges));

        Set<ModuleAccessPrivileges> result = rbacFetcher.getPrivilegesByRole("ADMIN");

        assertThat(result).hasSize(2);
        assertThat(result).contains(conceptPrivileges, operationPrivileges);
    }

    @Test
    void should_throw_exception_when_role_not_found() {
        givenRoles();

        assertThatThrownBy(() -> rbacFetcher.getPrivilegesByRole("UNKNOWN_ROLE"))
                .isInstanceOf(UnknownRoleException.class)
                .hasMessageContaining("UNKNOWN_ROLE");
    }

    @Test
    void should_get_application_privileges_by_role() {
        var readPrivilege = privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        givenRoles(role("ADMIN", conceptPrivileges(readPrivilege, createPrivilege)));

        Set<ModuleAccessPrivileges.Privilege> result =
                rbacFetcher.getApplicationPrivilegesByRole("ADMIN", RBAC.Module.CONCEPT_CONCEPT);

        assertThat(result).hasSize(2);
        assertThat(result).contains(readPrivilege, createPrivilege);
    }

    @Test
    void should_throw_exception_when_application_not_found() {
        givenRoles(role("ADMIN", conceptPrivileges()));

        assertThatThrownBy(() -> rbacFetcher.getApplicationPrivilegesByRole("ADMIN", RBAC.Module.OPERATION_SERIES))
                .isInstanceOf(UnknownApplicationException.class);
    }

    @Test
    void should_get_application_action_strategy_by_role() {
        givenRoles(role("ADMIN", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL))));

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
                List.of("ADMIN"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ);

        assertThat(result).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void should_return_none_strategy_when_role_not_found_in_get_application_action_strategy_by_role() {
        givenRoles();

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
                List.of("UNKNOWN_ROLE"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ);

        assertThat(result).isEqualTo(RBAC.Strategy.NONE);
    }

    @Test
    void should_return_min_strategy_when_multiple_roles() {
        givenRoles(
                role("ADMIN", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL))),
                role("USER", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP))));

        RBAC.Strategy result = rbacFetcher.getApplicationActionStrategyByRole(
                List.of("ADMIN", "USER"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ);

        // ALL (0) < STAMP (1), so minimum should be ALL
        assertThat(result).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void should_compute_privileges() {
        givenRoles(role(
                "ADMIN",
                conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)),
                new ModuleAccessPrivileges(
                        RBAC.Module.OPERATION_SERIES, Set.of(privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP)))));

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN"), null);

        assertThat(result).hasSize(2);
        assertThat(result.stream().anyMatch(mp -> mp.application().equals(RBAC.Module.CONCEPT_CONCEPT)))
                .isTrue();
        assertThat(result.stream().anyMatch(mp -> mp.application().equals(RBAC.Module.OPERATION_SERIES)))
                .isTrue();
    }

    @Test
    void should_compute_privileges_for_multiple_roles() {
        givenRoles(
                role(
                        "ADMIN",
                        conceptPrivileges(
                                privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL),
                                privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP))),
                role("USER", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP))));

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
    void should_warn_and_ignore_unknown_role_when_computing_privileges() {
        givenRoles(role("ADMIN", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL))));

        Logger logger = (Logger) LoggerFactory.getLogger(PropertiesRbacFetcher.class);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
        try {
            Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of("ADMIN", "GHOST_ROLE"), null);

            assertThat(result)
                    .extracting(ModuleAccessPrivileges::application)
                    .containsExactly(RBAC.Module.CONCEPT_CONCEPT);
            assertThat(logs.list)
                    .filteredOn(event -> event.getLevel() == Level.WARN)
                    .as("un rôle absent de la configuration RBAC doit être signalé")
                    .anySatisfy(event -> assertThat(event.getFormattedMessage()).contains("GHOST_ROLE"));
        } finally {
            logger.detachAppender(logs);
        }
    }

    @Test
    void should_add_read_all_for_all_non_unknown_modules_when_source_is_insee() {
        givenRoles();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of(), Source.INSEE);

        long expectedModuleCount = Arrays.stream(RBAC.Module.values())
                .filter(m -> m != RBAC.Module.UNKNOWN)
                .count();
        assertThat(result).hasSize((int) expectedModuleCount);
        assertThat(result)
                .allSatisfy(
                        mp -> assertThat(mp.privileges()).contains(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)));
    }

    /** ROLE_A and ROLE_B both grant READ on concepts, each with its own strategy. */
    private void assertReadStrategiesMergeInto(
            RBAC.Strategy roleAStrategy, RBAC.Strategy roleBStrategy, RBAC.Strategy expectedStrategy) {
        givenRoles(
                role("ROLE_A", conceptPrivileges(privilege(RBAC.Privilege.READ, roleAStrategy))),
                role("ROLE_B", conceptPrivileges(privilege(RBAC.Privilege.READ, roleBStrategy))));

        var conceptResult = computedConceptPrivileges(List.of("ROLE_A", "ROLE_B"), null);

        assertThat(conceptResult.privileges()).containsExactly(privilege(RBAC.Privilege.READ, expectedStrategy));
    }

    @Test
    void should_merge_read_stamp_and_read_all_to_read_all_when_two_roles_conflict() {
        assertReadStrategiesMergeInto(RBAC.Strategy.STAMP, RBAC.Strategy.ALL, RBAC.Strategy.ALL);
    }

    @Test
    void should_merge_read_none_and_read_stamp_to_read_stamp_when_two_roles_conflict() {
        assertReadStrategiesMergeInto(RBAC.Strategy.NONE, RBAC.Strategy.STAMP, RBAC.Strategy.STAMP);
    }

    @Test
    void should_keep_strategy_unchanged_when_both_roles_have_same_strategy() {
        assertReadStrategiesMergeInto(RBAC.Strategy.STAMP, RBAC.Strategy.STAMP, RBAC.Strategy.STAMP);
    }

    @Test
    void should_override_existing_read_stamp_with_read_all_when_source_is_insee() {
        givenRoles(role("ROLE_A", conceptPrivileges(privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP))));

        var conceptResult = computedConceptPrivileges(List.of("ROLE_A"), Source.INSEE);

        assertThat(conceptResult.privileges()).contains(privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL));
    }

    @Test
    void should_preserve_non_read_privileges_from_roles_when_source_is_insee() {
        givenRoles(role(
                "ROLE_A",
                conceptPrivileges(
                        privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP),
                        privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE))));

        var conceptResult = computedConceptPrivileges(List.of("ROLE_A"), Source.INSEE);

        assertThat(conceptResult.privileges())
                .contains(
                        privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL),
                        privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP),
                        privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE));
    }

    @Test
    void should_not_add_read_privilege_for_unknown_module_when_source_is_insee() {
        givenRoles();

        Set<ModuleAccessPrivileges> result = rbacFetcher.computePrivileges(List.of(), Source.INSEE);

        assertThat(result).noneMatch(mp -> mp.application().equals(RBAC.Module.UNKNOWN));
    }
}
