package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserResourcesTest {

    @Mock
    private UserService userService;

    private UserResources userResources;

    @BeforeEach
    void setUp() {
        userResources = new UserResources(userService);
    }

    @Test
    void should_get_user_information() throws MissingUserInformationException {
        Object principal = "somePrincipal";
        var privilege1 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var privilege2 = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);

        var conceptPrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege1));
        var operationPrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(privilege2));

        Set<ModuleAccessPrivileges> expectedPrivileges = Set.of(conceptPrivileges, operationPrivileges);

        when(userService.computePrivileges(principal)).thenReturn(expectedPrivileges);

        Set<ModuleAccessPrivileges> result = userResources.getUserInformation(principal);

        assertThat(result).isEqualTo(expectedPrivileges);
        assertThat(result).hasSize(2);
        assertThat(result).contains(conceptPrivileges, operationPrivileges);
        verify(userService).computePrivileges(principal);
    }

    @Test
    void should_return_empty_set_when_user_has_no_privileges() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.computePrivileges(principal)).thenReturn(Set.of());

        Set<ModuleAccessPrivileges> result = userResources.getUserInformation(principal);

        assertThat(result).isEmpty();
        verify(userService).computePrivileges(principal);
    }

    @Test
    void should_throw_unauthorized_exception_when_user_information_is_missing() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.computePrivileges(principal))
            .thenThrow(new MissingUserInformationException("User information is missing"));

        assertThatThrownBy(() -> userResources.getUserInformation(principal))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED)
            .hasMessageContaining("User information is missing");
    }

    @Test
    void should_get_stamps() throws MissingUserInformationException {
        Object principal = "somePrincipal";
        var expectedStamps = Set.of(new Stamp("STAMP-01"));

        when(userService.findStampsFrom(principal)).thenReturn(expectedStamps);

        var result = userResources.getStamps(principal);

        assertThat(result).isEqualTo(expectedStamps);
        assertThat(result).extracting(Stamp::stamp).containsExactly("STAMP-01");
        verify(userService).findStampsFrom(principal);
    }

    @Test
    void should_return_empty_set_when_stamps_not_found() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.findStampsFrom(principal)).thenReturn(Set.of());

        Set<Stamp> result = userResources.getStamps(principal);

        assertThat(result).isEmpty();
        verify(userService).findStampsFrom(principal);
    }

    @Test
    void should_throw_unauthorized_exception_when_stamps_cannot_be_retrieved() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.findStampsFrom(principal))
            .thenThrow(new MissingUserInformationException("Cannot retrieve stamps"));

        assertThatThrownBy(() -> userResources.getStamps(principal))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED)
            .hasMessageContaining("Cannot retrieve stamps");
    }

    @Test
    void should_handle_multiple_module_privileges() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        var readPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var createPrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL);
        var updatePrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var deletePrivilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE);

        var conceptPrivileges = new ModuleAccessPrivileges(
            RBAC.Module.CONCEPT_CONCEPT,
            Set.of(readPrivilege, createPrivilege, updatePrivilege, deletePrivilege)
        );

        Set<ModuleAccessPrivileges> expectedPrivileges = Set.of(conceptPrivileges);

        when(userService.computePrivileges(principal)).thenReturn(expectedPrivileges);

        Set<ModuleAccessPrivileges> result = userResources.getUserInformation(principal);

        assertThat(result).hasSize(1);
        ModuleAccessPrivileges modulePrivileges = result.iterator().next();
        assertThat(modulePrivileges.application()).isEqualTo(RBAC.Module.CONCEPT_CONCEPT);
        assertThat(modulePrivileges.privileges()).hasSize(4);
    }

    @Test
    void should_handle_stamps_with_empty_value() throws MissingUserInformationException {
        Object principal = "somePrincipal";
        Set<Stamp> emptyStamps = Set.of(new Stamp(""));

        when(userService.findStampsFrom(principal)).thenReturn(emptyStamps);

        Set<Stamp> result = userResources.getStamps(principal);

        assertThat(result).isEqualTo(emptyStamps);
        assertThat(result.iterator().next().stamp()).isEmpty();
    }

    @Test
    void should_handle_different_principal_types() throws MissingUserInformationException {
        // Test with String principal
        String stringPrincipal = "stringPrincipal";
        Set<Stamp> stamps1 = Set.of(new Stamp("STAMP-01"));
        when(userService.findStampsFrom(stringPrincipal)).thenReturn(stamps1);

        Set<Stamp> result1 = userResources.getStamps(stringPrincipal);
        assertThat(result1).isEqualTo(stamps1);

        // Test with Object principal
        Object objectPrincipal = new Object();
        Set<Stamp> stamps2 = Set.of(new Stamp("STAMP-02"));
        when(userService.findStampsFrom(objectPrincipal)).thenReturn(stamps2);

        Set<Stamp> result2 = userResources.getStamps(objectPrincipal);
        assertThat(result2).isEqualTo(stamps2);
    }
}
