package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
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
                        "CodeList", "cl-type",
                        "Category", "cat-type"));
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

    @Test
    void getStoredItems_readsTheLatestVersionOfThePayloadItemsOnly() {
        Ddi4Response payload = new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, null, null, List.of(codeList("cl-1"), codeList("cl-2")), null, null);
        ColecticaItemResponse[] stored = {item("cl-type", "cl-1", 3)};
        when(colecticaClient.getLatestItems(
                        List.of(new ItemReference(AGENCY, "cl-1"), new ItemReference(AGENCY, "cl-2"))))
                .thenReturn(stored);
        Ddi4Response converted = new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null, null, null);
        when(converter.convertDdi3ToDdi4(
                        argThat(ddi3 -> ddi3.items().size() == 1
                                && "cl-1".equals(ddi3.items().getFirst().identifier())),
                        eq(Ddi4Response.SCHEMA)))
                .thenReturn(converted);

        assertThat(reader.getStoredItems(payload)).isSameAs(converted);
        verify(colecticaClient, never()).getSet(anyString(), anyString(), any());
    }

    @Test
    void getLatestVersions_pointsEachDistinctKnownTargetToItsLatestVersion() {
        Reference codeList = Reference.of(AGENCY, "cl-1", "1", Ddi4CodeList.TYPE);
        when(colecticaClient.getLatestVersionNumbers(
                        List.of(new ItemReference(AGENCY, "cl-1"), new ItemReference(AGENCY, "unknown"))))
                .thenReturn(List.of(new ColecticaSetItem("cl-1", 4, AGENCY)));

        List<Reference> latest = reader.getLatestVersions(
                List.of(codeList, codeList, Reference.of(AGENCY, "unknown", "1", Ddi4CodeList.TYPE)));

        assertThat(latest).containsExactly(Reference.of(AGENCY, "cl-1", "4", Ddi4CodeList.TYPE));
    }

    private static Ddi4CodeList codeList(String id) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
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
                + "<VariableRepresentation>"
                + reference("MissingValuesReference", missingValuesId, "ManagedMissingValuesRepresentation")
                + "</VariableRepresentation></Variable>");
    }

    private static String missingValuesXml(String id) {
        return fragment("<r:ManagedMissingValuesRepresentation" + VERSIONED + ">" + identity(id)
                + "</r:ManagedMissingValuesRepresentation>");
    }
}
