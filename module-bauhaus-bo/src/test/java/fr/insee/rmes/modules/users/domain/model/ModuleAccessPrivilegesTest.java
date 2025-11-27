package fr.insee.rmes.modules.users.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleAccessPrivilegesTest {

    @Test
    void shouldCreateModuleAccessPrivileges() {
        var privilege1 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL);
        var privilege2 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP);

        var modulePrivileges = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(privilege1, privilege2)
        );

        assertThat(modulePrivileges.application()).isEqualTo(RBAC.Module.CONCEPT_CONCEPT);
        assertThat(modulePrivileges.privileges()).hasSize(2);
        assertThat(modulePrivileges.privileges()).contains(privilege1, privilege2);
    }

    @Test
    void shouldCreateEmptyPrivileges() {
        var modulePrivileges = new ModuleAccessPrivileges(
            RBAC.Module.OPERATION_SERIES,
            Set.of()
        );

        assertThat(modulePrivileges.application()).isEqualTo(RBAC.Module.OPERATION_SERIES);
        assertThat(modulePrivileges.privileges()).isEmpty();
    }

    @Test
    void shouldCreatePrivilegeWithAllStrategy() {
        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.PUBLISH, RBAC.Strategy.ALL);

        assertThat(privilege.privilege()).isEqualTo(RBAC.Privilege.PUBLISH);
        assertThat(privilege.strategy()).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void shouldCreatePrivilegeWithStampStrategy() {
        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);

        assertThat(privilege.privilege()).isEqualTo(RBAC.Privilege.UPDATE);
        assertThat(privilege.strategy()).isEqualTo(RBAC.Strategy.STAMP);
    }

    @Test
    void shouldCreatePrivilegeWithNoneStrategy() {
        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE);

        assertThat(privilege.privilege()).isEqualTo(RBAC.Privilege.DELETE);
        assertThat(privilege.strategy()).isEqualTo(RBAC.Strategy.NONE);
    }

    @Test
    void shouldSupportAllModules() {
        assertThat(RBAC.Module.CONCEPT_CONCEPT).isNotNull();
        assertThat(RBAC.Module.OPERATION_SERIES).isNotNull();
        assertThat(RBAC.Module.DATASET_DATASET).isNotNull();
        assertThat(RBAC.Module.STRUCTURE_STRUCTURE).isNotNull();
    }

    @Test
    void shouldSupportAllPrivileges() {
        assertThat(RBAC.Privilege.CREATE).isNotNull();
        assertThat(RBAC.Privilege.READ).isNotNull();
        assertThat(RBAC.Privilege.UPDATE).isNotNull();
        assertThat(RBAC.Privilege.DELETE).isNotNull();
        assertThat(RBAC.Privilege.PUBLISH).isNotNull();
        assertThat(RBAC.Privilege.ADMINISTRATION).isNotNull();
    }

    @Test
    void shouldSupportAllStrategies() {
        assertThat(RBAC.Strategy.ALL).isNotNull();
        assertThat(RBAC.Strategy.STAMP).isNotNull();
        assertThat(RBAC.Strategy.NONE).isNotNull();
    }
}
