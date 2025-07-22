package fr.insee.rmes.rbac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.config.additional-location=classpath:rbac.yml")
@EnableConfigurationProperties(RBACConfiguration.class)
class PropertiesRbacFetcherTest {
    @Autowired
    RbacFetcher rbacFetcher;

    @Test
    void shouldManageOneRole() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.STAMP);
    }

    @Test
    void shouldManageMultipleRole() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS", "Unknown"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void shouldReturnEmptySetIfEmptyRolesList(){
        assertThat(rbacFetcher.computePrivileges(Collections.<String>emptyList()).size()).isEqualTo(0);
    }

    @Test
    void shouldComputeStrategies(){
        var result = rbacFetcher.computePrivileges(List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS"));

        assertEquals(18, result.size());

        ModuleAccessPrivileges privileges = result.stream().filter(r -> r.application().equals(RBAC.Module.CLASSIFICATION_CLASSIFICATION)).findFirst().get();

        assertEquals(RBAC.Module.CLASSIFICATION_CLASSIFICATION, privileges.application());
        assertEquals(5, privileges.privileges().size());

        ModuleAccessPrivileges.Privilege privilege = privileges.privileges().stream().filter(p -> p.privilege().equals(RBAC.Privilege.UPDATE)).findFirst().get();

        assertEquals(RBAC.Privilege.UPDATE, privilege.privilege());
        assertEquals(RBAC.Strategy.ALL, privilege.strategy());
    }
}