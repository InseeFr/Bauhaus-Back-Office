package fr.insee.rmes.rbac;

import fr.insee.rmes.modules.users.domain.model.AllModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

 class AllModuleAccessPrivilegesTest {

    Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> firstSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.NONE)));
    Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> secondSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.ALL)));
    Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> thirdSet = new HashSet<>(List.of(new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.STAMP)));

    ModuleAccessPrivileges firstModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,firstSet);
    ModuleAccessPrivileges secondModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,secondSet);
    ModuleAccessPrivileges thirdModuleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,thirdSet);

    Set<ModuleAccessPrivileges> modules = new HashSet<>(List.of(firstModuleAccessPrivileges,secondModuleAccessPrivileges,thirdModuleAccessPrivileges));

    @ParameterizedTest
    @ValueSource(strings = { "firstMockedRoleName", "secondMockedRoleName","thirdMockedRoleName" })
    void shouldReturnResultWhenAllModulesAccessPrivileges(String roleName){
        AllModuleAccessPrivileges allModuleAccessPrivileges = new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(roleName),modules);
        String actual = allModuleAccessPrivileges.privileges().toString();
        boolean actualContainsEnum = actual.contains("READ") &&
                actual.contains("ALL") &&
                actual.contains("NONE") &&
                actual.contains("STAMP");
        assertTrue(actualContainsEnum);
    }

    @ParameterizedTest
    @ValueSource(strings = { "firstMockedRoleName", "secondMockedRoleName","thirdMockedRoleName" })
    void shouldReturnResultWhenAllModulesRoleNames(String roleName){
        AllModuleAccessPrivileges allModuleAccessPrivileges = new AllModuleAccessPrivileges(new AllModuleAccessPrivileges.RoleName(roleName),modules);
        assertTrue(allModuleAccessPrivileges.roleName().toString().contains(roleName));
    }

}