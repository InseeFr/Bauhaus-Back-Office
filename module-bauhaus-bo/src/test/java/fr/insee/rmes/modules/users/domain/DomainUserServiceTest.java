package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import fr.insee.rmes.domain.auth.Source;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainUserServiceTest {

    @Mock
    private UserDecoder userDecoder;

    @Mock
    private RbacFetcher rbacFetcher;

    private DomainUserService userService;

    @BeforeEach
    void setUp() {
        userService = new DomainUserService(userDecoder, rbacFetcher);
    }

    @Test
    void should_find_stamps_from_principal() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "insee");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        Set<Stamp> result = userService.findStampsFrom(principal);

        assertThat(result).isNotNull();
        assertThat(result).extracting(Stamp::stamp).containsExactly("STAMP-01");
        verify(userDecoder).fromPrincipal(principal);
    }

    @Test
    void should_return_empty_stamps_when_user_not_found() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.empty());

        Set<Stamp> result = userService.findStampsFrom(principal);

        assertThat(result).isEmpty();
        verify(userDecoder).fromPrincipal(principal);
    }

    @Test
    void should_get_user() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "insee");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        User result = userService.getUser(principal);

        assertThat(result).isEqualTo(user);
        assertThat(result.id()).isEqualTo("user123");
        assertThat(result.roles()).containsExactly("ADMIN");
        verify(userDecoder).fromPrincipal(principal);
    }

    @Test
    void should_throw_exception_when_get_user_fails() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(principal))
            .isInstanceOf(Exception.class);
    }

    @Test
    void should_compute_privileges() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN", "USER"), Set.of("STAMP-01"), "insee");
        Object principal = "somePrincipal";

        var privilege1 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL);
        var privilege2 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege1, privilege2));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of(modulePrivileges));

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(principal);

        assertThat(result).hasSize(1);
        assertThat(result).contains(modulePrivileges);
        verify(userDecoder).fromPrincipal(principal);
        verify(rbacFetcher).computePrivileges(eq(List.of("ADMIN", "USER")), eq(Source.INSEE));
    }

    @Test
    void should_compute_privileges_with_multiple_modules() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "insee");
        Object principal = "somePrincipal";

        var conceptPrivileges = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL))
        );
        var operationPrivileges = new ModuleAccessPrivileges(
            RBAC.Module.OPERATION_SERIES,
            Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP))
        );

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of(conceptPrivileges, operationPrivileges));

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(principal);

        assertThat(result).hasSize(2);
        assertThat(result).contains(conceptPrivileges, operationPrivileges);
    }

    @Test
    void should_handle_empty_roles() throws MissingUserInformationException {
        var user = new User("user123", List.of(), Set.of("STAMP-01"), "insee");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of());

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(principal);

        assertThat(result).isEmpty();
        verify(rbacFetcher).computePrivileges(eq(List.of()), eq(Source.INSEE));
    }

    @Test
    void should_find_multiple_stamps_from_principal() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), Set.of("STAMP-01", "STAMP-02", "STAMP-03"), "insee");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        Set<Stamp> result = userService.findStampsFrom(principal);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Stamp::stamp).containsExactlyInAnyOrder("STAMP-01", "STAMP-02", "STAMP-03");
    }
}
