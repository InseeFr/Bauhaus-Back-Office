package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DDI3toDDI4ConverterServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

class ColecticaPhysicalInstanceReaderTest {

    private static final String AGENCY = "fr.insee";
    private static final String PI_ID = "pi-1";
    private static final String PHYSICAL_INSTANCE = "pi-type";
    private static final String DATA_RELATIONSHIP = "dr-type";
    private static final String VARIABLE = "var-type";
    private static final String MMVR = "mmvr-type";
    private static final String CODE_LIST = "cl-type";
    private static final String CATEGORY = "cat-type";

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration =
            mock(ColecticaConfiguration.ColecticaInstanceConfiguration.class);
    private final DDI3toDDI4ConverterService converter = mock(DDI3toDDI4ConverterService.class);
    private final ColecticaClient colecticaClient = mock(ColecticaClient.class);

    private ColecticaPhysicalInstanceReader reader;

    @BeforeEach
    void setUp() {
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "PhysicalInstance", PHYSICAL_INSTANCE,
                        "DataRelationship", DATA_RELATIONSHIP,
                        "Variable", VARIABLE,
                        "ManagedMissingValuesRepresentation", MMVR,
                        "CodeList", CODE_LIST,
                        "Category", CATEGORY));
        reader = new ColecticaPhysicalInstanceReader(
                instanceConfiguration,
                converter,
                new ColecticaSetReader(instanceConfiguration, colecticaClient),
                colecticaClient);
    }

    /**
     * Le set d'une PI qui référence une nomenclature compte des dizaines de milliers d'items :
     * {@code _query/set} y coûte ~25 s même filtré. La PI est lue en descendant ses références, un
     * {@code _getListLatest} par niveau (dernière version, comme {@code set/}).
     */
    @Test
    void getPhysicalInstance_followsTheReferencesDownToTheMissingValuesWithoutQueryingTheSet() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenReturn(item(PHYSICAL_INSTANCE, PI_ID, 3, physicalInstanceXml(PI_ID, "dr-1")));
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "dr-1"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(DATA_RELATIONSHIP, "dr-1", 2, dataRelationshipXml("dr-1", "var-1"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "var-1"))))
                .thenReturn(new ColecticaItemResponse[] {item(VARIABLE, "var-1", 4, variableXml("var-1", "mmvr-1"))});
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "mmvr-1"))))
                .thenReturn(new ColecticaItemResponse[] {item(MMVR, "mmvr-1", 1, missingValuesXml("mmvr-1"))});

        Ddi4Response result = descendingReader.getPhysicalInstance(AGENCY, PI_ID);

        assertThat(result.topLevelReference()).containsExactly(Reference.of(AGENCY, PI_ID, "3", "PhysicalInstance"));
        assertThat(result.physicalInstance())
                .extracting(Ddi4PhysicalInstance::id)
                .containsExactly(PI_ID);
        assertThat(result.dataRelationship())
                .extracting(Ddi4DataRelationship::id)
                .containsExactly("dr-1");
        assertThat(result.variable()).extracting(Ddi4Variable::id).containsExactly("var-1");
        assertThat(result.managedMissingValuesRepresentation())
                .extracting(Ddi4ManagedMissingValuesRepresentation::id)
                .containsExactly("mmvr-1");
        verify(colecticaClient, never()).querySet(any(), any());
        verify(colecticaClient, never()).getSet(anyString(), anyString(), any());
    }

    @Test
    void getPhysicalInstance_withoutDataRelationshipReadsNothingButItsRoot() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenReturn(item(PHYSICAL_INSTANCE, PI_ID, 1, physicalInstanceXml(PI_ID, null)));

        Ddi4Response result = descendingReader.getPhysicalInstance(AGENCY, PI_ID);

        assertThat(result.physicalInstance())
                .extracting(Ddi4PhysicalInstance::id)
                .containsExactly(PI_ID);
        assertThat(result.variable()).isNull();
        verify(colecticaClient, never()).getLatestItems(any());
    }

    private ColecticaPhysicalInstanceReader readerWithRealConverter() {
        return new ColecticaPhysicalInstanceReader(
                instanceConfiguration,
                new DDI3toDDI4ConverterServiceImpl(instanceConfiguration.itemTypes()),
                new ColecticaSetReader(instanceConfiguration, colecticaClient),
                colecticaClient);
    }

    @Test
    void getPhysicalInstance_returnsNullWhenColecticaDoesNotKnowIt() {
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(reader.getPhysicalInstance(AGENCY, PI_ID)).isNull();
    }

    /**
     * #447 / #1591 : {@code /fichier} lit la PI en descendant ses références, sentinelles comprises
     * (MMVR, puis la CodeList de sentinelles, puis ses Category), sans {@code set/} ni {@code _getList}
     * du set : celui d'une PI qui référence une nomenclature compte des dizaines de milliers d'items.
     */
    @Test
    void getDataRelationships_followsTheReferencesDownToTheSentinelCategoriesWithoutReadingTheSet() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        stubPhysicalInstanceWithSentinels(null);

        Ddi4Response result = descendingReader.getDataRelationships(AGENCY, PI_ID, null);

        assertThat(result.topLevelReference()).containsExactly(Reference.of(AGENCY, PI_ID, "3", "PhysicalInstance"));
        assertThat(result.physicalInstance())
                .extracting(Ddi4PhysicalInstance::id)
                .containsExactly(PI_ID);
        assertThat(result.dataRelationship())
                .extracting(Ddi4DataRelationship::id)
                .containsExactly("dr-1");
        assertThat(result.variable()).extracting(Ddi4Variable::id).containsExactly("var-1");
        assertThat(result.managedMissingValuesRepresentation())
                .extracting(Ddi4ManagedMissingValuesRepresentation::id)
                .containsExactly("mmvr-1");
        assertThat(result.codeList()).extracting(Ddi4CodeList::id).containsExactly("cl-sentinel");
        assertThat(result.category()).extracting(Ddi4Category::id).containsExactly("cat-sentinel");
        verifyTheSetIsNeverRead();
    }

    @Test
    void getDataRelationships_readsTheRequestedVersionOfThePhysicalInstance() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        stubPhysicalInstanceWithSentinels("3");

        Ddi4Response result = descendingReader.getDataRelationships(AGENCY, PI_ID, "3");

        assertThat(result.physicalInstance())
                .extracting(Ddi4PhysicalInstance::id)
                .containsExactly(PI_ID);
        verify(colecticaClient).getItem(AGENCY, PI_ID, "3");
    }

    @Test
    void getDataRelationshipsXml_assemblesThePhysicalInstanceFirstThenTheItemsItReferences() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        stubPhysicalInstanceWithSentinels(null);

        String xml = descendingReader.getDataRelationshipsXml(AGENCY, PI_ID, null);

        assertThat(xml)
                .containsSubsequence(
                        "<r:ID>" + PI_ID + "</r:ID>",
                        "<r:ID>dr-1</r:ID>",
                        "<r:ID>var-1</r:ID>",
                        "<r:ID>mmvr-1</r:ID>",
                        "<r:ID>cl-sentinel</r:ID>",
                        "<r:ID>cat-sentinel</r:ID>");
        verifyTheSetIsNeverRead();
    }

    @Test
    void getDataRelationships_returnsNullWhenTheRootIsNotAPhysicalInstance() {
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenReturn(item(VARIABLE, PI_ID, 1, variableXml(PI_ID, null)));

        assertThat(reader.getDataRelationships(AGENCY, PI_ID, null)).isNull();
        assertThat(reader.getDataRelationshipsXml(AGENCY, PI_ID, null)).isNull();
        verify(colecticaClient, never()).getLatestItems(any());
    }

    @Test
    void getDataRelationships_returnsNullWhenColecticaDoesNotKnowThePhysicalInstance() {
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThat(reader.getDataRelationships(AGENCY, PI_ID, null)).isNull();
        assertThat(reader.getDataRelationshipsXml(AGENCY, PI_ID, null)).isNull();
    }

    /**
     * Une liste de sentinelles peut porter des dizaines de milliers de Category (IRIS : 33 059 codes).
     * Colectica sert {@code _getListLatest} deux fois plus vite en lots parallèles de 1000 qu'en un
     * seul appel (16 s au lieu de 36 s pour 31 130 Category, mesuré sur la démo).
     */
    @Test
    void getDataRelationships_readsTheCategoriesOfALargeSentinelCodeListInBatchesOfAThousand() {
        ColecticaPhysicalInstanceReader descendingReader = readerWithRealConverter();
        List<String> categoryIds =
                IntStream.range(0, 2500).mapToObj(i -> "cat-" + i).toList();
        when(colecticaClient.getItem(AGENCY, PI_ID, null))
                .thenReturn(item(PHYSICAL_INSTANCE, PI_ID, 3, physicalInstanceXml(PI_ID, "dr-1")));
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "dr-1"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(DATA_RELATIONSHIP, "dr-1", 2, dataRelationshipXml("dr-1", "var-1"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "var-1"))))
                .thenReturn(new ColecticaItemResponse[] {item(VARIABLE, "var-1", 4, variableXml("var-1", "mmvr-1"))});
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "mmvr-1"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(MMVR, "mmvr-1", 1, sentinelMissingValuesXml("mmvr-1", "cl-sentinel"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "cl-sentinel"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(CODE_LIST, "cl-sentinel", 1, codeListXml("cl-sentinel", categoryIds))
                });
        when(colecticaClient.getLatestItems(argThat(references -> references != null && references.size() > 1)))
                .thenAnswer(invocation -> invocation.<List<ItemReference>>getArgument(0).stream()
                        .map(reference ->
                                item(CATEGORY, reference.identifier(), 1, categoryXml(reference.identifier())))
                        .toArray(ColecticaItemResponse[]::new));

        Ddi4Response result = descendingReader.getDataRelationships(AGENCY, PI_ID, null);

        assertThat(result.category()).extracting(Ddi4Category::id).containsExactlyElementsOf(categoryIds);
        verify(colecticaClient, times(3)).getLatestItems(argThat(references -> references.size() > 1));
        verify(colecticaClient, never()).getLatestItems(argThat(references -> references.size() > 1000));
    }

    private void stubPhysicalInstanceWithSentinels(String version) {
        when(colecticaClient.getItem(AGENCY, PI_ID, version))
                .thenReturn(item(PHYSICAL_INSTANCE, PI_ID, 3, physicalInstanceXml(PI_ID, "dr-1")));
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "dr-1"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(DATA_RELATIONSHIP, "dr-1", 2, dataRelationshipXml("dr-1", "var-1"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "var-1"))))
                .thenReturn(new ColecticaItemResponse[] {item(VARIABLE, "var-1", 4, variableXml("var-1", "mmvr-1"))});
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "mmvr-1"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(MMVR, "mmvr-1", 1, sentinelMissingValuesXml("mmvr-1", "cl-sentinel"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "cl-sentinel"))))
                .thenReturn(new ColecticaItemResponse[] {
                    item(CODE_LIST, "cl-sentinel", 1, codeListXml("cl-sentinel", "cat-sentinel"))
                });
        when(colecticaClient.getLatestItems(List.of(new ItemReference(AGENCY, "cat-sentinel"))))
                .thenReturn(
                        new ColecticaItemResponse[] {item(CATEGORY, "cat-sentinel", 1, categoryXml("cat-sentinel"))});
    }

    private void verifyTheSetIsNeverRead() {
        verify(colecticaClient, never()).getSet(anyString(), anyString(), any());
        verify(colecticaClient, never()).getDescriptions(any());
        verify(colecticaClient, never()).querySet(any(), any());
    }

    @Test
    void getStoredItems_readsTheLatestVersionOfThePayloadItemsOnly() {
        Ddi4Response payload = new Ddi4Response(
                Ddi4Response.SCHEMA_ID,
                null,
                null,
                null,
                null,
                List.of(codeList("cl-1"), codeList("cl-2")),
                null,
                null);
        ColecticaItemResponse[] stored = {item("cl-type", "cl-1", 3)};
        when(colecticaClient.getLatestItems(
                        List.of(new ItemReference(AGENCY, "cl-1"), new ItemReference(AGENCY, "cl-2"))))
                .thenReturn(stored);
        Ddi4Response converted = new Ddi4Response(Ddi4Response.SCHEMA_ID, null, null, null, null, null, null, null);
        when(converter.convertDdi3ToDdi4(
                        argThat(ddi3 -> ddi3.items().size() == 1
                                && "cl-1".equals(ddi3.items().getFirst().identifier())),
                        eq(Ddi4Response.SCHEMA_ID)))
                .thenReturn(converted);

        assertThat(reader.getStoredItems(payload)).isSameAs(converted);
        verify(colecticaClient, never()).getSet(anyString(), anyString(), any());
    }

    @Test
    void getLatestVersions_pointsEachDistinctKnownTargetToItsLatestVersion() {
        Reference codeList = Reference.of(AGENCY, "cl-1", "1", Ddi4CodeList.TYPE_NAME);
        when(colecticaClient.getLatestVersionNumbers(
                        List.of(new ItemReference(AGENCY, "cl-1"), new ItemReference(AGENCY, "unknown"))))
                .thenReturn(List.of(new ColecticaSetItem("cl-1", 4, AGENCY)));

        List<Reference> latest = reader.getLatestVersions(
                List.of(codeList, codeList, Reference.of(AGENCY, "unknown", "1", Ddi4CodeList.TYPE_NAME)));

        assertThat(latest).containsExactly(Reference.of(AGENCY, "cl-1", "4", Ddi4CodeList.TYPE_NAME));
    }

    private static Ddi4CodeList codeList(String id) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE_NAME,
                null,
                Reference.synthesizeUrn(AGENCY, id, "1"),
                AGENCY,
                id,
                "1",
                null,
                null,
                List.of());
    }

    private static ColecticaItemResponse item(String type, String id, int version) {
        return item(type, id, version, "<Fragment/>");
    }

    private static ColecticaItemResponse item(String type, String id, int version, String xml) {
        return new ColecticaItemResponse(type, AGENCY, version, id, xml, null, null, false, false, false, null);
    }

    private static String fragment(String content) {
        return "<Fragment xmlns=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\">" + content + "</Fragment>";
    }

    private static String identity(String id) {
        return "<r:URN>urn:ddi:fr.insee:" + id + ":1</r:URN><r:Agency>fr.insee</r:Agency><r:ID>" + id
                + "</r:ID><r:Version>1</r:Version>";
    }

    private static String reference(String element, String id, String typeOfObject) {
        return "<" + element + "><r:Agency>fr.insee</r:Agency><r:ID>" + id + "</r:ID><r:Version>1</r:Version>"
                + "<r:TypeOfObject>" + typeOfObject + "</r:TypeOfObject></" + element + ">";
    }

    private static final String VERSIONED = " isUniversallyUnique=\"true\" versionDate=\"2026-01-01T00:00:00Z\"";

    private static String physicalInstanceXml(String id, String dataRelationshipId) {
        return fragment("<PhysicalInstance xmlns=\"ddi:physicalinstance:3_3\"" + VERSIONED + ">" + identity(id)
                + (dataRelationshipId == null
                        ? ""
                        : reference("r:DataRelationshipReference", dataRelationshipId, "DataRelationship"))
                + "</PhysicalInstance>");
    }

    private static String dataRelationshipXml(String id, String variableId) {
        return fragment("<DataRelationship xmlns=\"ddi:logicalproduct:3_3\"" + VERSIONED + ">" + identity(id)
                + "<LogicalRecord isUniversallyUnique=\"true\">" + identity("lr-" + id) + "<VariablesInRecord>"
                + reference("VariableUsedReference", variableId, "Variable")
                + "</VariablesInRecord></LogicalRecord></DataRelationship>");
    }

    private static String variableXml(String id, String missingValuesId) {
        return fragment("<Variable xmlns=\"ddi:logicalproduct:3_3\"" + VERSIONED + ">" + identity(id)
                + (missingValuesId == null
                        ? ""
                        : "<VariableRepresentation>"
                                + reference(
                                        "MissingValuesReference", missingValuesId, "ManagedMissingValuesRepresentation")
                                + "</VariableRepresentation>")
                + "</Variable>");
    }

    private static String missingValuesXml(String id) {
        return fragment("<r:ManagedMissingValuesRepresentation" + VERSIONED + ">" + identity(id)
                + "</r:ManagedMissingValuesRepresentation>");
    }

    private static String sentinelMissingValuesXml(String id, String codeListId) {
        return fragment("<r:ManagedMissingValuesRepresentation" + VERSIONED + ">" + identity(id)
                + "<r:MissingCodeRepresentation blankIsMissingValue=\"false\">"
                + reference("r:CodeListReference", codeListId, "CodeList")
                + "</r:MissingCodeRepresentation></r:ManagedMissingValuesRepresentation>");
    }

    private static String codeListXml(String id, String categoryId) {
        return codeListXml(id, List.of(categoryId));
    }

    private static String codeListXml(String id, List<String> categoryIds) {
        return fragment("<CodeList xmlns=\"ddi:logicalproduct:3_3\"" + VERSIONED + ">" + identity(id)
                + categoryIds.stream()
                        .map(categoryId -> "<Code isUniversallyUnique=\"true\">" + identity("code-" + categoryId)
                                + reference("r:CategoryReference", categoryId, "Category")
                                + "<r:Value>99</r:Value></Code>")
                        .collect(Collectors.joining())
                + "</CodeList>");
    }

    private static String categoryXml(String id) {
        return fragment("<Category xmlns=\"ddi:logicalproduct:3_3\"" + VERSIONED + ">" + identity(id) + "</Category>");
    }
}
