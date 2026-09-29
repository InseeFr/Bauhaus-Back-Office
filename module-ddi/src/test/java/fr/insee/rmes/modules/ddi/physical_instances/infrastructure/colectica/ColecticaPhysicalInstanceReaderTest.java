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
import fr.insee.rmes.colectica.client.dto.ColecticaTypedSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest.IdentifierRef;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
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

    @Test
    void getPhysicalInstance_downloadsOnlyItsOwnItemTypesFromTheLatestVersion() {
        when(colecticaClient.getItem(AGENCY, PI_ID, null)).thenReturn(item(PHYSICAL_INSTANCE, PI_ID, 3));
        when(colecticaClient.querySet(
                        eq(new ColecticaSetItem(PI_ID, 3, AGENCY)),
                        argThat(types -> types.size() == 4
                                && types.containsAll(List.of(PHYSICAL_INSTANCE, DATA_RELATIONSHIP, VARIABLE, MMVR)))))
                .thenReturn(new ColecticaTypedSetItem[] {
                    new ColecticaTypedSetItem(new ColecticaSetItem(PI_ID, 3, AGENCY), PHYSICAL_INSTANCE),
                    new ColecticaTypedSetItem(new ColecticaSetItem("var-1", 2, AGENCY), VARIABLE)
                });
        ColecticaItemResponse[] items = {item(PHYSICAL_INSTANCE, PI_ID, 3), item(VARIABLE, "var-1", 2)};
        when(colecticaClient.getDescriptions(
                        List.of(new IdentifierRef(AGENCY, PI_ID, 3), new IdentifierRef(AGENCY, "var-1", 2))))
                .thenReturn(items);
        Ddi4Response converted = new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null, null, null);
        when(converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq(Ddi4Response.SCHEMA)))
                .thenReturn(converted);

        Ddi4Response result = reader.getPhysicalInstance(AGENCY, PI_ID);

        assertThat(result).isSameAs(converted);
        verify(colecticaClient, never()).getSet(anyString(), anyString(), any());
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
        return new ColecticaItemResponse(
                type, AGENCY, version, id, "<Fragment/>", null, null, false, false, false, null);
    }
}
