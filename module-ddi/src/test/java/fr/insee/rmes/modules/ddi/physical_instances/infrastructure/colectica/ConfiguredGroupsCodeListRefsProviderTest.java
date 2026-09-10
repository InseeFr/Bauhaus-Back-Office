package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration.ColecticaInstanceConfiguration;
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

    private void stubGroupChildren(String groupId, ItemReference... codeLists) {
        when(colecticaClient.findRelatedDescriptions(
                        RelationshipDirection.BY_SUBJECT,
                        new ItemReference(DEFAULT_AGENCY, groupId),
                        List.of(CODE_LIST_TYPE)))
                .thenReturn(List.of(codeLists));
    }

    @Test
    void queriesCodeListChildrenOfEachConfiguredGroup_usingDefaultAgency_oneCallPerGroup() {
        stubGroupChildren(
                "group-1", new ItemReference(DEFAULT_AGENCY, "cl-1"), new ItemReference(DEFAULT_AGENCY, "cl-2"));
        stubGroupChildren("group-2", new ItemReference(DEFAULT_AGENCY, "cl-3"));

        List<ItemReference> refs = provider(group("group-1"), group("group-2")).codeListRefs();

        assertThat(refs)
                .containsExactly(
                        new ItemReference(DEFAULT_AGENCY, "cl-1"),
                        new ItemReference(DEFAULT_AGENCY, "cl-2"),
                        new ItemReference(DEFAULT_AGENCY, "cl-3"));

        // Direct group → CodeList only: one bysubject call per configured group, no package/scheme walk.
        verify(colecticaClient, times(2))
                .findRelatedDescriptions(eq(RelationshipDirection.BY_SUBJECT), any(), anyList());
        verify(colecticaClient, times(0))
                .findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyList());
    }

    @Test
    void deduplicatesCodeListRefsSharedAcrossGroups() {
        stubGroupChildren("group-1", new ItemReference(DEFAULT_AGENCY, "cl-shared"));
        stubGroupChildren(
                "group-2", new ItemReference(DEFAULT_AGENCY, "cl-shared"), new ItemReference(DEFAULT_AGENCY, "cl-x"));

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
        when(colecticaClient.findRelatedDescriptions(
                        RelationshipDirection.BY_SUBJECT,
                        new ItemReference(otherAgency, "group-1"),
                        List.of(CODE_LIST_TYPE)))
                .thenReturn(List.of(new ItemReference(otherAgency, "cl-1")));

        List<ItemReference> refs = provider.codeListRefs();

        assertThat(refs).containsExactly(new ItemReference(otherAgency, "cl-1"));
    }

    @Test
    void emptyConfiguredGroups_returnsEmpty_withoutCallingColectica() {
        List<ItemReference> refs = provider().codeListRefs();

        assertThat(refs).isEmpty();
        verifyNoInteractions(colecticaClient);
    }
}
