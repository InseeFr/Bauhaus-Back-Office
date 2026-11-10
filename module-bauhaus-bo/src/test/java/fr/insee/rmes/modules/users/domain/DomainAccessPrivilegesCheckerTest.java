package fr.insee.rmes.modules.users.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.StampFetchException;
import fr.insee.rmes.modules.users.domain.exceptions.UnsupportedModuleException;
import fr.insee.rmes.modules.users.domain.model.ModuleAccessPrivileges;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.domain.port.serverside.StampChecker;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

@ExtendWith(MockitoExtension.class)
class DomainAccessPrivilegesCheckerTest {

    private static final Object PRINCIPAL = "somePrincipal";
    private static final String RESOURCE_ID = "resource-id";
    private static final String DDI_GROUP_ID = "fr.insee|group-id";

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

    private static User userWithStamps(String source, String... stamps) {
        return new User("user123", List.of("USER"), Set.of(stamps), source);
    }

    private void givenUser(User user) throws MissingUserInformationException {
        when(userDecoder.fromPrincipal(PRINCIPAL)).thenReturn(Optional.of(user));
    }

    private void givenUserWithPrivilege(User user, RBAC.Module module, RBAC.Privilege privilege, RBAC.Strategy strategy)
            throws MissingUserInformationException {
        var modulePrivileges =
                new ModuleAccessPrivileges(module, Set.of(new ModuleAccessPrivileges.Privilege(privilege, strategy)));

        givenUser(user);
        when(rbacFetcher.computePrivileges(anyList(), any())).thenReturn(Set.of(modulePrivileges));
    }

