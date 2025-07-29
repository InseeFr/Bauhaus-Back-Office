package fr.insee.rmes.rbac;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class RBACConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = { "firstMockedRoleName", "secondMockedRoleName","thirdMockedRoleName" })
    void shouldReturnResultWhenAllModulesAccessPrivileges(String roleName){

        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> firstSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.NONE)));
        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> secondSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.ALL)));
        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> thirdSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.STAMP)));
        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> fourthSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.NONE)));
        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> fifthSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.ALL)));
        Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> sixthSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.STAMP)));

        ModuleAccessPrivileges firstModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,firstSet);
        ModuleAccessPrivileges secondModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,secondSet);
        ModuleAccessPrivileges thirdModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,thirdSet);
        ModuleAccessPrivileges fourthModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,fourthSet);
        ModuleAccessPrivileges fifthModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,fifthSet);
        ModuleAccessPrivileges sixthModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,sixthSet);

        Set<ModuleAccessPrivileges> firstModule = new HashSet<>(List.of(firstModuleAccessPrivileges,secondModuleAccessPrivileges,thirdModuleAccessPrivileges));
        Set<ModuleAccessPrivileges> secondModule = new HashSet<>(List.of(fourthModuleAccessPrivileges,fifthModuleAccessPrivileges,sixthModuleAccessPrivileges));

        AllModuleAccessPrivileges firstAccessPrivileges = new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(roleName),firstModule);
        AllModuleAccessPrivileges secondAccessPrivileges = new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(roleName),secondModule);

        Set<AllModuleAccessPrivileges> allModulesAccessPrivileges = new HashSet<>(List.of(firstAccessPrivileges,secondAccessPrivileges));

        RBACConfiguration rbacConfiguration = new RBACConfiguration(allModulesAccessPrivileges);

        String actual = rbacConfiguration.allModulesAccessPrivileges().toString();

        boolean actualContainsValues = actual.contains("READ") &&
                actual.contains("UPDATE") &&
                actual.contains("NONE") &&
                actual.contains("ALL") &&
                actual.contains("STAMP") &&
                actual.contains(roleName) ;

        assertTrue(actualContainsValues);
    }


    @ParameterizedTest
    @ValueSource(strings = { "firstMockedRoleName", "secondMockedRoleName","thirdMockedRoleName" })
    void shouldReturnResultWhenAllModulesAccessPrivilegesWithAnotherConstructor(String roleName){

        Map<RBAC.Privilege, RBAC.Strategy> firstLevel = Map.of(RBAC.Privilege.READ,RBAC.Strategy.STAMP);
        Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>> secondLevel = Map.of(RBAC.Module.CONCEPT_CONCEPT,firstLevel);
        Map<String, Map<RBAC.Module, Map<RBAC.Privilege, RBAC.Strategy>>> thirdLevel = Map.of(roleName,secondLevel);

        RBACConfiguration rbacConfiguration = new RBACConfiguration(thirdLevel);

        String actual = rbacConfiguration.allModulesAccessPrivileges().toString();

        boolean actualContainsValues = actual.contains("READ") &&
                actual.contains("STAMP") &&
                actual.contains("CONCEPT_CONCEPT") &&
                actual.contains(roleName) ;

        assertTrue(actualContainsValues);

    }












}