package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration.ColecticaInstanceConfiguration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfiguredGroupsCodeListRefsProviderTest {

    private static final String CODE_LIST_TYPE = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
    private static final String DEFAULT_AGENCY = "fr.insee";

    @Mock
    private ColecticaClient colecticaClient;

    private final ColecticaInstanceConfiguration instanceConfiguration = new ColecticaInstanceConfiguration(
            "https://example.com",
            "/api/v1/",
            Map.of("CodeList", CODE_LIST_TYPE),
            "resp",
            "fmt",
            "token",
            null,
            null,
            DEFAULT_AGENCY);

    private ConfiguredGroupsCodeListRefsProvider provider(ItemReference... groups) {
        return new ConfiguredGroupsCodeListRefsProvider(instanceConfiguration, List.of(groups), colecticaClient);
    }

    private static ItemReference group(String identifier) {
        return new ItemReference(DEFAULT_AGENCY, identifier);
    }

    private void stubLatestVersions(ColecticaSetItem... latest) {
        when(colecticaClient.getLatestVersionNumbers(anyList())).thenReturn(List.of(latest));
    }

    private static ColecticaSetItem latest(String agencyId, String groupId, int version) {
        return new ColecticaSetItem(groupId, version, agencyId);
    }

    private void stubGroupChildren(String agencyId, String groupId, int version, ItemReference... codeLists) {
        when(colecticaClient.findRelatedDescriptions(
                        RelationshipDirection.BY_SUBJECT,
                        new ItemReference(agencyId, groupId),
                        version,
                        List.of(CODE_LIST_TYPE)))
                .thenReturn(Arrays.asList(codeLists));
    }

    @Test
    void queriesCodeListChildrenOfEachConfiguredGroup_usingDefaultAgency_oneCallPerGroup() {
        stubLatestVersions(latest(DEFAULT_AGENCY, "group-1", 1), latest(DEFAULT_AGENCY, "group-2", 1));
        stubGroupChildren(
                DEFAULT_AGENCY,
                "group-1",
                1,
                new ItemReference(DEFAULT_AGENCY, "cl-1"),
                new ItemReference(DEFAULT_AGENCY, "cl-2"));
        stubGroupChildren(DEFAULT_AGENCY, "group-2", 1, new ItemReference(DEFAULT_AGENCY, "cl-3"));

        List<ItemReference> refs = provider(group("group-1"), group("group-2")).codeListRefs();

        assertThat(refs)
                .containsExactly(
                        new ItemReference(DEFAULT_AGENCY, "cl-1"),
                        new ItemReference(DEFAULT_AGENCY, "cl-2"),
                        new ItemReference(DEFAULT_AGENCY, "cl-3"));

        // Direct group → CodeList only: one bysubject call per configured group, no package/scheme walk.
        verify(colecticaClient, times(2))
                .findRelatedDescriptions(eq(RelationshipDirection.BY_SUBJECT), any(), anyInt(), anyList());
        verify(colecticaClient, times(0))
                .findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyInt(), anyList());
    }

    @Test
    void deduplicatesCodeListRefsSharedAcrossGroups() {
        stubLatestVersions(latest(DEFAULT_AGENCY, "group-1", 1), latest(DEFAULT_AGENCY, "group-2", 1));
        stubGroupChildren(DEFAULT_AGENCY, "group-1", 1, new ItemReference(DEFAULT_AGENCY, "cl-shared"));
        stubGroupChildren(
                DEFAULT_AGENCY,
                "group-2",
                1,
                new ItemReference(DEFAULT_AGENCY, "cl-shared"),
                new ItemReference(DEFAULT_AGENCY, "cl-x"));

        List<ItemReference> refs = provider(group("group-1"), group("group-2")).codeListRefs();

        assertThat(refs)
                .containsExactly(
                        new ItemReference(DEFAULT_AGENCY, "cl-shared"), new ItemReference(DEFAULT_AGENCY, "cl-x"));
    }

    @Test
    void usesTheConfiguredAgency_forTheGroupTargetItem() {
        String otherAgency = "fr.insee.other";
        var provider = new ConfiguredGroupsCodeListRefsProvider(
                instanceConfiguration, List.of(new ItemReference(otherAgency, "group-1")), colecticaClient);
        stubLatestVersions(latest(otherAgency, "group-1", 1));
        stubGroupChildren(otherAgency, "group-1", 1, new ItemReference(otherAgency, "cl-1"));

        List<ItemReference> refs = provider.codeListRefs();

        assertThat(refs).containsExactly(new ItemReference(otherAgency, "cl-1"));
    }

    @Test
    void readsTheCodeListsOfTheLatestGroupVersionOnly() {
        stubLatestVersions(latest(DEFAULT_AGENCY, "group-1", 3));
        stubGroupChildren(DEFAULT_AGENCY, "group-1", 3, new ItemReference(DEFAULT_AGENCY, "cl-current"));

        List<ItemReference> refs = provider(group("group-1")).codeListRefs();

        assertThat(refs).containsExactly(new ItemReference(DEFAULT_AGENCY, "cl-current"));
        verify(colecticaClient).getLatestVersionNumbers(List.of(group("group-1")));
        verify(colecticaClient, times(0)).findRelatedDescriptions(any(), any(), anyList());
    }

    @Test
    void skipsAGroupUnknownToColectica() {
        stubLatestVersions(latest(DEFAULT_AGENCY, "group-2", 2));
        stubGroupChildren(DEFAULT_AGENCY, "group-2", 2, new ItemReference(DEFAULT_AGENCY, "cl-2"));

        List<ItemReference> refs =
                provider(group("group-missing"), group("group-2")).codeListRefs();

        assertThat(refs).containsExactly(new ItemReference(DEFAULT_AGENCY, "cl-2"));
    }

    @Test
    void emptyConfiguredGroups_returnsEmpty_withoutCallingColectica() {
        List<ItemReference> refs = provider().codeListRefs();

        assertThat(refs).isEmpty();
        verifyNoInteractions(colecticaClient);
    }
}