    /** The user holds UPDATE with the STAMP strategy on OPERATION_SERIES. */
    private void givenSeriesUpdateWithStampStrategy(User user) throws MissingUserInformationException {
        givenUserWithPrivilege(user, RBAC.Module.OPERATION_SERIES, RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
    }

    private OngoingStubbing<List<String>> whenSeriesCreatorsStampsFetched()
            throws StampFetchException, UnsupportedModuleException {
        return when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, RESOURCE_ID));
    }

    /**
     * A user stamped STAMP-01 holds UPDATE with the STAMP strategy on the module, whose resource
     * has the given contributors stamps.
     */
    private void givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module module, String... contributorsStamps)
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(userWithStamps("ssm", "STAMP-01"), module, RBAC.Privilege.UPDATE, RBAC.Strategy.STAMP);
        when(stampChecker.getContributorsStamps(module, RESOURCE_ID)).thenReturn(List.of(contributorsStamps));
    }

    private boolean hasAccess(String module, String privilege) throws MissingUserInformationException {
        return accessChecker.hasAccess(module, privilege, RESOURCE_ID, PRINCIPAL);
    }

    /** The user holds CREATE with the STAMP strategy on DDI_PHYSICALINSTANCE; the group is created by stamp-A and stamp-B. */
    private void givenDdiGroupCreatedByStampsAAndB(User user)
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(user, RBAC.Module.DDI_PHYSICALINSTANCE, RBAC.Privilege.CREATE, RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, DDI_GROUP_ID))
                .thenReturn(List.of("stamp-A", "stamp-B"));
    }

    @Test
    void should_grant_access_for_insee_user_with_read_privilege() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("insee", "STAMP-01"),
                RBAC.Module.CONCEPT_CONCEPT,
                RBAC.Privilege.READ,
                RBAC.Strategy.ALL);

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "READ");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void should_deny_access_when_user_not_found() throws MissingUserInformationException {
        when(userDecoder.fromPrincipal(PRINCIPAL)).thenReturn(Optional.empty());

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "READ");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_grant_access_with_all_strategy() throws MissingUserInformationException {
        givenUserWithPrivilege(
                new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "ssm"),
                RBAC.Module.CONCEPT_CONCEPT,
                RBAC.Privilege.CREATE,
                RBAC.Strategy.ALL);

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "CREATE");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void should_deny_access_with_none_strategy() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.CONCEPT_CONCEPT,
                RBAC.Privilege.DELETE,
                RBAC.Strategy.NONE);

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "DELETE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_grant_access_with_stamp_strategy_when_stamp_matches()
            throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-01"));
        whenSeriesCreatorsStampsFetched().thenReturn(List.of("STAMP-01", "STAMP-02"));

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isTrue();
        verify(stampChecker).getCreatorsStamps(RBAC.Module.OPERATION_SERIES, RESOURCE_ID);
    }

    @Test
    void should_deny_access_with_stamp_strategy_when_stamp_does_not_match()
            throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-03"));
        whenSeriesCreatorsStampsFetched().thenReturn(List.of("STAMP-01", "STAMP-02"));

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_grant_access_with_stamp_strategy_when_no_stamps_required()
            throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-01"));
        whenSeriesCreatorsStampsFetched().thenReturn(List.of());

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void should_deny_access_when_stamp_fetch_fails()
            throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-01"));
        whenSeriesCreatorsStampsFetched().thenThrow(new StampFetchException(RBAC.Module.OPERATION_SERIES, RESOURCE_ID));

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_handle_dataset_distribution_module()
            throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, "STAMP-01");

        boolean hasAccess = hasAccess("DATASET_DISTRIBUTION", "UPDATE");

        assertThat(hasAccess).isTrue();
        verify(stampChecker).getContributorsStamps(RBAC.Module.DATASET_DISTRIBUTION, RESOURCE_ID);
    }

    @Test
    void should_handle_structure_module()
            throws Exception, MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.STRUCTURE_STRUCTURE, "STAMP-01");

        boolean hasAccess = hasAccess("STRUCTURE_STRUCTURE", "UPDATE");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void should_deny_access_when_no_matching_privilege() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ, RBAC.Strategy.ALL);

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "DELETE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_deny_access_when_no_matching_module() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ, RBAC.Strategy.ALL);

        boolean hasAccess = hasAccess("OPERATION_SERIES", "READ");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_deny_access_when_privilege_identifier_is_invalid() throws MissingUserInformationException {
        givenUser(userWithStamps("ssm", "STAMP-01"));

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "INVALID_PRIVILEGE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_deny_access_when_module_identifier_is_invalid() throws MissingUserInformationException {
        givenUser(userWithStamps("ssm", "STAMP-01"));

        boolean hasAccess = hasAccess("INVALID_MODULE", "READ");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_grant_access_with_stamp_strategy_when_user_has_multiple_stamps_and_one_matches()
            throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        // User has multiple stamps: STAMP-01, STAMP-02, STAMP-03
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-01", "STAMP-02", "STAMP-03"));
        // Resource only requires STAMP-02
        whenSeriesCreatorsStampsFetched().thenReturn(List.of("STAMP-02"));

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isTrue();
    }

    @Test
    void should_grant_access_for_ddi_physicalinstance_with_stamp_strategy_when_stamp_matches()
            throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        givenDdiGroupCreatedByStampsAAndB(userWithStamps("ssm", "stamp-A"));

        boolean hasAccess = accessChecker.hasAccess("DDI_PHYSICALINSTANCE", "CREATE", DDI_GROUP_ID, PRINCIPAL);

        assertThat(hasAccess).isTrue();
        verify(stampChecker).getCreatorsStamps(RBAC.Module.DDI_PHYSICALINSTANCE, DDI_GROUP_ID);
    }

    @Test
    void should_deny_access_for_ddi_physicalinstance_with_stamp_strategy_when_stamp_does_not_match()
            throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        givenDdiGroupCreatedByStampsAAndB(userWithStamps("ssm", "stamp-X"));

        boolean hasAccess = accessChecker.hasAccess("DDI_PHYSICALINSTANCE", "CREATE", DDI_GROUP_ID, PRINCIPAL);

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_deny_access_when_user_has_multiple_stamps_but_none_matches()
            throws Exception, StampFetchException, UnsupportedModuleException, MissingUserInformationException {
        // User has multiple stamps but none match the resource
        givenSeriesUpdateWithStampStrategy(userWithStamps("ssm", "STAMP-01", "STAMP-02"));
        // Resource requires STAMP-99 which user doesn't have
        whenSeriesCreatorsStampsFetched().thenReturn(List.of("STAMP-99"));

        boolean hasAccess = hasAccess("OPERATION_SERIES", "UPDATE");

        assertThat(hasAccess).isFalse();
    }

    @Test
    void should_handle_null_source_for_insee_check() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps(null, "STAMP-01"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ, RBAC.Strategy.ALL);

        boolean hasAccess = hasAccess("CONCEPT_CONCEPT", "READ");

        assertThat(hasAccess).isTrue();
    }

    /**
     * La stratégie ALL ouvre la lecture à tout le monde, mais écrire suppose un timbre : un
     * utilisateur sans timbre ne peut ni créer, ni modifier, ni supprimer, ni publier, ni administrer.
     */
    @ParameterizedTest
    @ValueSource(strings = {"CREATE", "UPDATE", "DELETE", "PUBLISH", "ADMINISTRATION"})
    void should_deny_a_write_privilege_with_all_strategy_to_a_user_without_any_stamp(String privilegeName)
            throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("insee"),
                RBAC.Module.CONCEPT_CONCEPT,
                RBAC.Privilege.valueOf(privilegeName),
                RBAC.Strategy.ALL);

        assertThat(hasAccess("CONCEPT_CONCEPT", privilegeName)).isFalse();
    }

    @Test
    void should_grant_a_read_privilege_with_all_strategy_to_a_user_without_any_stamp()
            throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("insee"), RBAC.Module.CONCEPT_CONCEPT, RBAC.Privilege.READ, RBAC.Strategy.ALL);

        assertThat(hasAccess("CONCEPT_CONCEPT", "READ")).isTrue();
    }

    @Test
    void should_check_the_contributors_stamps_of_a_component()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.STRUCTURE_COMPONENT, "STAMP-01");

        assertThat(hasAccess("STRUCTURE_COMPONENT", "UPDATE")).isTrue();
    }

    @Test
    void should_check_the_contributors_stamps_of_a_dataset()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.DATASET_DATASET, "STAMP-02");

        assertThat(hasAccess("DATASET_DATASET", "UPDATE")).isFalse();
    }
}
