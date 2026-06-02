package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConverter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters.GroupDDIItemConverter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters.StudyUnitDDIItemConverter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DDIItemConvertServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String GROUP_FRAGMENT = """
            <Fragment xmlns="ddi:instance:3_3">
                <ddi:Group xmlns:ddi="ddi:group:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:abc:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">abc</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                </ddi:Group>
            </Fragment>
            """;

    private static final String CODELIST_FRAGMENT = """
            <Fragment xmlns="ddi:instance:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:cl-1:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">cl-1</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                </CodeList>
            </Fragment>
            """;

    private static final Map<String, String> ITEM_TYPES = Map.of(
            "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
            "Category", "7e47c269-bcae-44e2-a3ce-49b417a2f877"
    );

    private static final Map<String, String> FULL_ITEM_TYPES = Map.of(
            "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
            "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
            "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
            "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
            "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
    );

    private static final String PHYSICAL_INSTANCE_FRAGMENT = """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                <PhysicalInstance isUniversallyUnique="true" versionDate="2026-04-05T17:25:21.011124+01:00"
                    xmlns="ddi:physicalinstance:3_3">
                    <r:URN>urn:ddi:fr.insee:c05c0443-fc56-4069-9bea-a9c7300ae0a0:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>c05c0443-fc56-4069-9bea-a9c7300ae0a0</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">Une PhysicalInstance</r:String>
                        </r:Title>
                    </r:Citation>
                </PhysicalInstance>
            </Fragment>
            """;

    @Test
    void convert_dispatchesToMatchingConverter() {
        DDIItemConverter groupConverter = mock(DDIItemConverter.class);
        when(groupConverter.supports("Group")).thenReturn(true);
        JsonNode expected = mock(JsonNode.class);
        when(groupConverter.convert(GROUP_FRAGMENT)).thenReturn(expected);

        var service = new DDIItemConvertServiceImpl(
                List.of(groupConverter), mock(DDI3toDDI4ConverterService.class), ITEM_TYPES, OBJECT_MAPPER);
        JsonNode result = service.convert(GROUP_FRAGMENT);

        assertSame(expected, result);
        verify(groupConverter).convert(GROUP_FRAGMENT);
    }

    @Test
    void convert_fallsBackToSchemaConverter_whenNoDedicatedConverterSupportsType() {
        DDIItemConverter unsupported = mock(DDIItemConverter.class);
        when(unsupported.supports(anyString())).thenReturn(false);

        DDI3toDDI4ConverterService schemaConverter = mock(DDI3toDDI4ConverterService.class);
        Ddi4CodeList codeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE, null, "urn:ddi:fr.insee:cl-1:1",
                "fr.insee", "cl-1", "1", null, List.of());
        Ddi4Response ddi4 = new Ddi4Response(
                "ddi:4.0", null, null, null, null, List.of(codeList), List.<Ddi4Category>of());
        when(schemaConverter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0"))).thenReturn(ddi4);

        var service = new DDIItemConvertServiceImpl(List.of(unsupported), schemaConverter, ITEM_TYPES, OBJECT_MAPPER);
        JsonNode result = service.convert(CODELIST_FRAGMENT);

        // The fragment is forwarded with the Colectica item type matching its local name.
        ArgumentCaptor<Ddi3Response> captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(schemaConverter).convertDdi3ToDdi4(captor.capture(), eq("ddi:4.0"));
        Ddi3Response forwarded = captor.getValue();
        assertEquals(1, forwarded.items().size());
        assertEquals(ITEM_TYPES.get("CodeList"), forwarded.items().get(0).itemType());
        assertEquals(CODELIST_FRAGMENT, forwarded.items().get(0).item());

        // The resulting DDI4 response is serialized as JSON.
        assertNotNull(result);
        assertTrue(result.has("CodeList"));
        assertEquals("cl-1", result.get("CodeList").get(0).get("ID").asText());
    }

    @Test
    void convert_physicalInstance_producesDdi4ResponseEnvelope() {
        // With the production dedicated converters (Group, StudyUnit), a PhysicalInstance
        // fragment must go through the schema fallback and come out as a Ddi4Response
        // envelope ($schema + PhysicalInstance[]), the same DDI4 shape as GET /physical-instance,
        // not a flat single object.
        var schemaConverter = new DDI3toDDI4ConverterServiceImpl(FULL_ITEM_TYPES);
        var service = new DDIItemConvertServiceImpl(
                List.of(new GroupDDIItemConverter(), new StudyUnitDDIItemConverter()),
                schemaConverter, FULL_ITEM_TYPES, OBJECT_MAPPER);

        JsonNode result = service.convert(PHYSICAL_INSTANCE_FRAGMENT);

        assertEquals("ddi:4.0", result.get("$schema").asText());
        JsonNode physicalInstances = result.get("PhysicalInstance");
        assertNotNull(physicalInstances, "expected a PhysicalInstance envelope array");
        assertEquals(1, physicalInstances.size());
        assertEquals("PhysicalInstance", physicalInstances.get(0).get("$type").asText());
        assertEquals("c05c0443-fc56-4069-9bea-a9c7300ae0a0", physicalInstances.get(0).get("ID").asText());
    }

    @Test
    void convert_codeList_producesDdi4ResponseEnvelope() {
        // A CodeList has no dedicated converter: it must go through the schema fallback
        // and come out as a Ddi4Response envelope ($schema + CodeList[]), not a flat object.
        String codeListFragment = """
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>CL_AGEMEN8</r:ID>
                        <r:Version>1</r:Version>
                    </CodeList>
                </Fragment>
                """;
        var schemaConverter = new DDI3toDDI4ConverterServiceImpl(FULL_ITEM_TYPES);
        var service = new DDIItemConvertServiceImpl(
                List.of(new GroupDDIItemConverter(), new StudyUnitDDIItemConverter()),
                schemaConverter, FULL_ITEM_TYPES, OBJECT_MAPPER);

        JsonNode result = service.convert(codeListFragment);

        assertEquals("ddi:4.0", result.get("$schema").asText());
        JsonNode codeLists = result.get("CodeList");
        assertNotNull(codeLists, "expected a CodeList envelope array");
        assertEquals(1, codeLists.size());
        assertEquals("CL_AGEMEN8", codeLists.get(0).get("ID").asText());
    }

    @Test
    void convert_throwsWhenTypeIsNeitherSupportedNorKnown() {
        DDIItemConverter converter = mock(DDIItemConverter.class);
        when(converter.supports(anyString())).thenReturn(false);

        var service = new DDIItemConvertServiceImpl(
                List.of(converter), mock(DDI3toDDI4ConverterService.class), Map.of(), OBJECT_MAPPER);
        assertThrows(IllegalArgumentException.class, () -> service.convert(GROUP_FRAGMENT));
    }
}
