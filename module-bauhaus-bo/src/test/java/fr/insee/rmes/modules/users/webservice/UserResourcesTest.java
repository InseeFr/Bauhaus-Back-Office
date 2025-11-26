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
import static org.mockito.ArgumentMatchers.any;
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
    void shouldGetUserInformation() throws MissingUserInformationException {
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
    void shouldReturnEmptySetWhenUserHasNoPrivileges() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.computePrivileges(principal)).thenReturn(Set.of());

        Set<ModuleAccessPrivileges> result = userResources.getUserInformation(principal);

        assertThat(result).isEmpty();
        verify(userService).computePrivileges(principal);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserInformationIsMissing() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.computePrivileges(principal))
            .thenThrow(new MissingUserInformationException("User information is missing"));

        assertThatThrownBy(() -> userResources.getUserInformation(principal))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED)
            .hasMessageContaining("User information is missing");
    }

    @Test
    void shouldGetStamp() throws MissingUserInformationException {
        Object principal = "somePrincipal";
        Stamp expectedStamp = new Stamp("STAMP-01");

        when(userService.findStampFrom(principal)).thenReturn(expectedStamp);

        Stamp result = userResources.getStamp(principal);

        assertThat(result).isEqualTo(expectedStamp);
        assertThat(result.stamp()).isEqualTo("STAMP-01");
        verify(userService).findStampFrom(principal);
    }

    @Test
    void shouldReturnNullStampWhenNotFound() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.findStampFrom(principal)).thenReturn(null);

        Stamp result = userResources.getStamp(principal);

        assertThat(result).isNull();
        verify(userService).findStampFrom(principal);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenStampCannotBeRetrieved() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userService.findStampFrom(principal))
            .thenThrow(new MissingUserInformationException("Cannot retrieve stamp"));

        assertThatThrownBy(() -> userResources.getStamp(principal))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED)
            .hasMessageContaining("Cannot retrieve stamp");
    }

    @Test
    void shouldHandleMultipleModulePrivileges() throws MissingUserInformationException {
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
    void shouldHandleStampWithEmptyValue() throws MissingUserInformationException {
        Object principal = "somePrincipal";
        Stamp emptyStamp = new Stamp("");

        when(userService.findStampFrom(principal)).thenReturn(emptyStamp);

        Stamp result = userResources.getStamp(principal);

        assertThat(result).isEqualTo(emptyStamp);
        assertThat(result.stamp()).isEmpty();
    }

    @Test
    void shouldHandleDifferentPrincipalTypes() throws MissingUserInformationException {
        // Test with String principal
        String stringPrincipal = "stringPrincipal";
        Stamp stamp1 = new Stamp("STAMP-01");
        when(userService.findStampFrom(stringPrincipal)).thenReturn(stamp1);

        Stamp result1 = userResources.getStamp(stringPrincipal);
        assertThat(result1).isEqualTo(stamp1);

        // Test with Object principal
        Object objectPrincipal = new Object();
        Stamp stamp2 = new Stamp("STAMP-02");
        when(userService.findStampFrom(objectPrincipal)).thenReturn(stamp2);

        Stamp result2 = userResources.getStamp(objectPrincipal);
        assertThat(result2).isEqualTo(stamp2);
    }
}
