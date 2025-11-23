package fr.insee.rmes.rbac;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class ModuleAccessPrivilegesTest {

    Set<fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege> set = new HashSet<>(
            List.of(
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.NONE),
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.ALL),
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ,RBAC.Strategy.STAMP),
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.NONE),
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.ALL),
                    new fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE,RBAC.Strategy.STAMP)));

    ModuleAccessPrivileges moduleAccessPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SIMS,set);


    @Test
    public void shouldReturnResultWhenPrivileges(){
        String actual = moduleAccessPrivileges.privileges().toString();
        boolean actualContainsEnum = actual.contains("READ") &&
                actual.contains("ALL") &&
                actual.contains("NONE") &&
                actual.contains("STAMP") &&
                actual.contains("UPDATE");
        assertTrue(actualContainsEnum);
    }

    @Test
    public void shouldReturnResultWhenApplication(){
        String actual = moduleAccessPrivileges.application().toString();
        assertEquals("OPERATION_SIMS",actual);
    }
}