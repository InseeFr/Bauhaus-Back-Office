package fr.insee.rmes.modules.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Source;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DomainUserServiceTest {

    private static final Object PRINCIPAL = "somePrincipal";
    private static final User ADMIN_USER = new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "insee");

    @Mock
    private UserDecoder userDecoder;

    @Mock
    private RbacFetcher rbacFetcher;

    private DomainUserService userService;

    @BeforeEach
    void setUp() {
        userService = new DomainUserService(userDecoder, rbacFetcher);
    }

    private void givenDecodedUser(User user) throws MissingUserInformationException {
        when(userDecoder.fromPrincipal(PRINCIPAL)).thenReturn(Optional.of(user));
    }

    private void givenNoDecodedUser() throws MissingUserInformationException {
        when(userDecoder.fromPrincipal(PRINCIPAL)).thenReturn(Optional.empty());
    }

    @Test
    void should_find_stamps_from_principal() throws MissingUserInformationException {
        givenDecodedUser(ADMIN_USER);

        Set<Stamp> result = userService.findStampsFrom(PRINCIPAL);

        assertThat(result).isNotNull();
        assertThat(result).extracting(Stamp::stamp).containsExactly("STAMP-01");
        verify(userDecoder).fromPrincipal(PRINCIPAL);
    }

    @Test
    void should_return_empty_stamps_when_user_not_found() throws MissingUserInformationException {
        givenNoDecodedUser();

        Set<Stamp> result = userService.findStampsFrom(PRINCIPAL);

        assertThat(result).isEmpty();
        verify(userDecoder).fromPrincipal(PRINCIPAL);
    }

    @Test
    void should_get_user() throws MissingUserInformationException {
        givenDecodedUser(ADMIN_USER);

        User result = userService.getUser(PRINCIPAL);

        assertThat(result).isEqualTo(ADMIN_USER);
        assertThat(result.id()).isEqualTo("user123");
        assertThat(result.roles()).containsExactly("ADMIN");
        verify(userDecoder).fromPrincipal(PRINCIPAL);
    }

    @Test
    void should_throw_exception_when_get_user_fails() throws MissingUserInformationException {
        givenNoDecodedUser();

        assertThatThrownBy(() -> userService.getUser(PRINCIPAL)).isInstanceOf(Exception.class);
    }

    @Test
    void should_compute_privileges() throws MissingUserInformationException {
        givenDecodedUser(new User("user123", List.of("ADMIN", "USER"), Set.of("STAMP-01"), "insee"));

        var privilege1 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL);
        var privilege2 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege1, privilege2));

        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of(modulePrivileges));

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(PRINCIPAL);

        assertThat(result).hasSize(1);
        assertThat(result).contains(modulePrivileges);
        verify(userDecoder).fromPrincipal(PRINCIPAL);
        verify(rbacFetcher).computePrivileges(List.of("ADMIN", "USER"), Source.INSEE);
    }

    @Test
    void should_compute_privileges_with_multiple_modules() throws MissingUserInformationException {
        givenDecodedUser(ADMIN_USER);

        var conceptPrivileges = new ModuleAccessPrivileges(
                RBAC.Module.CONCEPT_CONCEPT,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL)));
        var operationPrivileges = new ModuleAccessPrivileges(
                RBAC.Module.OPERATION_SERIES,
                Set.of(new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.STAMP)));

        when(rbacFetcher.computePrivileges(anyList(), any()))
                .thenReturn(Set.of(conceptPrivileges, operationPrivileges));

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(PRINCIPAL);

        assertThat(result).hasSize(2);
        assertThat(result).contains(conceptPrivileges, operationPrivileges);
    }

    @Test
    void should_handle_empty_roles() throws MissingUserInformationException {
        givenDecodedUser(new User("user123", List.of(), Set.of("STAMP-01"), "insee"));

        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of());

        Set<ModuleAccessPrivileges> result = userService.computePrivileges(PRINCIPAL);

        assertThat(result).isEmpty();
        verify(rbacFetcher).computePrivileges(List.of(), Source.INSEE);
    }

    @Test
    void should_find_multiple_stamps_from_principal() throws MissingUserInformationException {
        givenDecodedUser(new User("user123", List.of("ADMIN"), Set.of("STAMP-01", "STAMP-02", "STAMP-03"), "insee"));

        Set<Stamp> result = userService.findStampsFrom(PRINCIPAL);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Stamp::stamp).containsExactlyInAnyOrder("STAMP-01", "STAMP-02", "STAMP-03");
    }
}
