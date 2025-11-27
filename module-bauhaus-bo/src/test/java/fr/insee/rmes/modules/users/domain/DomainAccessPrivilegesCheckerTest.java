package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainAccessPrivilegesCheckerTest {

    @Mock
    private RbacFetcher rbacFetcher;

    @Mock
    private UserDecoder userDecoder;

    @Mock
    private StampChecker stampChecker;

    private DomainAccessPrivilegesChecker accessChecker;

    @BeforeEach
    void setUp() {
        accessChecker = new DomainAccessPrivilegesChecker(rbacFetcher, userDecoder, stampChecker);
    }

    @Test
    void shouldGrantAccessForInseeUserWithReadPrivilege() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "insee");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "READ", "resource-id", principal);

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldDenyAccessWhenUserNotFound() throws MissingUserInformationException {
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.empty());

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "READ", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldGrantAccessWithAllStrategy() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.CREATE, RBAC.Strategy.ALL);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "CREATE", "resource-id", principal);

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldDenyAccessWithNoneStrategy() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.DELETE, RBAC.Strategy.NONE);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "DELETE", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldGrantAccessWithStampStrategy_whenStampMatches() throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "resource-id"))
            .thenReturn(List.of("STAMP-01", "STAMP-02"));

        boolean hasAccess = accessChecker.hasAccess("OPERATION_SERIES", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isTrue();
        verify(stampChecker).getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "resource-id");
    }

    @Test
    void shouldDenyAccessWithStampStrategy_whenStampDoesNotMatch() throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        var user = new User("user123", List.of("USER"), "STAMP-03", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "resource-id"))
            .thenReturn(List.of("STAMP-01", "STAMP-02"));

        boolean hasAccess = accessChecker.hasAccess("OPERATION_SERIES", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldGrantAccessWithStampStrategy_whenNoStampsRequired() throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "resource-id"))
            .thenReturn(List.of());

        boolean hasAccess = accessChecker.hasAccess("OPERATION_SERIES", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldDenyAccessWhenStampFetchFails() throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.OPERATION_SERIES, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "resource-id"))
            .thenThrow(new StampFetchException(RBAC.Module.OPERATION_SERIES, "resource-id"));

        boolean hasAccess = accessChecker.hasAccess("OPERATION_SERIES", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldHandleDatasetDistributionModule() throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.DATASET_DISTRIBUTION, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, "resource-id"))
            .thenReturn(List.of("STAMP-01"));

        boolean hasAccess = accessChecker.hasAccess("DATASET_DISTRIBUTION", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isTrue();
        verify(stampChecker).getContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, "resource-id");
    }

    @Test
    void shouldHandleStructureModule() throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.STRUCTURE_STRUCTURE, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));
        when(stampChecker.getContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, "resource-id"))
            .thenReturn(List.of("STAMP-01"));

        boolean hasAccess = accessChecker.hasAccess("STRUCTURE_STRUCTURE", "UPDATE", "resource-id", principal);

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldDenyAccessWhenNoMatchingPrivilege() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "DELETE", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldDenyAccessWhenNoMatchingModule() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));

        boolean hasAccess = accessChecker.hasAccess("OPERATION_SERIES", "READ", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldDenyAccessWhenPrivilegeIdentifierIsInvalid() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "INVALID_PRIVILEGE", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldDenyAccessWhenModuleIdentifierIsInvalid() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", "ssm");
        Object principal = "somePrincipal";

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));

        boolean hasAccess = accessChecker.hasAccess("INVALID_MODULE", "READ", "resource-id", principal);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void shouldHandleNullSourceForInseeCheck() throws MissingUserInformationException {
        var user = new User("user123", List.of("USER"), "STAMP-01", null);
        Object principal = "somePrincipal";

        var privilege = new ModuleAccessPrivileges.Privilege(RBAC.Privilege.READ, RBAC.Strategy.ALL);
        var modulePrivileges = new ModuleAccessPrivileges(RBAC.Module.CONCEPT_CONCEPT, Set.of(privilege));

        when(userDecoder.fromPrincipal(principal)).thenReturn(Optional.of(user));
        when(rbacFetcher.computePrivileges(anyList())).thenReturn(Set.of(modulePrivileges));

        boolean hasAccess = accessChecker.hasAccess("CONCEPT_CONCEPT", "READ", "resource-id", principal);

        assertThat(hasAccess).isTrue();
    }
}
