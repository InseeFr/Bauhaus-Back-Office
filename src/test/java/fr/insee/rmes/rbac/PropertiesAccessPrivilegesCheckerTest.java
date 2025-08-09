package fr.insee.rmes.rbac;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.rbac.ModuleAccessPrivileges.Privilege;
import fr.insee.rmes.rbac.RBAC.Module;
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

    private final User mockUser = new User("john.doe", List.of("ROLE_USER"), "myStamp");

    @BeforeEach
    void setUp() {
        fetcher = mock(RbacFetcher.class);
        decoder = mock(UserDecoder.class);
        checker = new PropertiesAccessPrivilegesChecker(fetcher, decoder, null, null);
    }

    @Test
    void shouldReturnFalseWhenUserNotPresent() throws RmesException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.empty());

        boolean result = checker.hasAccess("MODULE_1", "READ", "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenGetWithMissingRole() throws RmesException {
        var user = new User("jane.doe", List.of(), "unknownStamp");;
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenGetWithUnknownRole() throws RmesException {
        var user = new User("jane.doe", List.of("unknown"), "unknownStamp");;
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.READ.toString(), "","principal");

        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenUpdateWithMissingRole() throws RmesException {
        var user = new User("jane.doe", List.of(), "unknownStamp");;
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(user));

        boolean result = checker.hasAccess("OPERATION_FAMILY", RBAC.Privilege.UPDATE.toString(), "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenStrategyIsAll() throws RmesException {
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
    void shouldReturnFalseWhenStrategyIsNone() throws RmesException {
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
    void shouldReturnTrueWhenStrategyIsStampAndNotAppliedToObject() throws RmesException {
        User userWithDifferentStamp = new User("jane.doe", List.of("ROLE_USER"), "unknownStamp");
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
    void shouldReturnFalseWhenModuleNotFound() throws RmesException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(mockUser));
        when(fetcher.computePrivileges(mockUser.roles())).thenReturn(Set.of());

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenPrivilegeNotFound() throws RmesException {
        when(decoder.fromPrincipal("principal")).thenReturn(Optional.of(mockUser));
        when(fetcher.computePrivileges(mockUser.roles())).thenReturn(Set.of(
                new ModuleAccessPrivileges(Module.OPERATION_FAMILY, Set.of())
        ));

        boolean result = checker.hasAccess("OPERATION_FAMILY", "READ", "","principal");

        assertFalse(result);
    }
}
