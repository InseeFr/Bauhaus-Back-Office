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
import java.util.Arrays;
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
    void should_grant_access_to_a_concept_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CONCEPT_CONCEPT, "STAMP-01");

        assertThat(hasAccess("CONCEPT_CONCEPT", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_concept_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CONCEPT_CONCEPT, "STAMP-02");

        assertThat(hasAccess("CONCEPT_CONCEPT", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_collection_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CONCEPT_COLLECTION, "STAMP-01");

        assertThat(hasAccess("CONCEPT_COLLECTION", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_collection_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CONCEPT_COLLECTION, "STAMP-02");

        assertThat(hasAccess("CONCEPT_COLLECTION", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_classification_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CLASSIFICATION_CLASSIFICATION, "STAMP-01");

        assertThat(hasAccess("CLASSIFICATION_CLASSIFICATION", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_classification_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CLASSIFICATION_CLASSIFICATION, "STAMP-02");

        assertThat(hasAccess("CLASSIFICATION_CLASSIFICATION", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_codes_list_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CODESLIST_CODESLIST, "STAMP-01");

        assertThat(hasAccess("CODESLIST_CODESLIST", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_codes_list_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CODESLIST_CODESLIST, "STAMP-02");

        assertThat(hasAccess("CODESLIST_CODESLIST", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_partial_codes_list_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CODESLIST_PARTIALCODESLIST, "STAMP-01");

        assertThat(hasAccess("CODESLIST_PARTIALCODESLIST", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_partial_codes_list_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.CODESLIST_PARTIALCODESLIST, "STAMP-02");

        assertThat(hasAccess("CODESLIST_PARTIALCODESLIST", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_family_whose_contributor_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.OPERATION_FAMILY, "STAMP-01");

        assertThat(hasAccess("OPERATION_FAMILY", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_family_whose_contributor_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.OPERATION_FAMILY, "STAMP-02");

        assertThat(hasAccess("OPERATION_FAMILY", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_an_operation_whose_parent_series_creator_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_OPERATION,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_OPERATION, RESOURCE_ID))
                .thenReturn(List.of("STAMP-01"));

        assertThat(hasAccess("OPERATION_OPERATION", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_an_operation_whose_parent_series_creator_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_OPERATION,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_OPERATION, RESOURCE_ID))
                .thenReturn(List.of("STAMP-02"));

        assertThat(hasAccess("OPERATION_OPERATION", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_an_indicator_whose_creator_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_INDICATOR,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_INDICATOR, RESOURCE_ID))
                .thenReturn(List.of("STAMP-01"));

        assertThat(hasAccess("OPERATION_INDICATOR", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_an_indicator_whose_creator_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_INDICATOR,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_INDICATOR, RESOURCE_ID))
                .thenReturn(List.of("STAMP-02"));

        assertThat(hasAccess("OPERATION_INDICATOR", "UPDATE")).isFalse();
    }

    @Test
    void should_grant_access_to_a_sims_whose_target_creator_matches_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_SIMS,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SIMS, RESOURCE_ID))
                .thenReturn(List.of("STAMP-01"));

        assertThat(hasAccess("OPERATION_SIMS", "UPDATE")).isTrue();
    }

    @Test
    void should_deny_access_to_a_sims_whose_target_creator_does_not_match_the_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_SIMS,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.STAMP);
        when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SIMS, RESOURCE_ID))
                .thenReturn(List.of("STAMP-02"));

        assertThat(hasAccess("OPERATION_SIMS", "UPDATE")).isFalse();
    }

    @Test
    void should_check_the_contributors_stamps_of_a_dataset()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenUpdateWithStampStrategyOnContributorsStamps(RBAC.Module.DATASET_DATASET, "STAMP-02");

        assertThat(hasAccess("DATASET_DATASET", "UPDATE")).isFalse();
    }

    /**
     * Création sous la stratégie STAMP : l'utilisateur, timbré STAMP-01 et STAMP-02, doit compter
     * l'un de ses timbres parmi les organisations propriétaires postées.
     */
    private void givenFamilyCreateWithStrategy(RBAC.Strategy strategy) throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01", "STAMP-02"),
                RBAC.Module.OPERATION_FAMILY,
                RBAC.Privilege.CREATE,
                strategy);
    }

    private boolean hasFamilyCreateAccess(List<String> owners) throws MissingUserInformationException {
        return accessChecker.hasCreateAccess("OPERATION_FAMILY", owners, PRINCIPAL);
    }

    @Test
    void should_grant_creation_with_stamp_strategy_when_one_owner_is_a_user_stamp()
            throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.STAMP);
        var owners =
                List.of("http://bauhaus/organisations/insee/DG75-G999", "http://bauhaus/organisations/insee/STAMP-02");
        when(stampChecker.toStamps(owners)).thenReturn(List.of("DG75-G999", "STAMP-02"));

        assertThat(hasFamilyCreateAccess(owners)).isTrue();
    }

    @Test
    void should_deny_creation_with_stamp_strategy_when_no_owner_is_a_user_stamp()
            throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.STAMP);
        var owners = List.of("http://bauhaus/organisations/insee/DG75-G999");
        when(stampChecker.toStamps(owners)).thenReturn(List.of("DG75-G999"));

        assertThat(hasFamilyCreateAccess(owners)).isFalse();
    }

    /** Une ressource créée sans propriétaire échapperait ensuite à tout contrôle par timbre. */
    @Test
    void should_deny_creation_with_stamp_strategy_without_any_owner() throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.STAMP);

        assertThat(hasFamilyCreateAccess(List.of())).isFalse();
    }

    @Test
    void should_deny_creation_with_stamp_strategy_when_the_owners_are_absent_from_the_body()
            throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.STAMP);

        assertThat(hasFamilyCreateAccess(null)).isFalse();
    }

    /** La stratégie ALL laisse créer au nom de n'importe quelle organisation. */
    @Test
    void should_grant_creation_with_all_strategy_whatever_the_owners() throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.ALL);

        assertThat(hasFamilyCreateAccess(List.of("http://bauhaus/organisations/insee/DG75-G999")))
                .isTrue();
    }

    @Test
    void should_deny_creation_without_the_create_privilege() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_FAMILY,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.ALL);

        assertThat(hasFamilyCreateAccess(List.of("STAMP-01"))).isFalse();
    }

    /** {@code "contributor": [null, "STAMP-01"]} : les valeurs nulles du corps sont ignorées. */
    @Test
    void should_ignore_null_owners_posted_in_the_request() throws MissingUserInformationException {
        givenFamilyCreateWithStrategy(RBAC.Strategy.STAMP);
        when(stampChecker.toStamps(List.of("STAMP-01"))).thenReturn(List.of("STAMP-01"));

        assertThat(hasFamilyCreateAccess(Arrays.asList(null, "STAMP-01"))).isTrue();
    }
    /**
     * Création d'une opération sous la stratégie STAMP : elle n'a pas de créateur propre, ce sont
     * ceux de la série parente, déjà en base, qui doivent porter un timbre de l'utilisateur.
     */
    private void givenOperationCreateWithStrategy(RBAC.Strategy strategy) throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01", "STAMP-02"),
                RBAC.Module.OPERATION_OPERATION,
                RBAC.Privilege.CREATE,
                strategy);
    }

    private OngoingStubbing<List<String>> whenParentSeriesCreatorsStampsFetched()
            throws StampFetchException, UnsupportedModuleException {
        return when(stampChecker.getCreatorsStamps(RBAC.Module.OPERATION_SERIES, "s42"));
    }

    private boolean hasOperationCreateAccessUnderSeries() throws MissingUserInformationException {
        return accessChecker.hasCreateAccessUnder("OPERATION_OPERATION", "OPERATION_SERIES", "s42", PRINCIPAL);
    }

    @Test
    void should_grant_operation_creation_when_a_parent_series_creator_is_a_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenOperationCreateWithStrategy(RBAC.Strategy.STAMP);
        whenParentSeriesCreatorsStampsFetched().thenReturn(List.of("DG75-G999", "STAMP-02"));

        assertThat(hasOperationCreateAccessUnderSeries()).isTrue();
    }

    @Test
    void should_deny_operation_creation_when_no_parent_series_creator_is_a_user_stamp()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenOperationCreateWithStrategy(RBAC.Strategy.STAMP);
        whenParentSeriesCreatorsStampsFetched().thenReturn(List.of("DG75-G999"));

        assertThat(hasOperationCreateAccessUnderSeries()).isFalse();
    }

    /** Une série sans créateur ne laisse personne y rattacher d'opération sous la stratégie STAMP. */
    @Test
    void should_deny_operation_creation_when_the_parent_series_has_no_creator()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenOperationCreateWithStrategy(RBAC.Strategy.STAMP);
        whenParentSeriesCreatorsStampsFetched().thenReturn(List.of());

        assertThat(hasOperationCreateAccessUnderSeries()).isFalse();
    }

    @Test
    void should_deny_operation_creation_when_the_parent_series_creators_cannot_be_fetched()
            throws MissingUserInformationException, StampFetchException, UnsupportedModuleException {
        givenOperationCreateWithStrategy(RBAC.Strategy.STAMP);
        whenParentSeriesCreatorsStampsFetched().thenThrow(new StampFetchException(RBAC.Module.OPERATION_SERIES, "s42"));

        assertThat(hasOperationCreateAccessUnderSeries()).isFalse();
    }

    /** La stratégie ALL laisse créer sous n'importe quelle série, sans relire ses créateurs. */
    @Test
    void should_grant_operation_creation_with_all_strategy_whatever_the_parent_series()
            throws MissingUserInformationException {
        givenOperationCreateWithStrategy(RBAC.Strategy.ALL);

        assertThat(hasOperationCreateAccessUnderSeries()).isTrue();
    }

    @Test
    void should_deny_operation_creation_without_the_create_privilege() throws MissingUserInformationException {
        givenUserWithPrivilege(
                userWithStamps("ssm", "STAMP-01"),
                RBAC.Module.OPERATION_OPERATION,
                RBAC.Privilege.UPDATE,
                RBAC.Strategy.ALL);

        assertThat(hasOperationCreateAccessUnderSeries()).isFalse();
    }
}
