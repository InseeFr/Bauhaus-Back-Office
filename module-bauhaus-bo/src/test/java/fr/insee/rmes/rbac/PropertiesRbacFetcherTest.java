package fr.insee.rmes.rbac;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.domain.auth.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@AppSpringBootTest
@EnableConfigurationProperties(RBACConfiguration.class)
class PropertiesRbacFetcherTest {
    @Autowired
    RbacFetcher rbacFetcher;

    @Test
    void should_manage_one_role() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.STAMP);
    }

    @Test
    void should_manage_multiple_role() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS", "Unknown"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void should_return_empty_set_if_empty_roles_list(){
        var user = new User("", Collections.emptyList(), "HIE0000", Source.SSM.getValue());
        assertThat(rbacFetcher.computePrivileges(user)).hasSize(0);
    }

    @Test
    void should_compute_strategies(){
        var user = new User("", List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS"), "HIE0000", Source.SSM.getValue());
        var result = rbacFetcher.computePrivileges(user);

        assertThat(result).hasSize(19);

        ModuleAccessPrivileges privileges = result.stream().filter(r -> r.application().equals(RBAC.Module.CLASSIFICATION_CLASSIFICATION)).findFirst().get();

        assertEquals(RBAC.Module.CLASSIFICATION_CLASSIFICATION, privileges.application());
        assertThat(privileges.privileges()).hasSize(5);

        ModuleAccessPrivileges.Privilege privilege = privileges.privileges().stream().filter(p -> p.privilege().equals(RBAC.Privilege.UPDATE)).findFirst().get();

        assertEquals(RBAC.Privilege.UPDATE, privilege.privilege());
        assertEquals(RBAC.Strategy.ALL, privilege.strategy());
    }

    @ParameterizedTest
    @ValueSource(strings = { "firstName", "secondName","thirdName" })
    void should_initialize_constructor_and_throw_unknown_role_exception( String roleName){

        Map<RBAC.Privilege, RBAC.Strategy> firstLevel = Map.of(RBAC.Privilege.READ,RBAC.Strategy.STAMP);
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> secondLevel = Map.of(RBAC.Module.CONCEPT_CONCEPT,firstLevel);
        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> thirdLevel = Map.of("roleName",secondLevel);
        RBACConfiguration rbacConfiguration = new RBACConfiguration(thirdLevel);

        PropertiesRbacFetcher propertiesRbacFetcher = new PropertiesRbacFetcher(rbacConfiguration);

        UnknownRoleException exception = assertThrows(UnknownRoleException.class, () -> propertiesRbacFetcher.getPrivilegesByRole(roleName));

        assertTrue(exception.getMessage().contains("Unknown role:"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Administrateur_RMESGNCS", "Gestionnaire_concept_RMESGNCS", "Utilisateur_RMESGNCS" })
    void should_throw_unknown_application_exception(String roleName){

        Map<RBAC.Privilege, RBAC.Strategy> firstLevel = Map.of(RBAC.Privilege.READ,RBAC.Strategy.STAMP);
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> secondLevel = Map.of(RBAC.Module.CONCEPT_CONCEPT,firstLevel);
        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> thirdLevel = Map.of(roleName,secondLevel);
        RBACConfiguration rbacConfiguration = new RBACConfiguration(thirdLevel);

        PropertiesRbacFetcher propertiesRbacFetcher = new PropertiesRbacFetcher(rbacConfiguration);

        UnknownApplicationException exception = assertThrows(UnknownApplicationException.class, () -> propertiesRbacFetcher.getApplicationPrivilegesByRole(roleName, RBAC.Module.UNKNOWN));

        assertTrue(exception.getMessage().contains("Unknown application 'UNKNOWN' for role:"));
    }

    @Test
    void should_initialize_read_privileges_for_insee_user_without_roles() {
        var user = new User("", Collections.emptyList(), "DG75-H250", Source.INSEE.getValue());
        var result = rbacFetcher.computePrivileges(user);

        // Tous les modules sauf UNKNOWN (24 modules)
        long expectedModulesCount = java.util.Arrays.stream(RBAC.Module.values())
                .filter(m -> m != RBAC.Module.UNKNOWN)
                .count();
        assertThat(result).hasSize((int) expectedModulesCount);

        // Vérifier que chaque module a le privilège READ avec la stratégie ALL
        for (ModuleAccessPrivileges modulePrivileges : result) {
            assertThat(modulePrivileges.privileges()).hasSize(1);
            ModuleAccessPrivileges.Privilege readPrivilege = modulePrivileges.privileges().stream().findFirst().get();
            assertEquals(RBAC.Privilege.READ, readPrivilege.privilege());
            assertEquals(RBAC.Strategy.ALL, readPrivilege.strategy());
        }
    }

    @Test
    void should_merge_read_privileges_for_insee_user_with_roles() {
        var user = new User("", List.of("Gestionnaire_indicateur_RMESGNCS"), "DG75-H250", Source.INSEE.getValue());
        var result = rbacFetcher.computePrivileges(user);

        // Tous les modules doivent avoir le privilège READ avec la stratégie ALL
        for (ModuleAccessPrivileges modulePrivileges : result) {
            ModuleAccessPrivileges.Privilege readPrivilege = modulePrivileges.privileges().stream()
                    .filter(p -> p.privilege().equals(RBAC.Privilege.READ))
                    .findFirst()
                    .orElse(null);

            assertNotNull(readPrivilege, "Module " + modulePrivileges.application() + " should have READ privilege");
            assertEquals(RBAC.Strategy.ALL, readPrivilege.strategy(),
                    "Module " + modulePrivileges.application() + " should have ALL strategy for READ");
        }

        // Vérifier qu'un module qui a des privilèges supplémentaires du rôle les conserve
        ModuleAccessPrivileges operationSimsPrivileges = result.stream()
                .filter(r -> r.application().equals(RBAC.Module.OPERATION_SIMS))
                .findFirst()
                .orElse(null);

        assertNotNull(operationSimsPrivileges);
        // Le module OPERATION_SIMS devrait avoir plus d'un privilège (READ + les privilèges du rôle)
        assertThat(operationSimsPrivileges.privileges().size()).isGreaterThan(1);
    }

}