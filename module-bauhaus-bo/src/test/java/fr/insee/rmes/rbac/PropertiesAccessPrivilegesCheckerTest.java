package fr.insee.rmes.rbac;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege;
import fr.insee.rmes.modules.users.domain.model.RBAC.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PropertiesAccessPrivilegesCheckerTest {

    private RbacFetcher fetcher;
    private UserDecoder decoder;
    private PropertiesAccessPrivilegesChecker checker;

    private final User mockUser = new User("john.doe", List.of("ROLE_USER"), "myStamp", "ssm");

    @BeforeEach
    void setUp() {
        fetcher = mock(RbacFetcher.class);
        decoder = mock(UserDecoder.class);
        checker = new PropertiesAccessPrivilegesChecker(fetcher, decoder, null, null);
    }

    @Test
    void shouldReturnFalseWhenUserNotPresent() throws RmesException, MissingUserInformationException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.empty());

        boolean result = checker.hasAccess("MODULE_1", "READ", "","principal");

        assertFalse(result);
    }

    @Test

    void shouldReturnTrueWhenGetWithMissingRole() throws RmesException, MissingUserInformationException {
        var user = new User("jane.doe", List.of(), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenGetWithUnknownRole() throws RmesException, MissingUserInformationException {
        var user = new User("jane.doe", List.of("unknown"), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenUpdateWithMissingRole() throws RmesException, MissingUserInformationException {
        var user = new User("jane.doe", List.of(), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.UPDATE.toString(), "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenStrategyIsAll() throws RmesException, MissingUserInformationException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(mockUser));
        when(fetcher.computePrivileges(mockUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenStrategyIsNone() throws RmesException, MissingUserInformationException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(mockUser));
        when(fetcher.computePrivileges(mockUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.READ, RBAC.Strategy.NONE)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenStrategyIsStampAndNotAppliedToObject() throws RmesException, MissingUserInformationException {
        User userWithDifferentStamp = new User("jane.doe", List.of("ROLE_USER"), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(userWithDifferentStamp));
        when(fetcher.computePrivileges(userWithDifferentStamp.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertTrue(result);
    }


    @Test
    void shouldReturnFalseWhenPrivilegeNotFound() throws RmesException, MissingUserInformationException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(mockUser));
        when(fetcher.computePrivileges(mockUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of())
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenReadOperationAndInseeSource() throws RmesException, MissingUserInformationException {
        // User with insee source should always have READ access regardless of RBAC configuration
        var inseeUser = new User("jane.doe", List.of(), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(inseeUser));
        // Even with no privileges configured or NONE strategy, should return true for READ + insee
        when(fetcher.computePrivileges(inseeUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.READ, RBAC.Strategy.NONE)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertTrue(result);
    }

    @Test
    void shouldNotReturnTrueWhenUpdateOperationAndInseeSource() throws RmesException, MissingUserInformationException {
        // INSEE source should not bypass RBAC for non-READ operations
        var inseeUser = new User("jane.doe", List.of(), "unknownStamp", "insee");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(inseeUser));
        when(fetcher.computePrivileges(inseeUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.NONE)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.UPDATE.toString(), "","principal");

        assertFalse(result);
    }

    @Test
    void shouldNotReturnTrueWhenReadOperationAndNonInseeSource() throws RmesException, MissingUserInformationException {
        // Non-INSEE source should not bypass RBAC even for READ operations
        var nonInseeUser = new User("jane.doe", List.of(), "unknownStamp", "proconnect");
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(nonInseeUser));
        when(fetcher.computePrivileges(nonInseeUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of(
                        new Privilege(RBAC.Privilege.READ, RBAC.Strategy.NONE)
                ))
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertFalse(result);
    }
}
