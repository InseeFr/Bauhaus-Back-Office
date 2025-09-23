package fr.insee.rmes.rbac;

import fr.insee.rmes.AppSpringBootTest;
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
    void shouldManageOneRole() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.STAMP);
    }

    @Test
    void shouldManageMultipleRole() {
        assertThat(rbacFetcher.getApplicationActionStrategyByRole(List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS", "Unknown"), RBAC.Module.OPERATION_SIMS, RBAC.Privilege.CREATE)).isEqualTo(RBAC.Strategy.ALL);
    }

    @Test
    void shouldReturnEmptySetIfEmptyRolesList(){
        assertThat(rbacFetcher.computePrivileges(Collections.emptyList())).hasSize(0);
    }

    @Test
    void shouldComputeStrategies(){
        var result = rbacFetcher.computePrivileges(List.of("Gestionnaire_indicateur_RMESGNCS", "Administrateur_RMESGNCS"));

        assertThat(result).hasSize(18);

        ModuleAccessPrivileges privileges = result.stream().filter(r -> r.application().equals(RBAC.Module.CLASSIFICATION_CLASSIFICATION)).findFirst().get();

        assertEquals(RBAC.Module.CLASSIFICATION_CLASSIFICATION, privileges.application());
        assertThat(privileges.privileges()).hasSize(5);

        ModuleAccessPrivileges.Privilege privilege = privileges.privileges().stream().filter(p -> p.privilege().equals(RBAC.Privilege.UPDATE)).findFirst().get();

        assertEquals(RBAC.Privilege.UPDATE, privilege.privilege());
        assertEquals(RBAC.Strategy.ALL, privilege.strategy());
    }

    @ParameterizedTest
    @ValueSource(strings = { "firstName", "secondName","thirdName" })
    void shouldInitializeConstructorAndThrowUnknownRoleException( String roleName){

        Map<RBAC.Privilege, RBAC.Strategy> firstLevel = Map.of(RBAC.Privilege.READ,RBAC.Strategy.STAMP);
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> secondLevel = Map.of(RBAC.Module.CONCEPT_CONCEPT,firstLevel);
        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> thirdLevel = Map.of("roleName",secondLevel);
        RBACConfiguration rbacConfiguration = new RBACConfiguration(thirdLevel);

        PropertiesRbacFetcher propertiesRbacFetcher = new PropertiesRbacFetcher(rbacConfiguration);

        UnknownRoleException exception = assertThrows(UnknownRoleException.class, () -> propertiesRbacFetcher.getPrivilegesByRole(roleName));

        assertTrue(exception.getMessage().contains("Unknown role:"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "firstName", "secondName","thirdName" })
    void shouldThrowUnknownApplicationException(String roleName){

        Map<RBAC.Privilege, RBAC.Strategy> firstLevel = Map.of(RBAC.Privilege.READ,RBAC.Strategy.STAMP);
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> secondLevel = Map.of(RBAC.Module.CONCEPT_CONCEPT,firstLevel);
        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> thirdLevel = Map.of(roleName,secondLevel);
        RBACConfiguration rbacConfiguration = new RBACConfiguration(thirdLevel);

        PropertiesRbacFetcher propertiesRbacFetcher = new PropertiesRbacFetcher(rbacConfiguration);

        UnknownApplicationException exception = assertThrows(UnknownApplicationException.class, () -> propertiesRbacFetcher.getApplicationPrivilegesByRole(roleName, RBAC.Module.UNKNOWN));

        assertTrue(exception.getMessage().contains("Unknown application 'UNKNOWN' for role:"));
    }

}