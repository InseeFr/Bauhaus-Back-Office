package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDI4toDDI3ConverterServiceImplTest {

    @Mock
    private Ddi3XmlWriter xmlWriter;

    private DDI4toDDI3ConverterServiceImpl converter;

    private static final Map<String, String> ITEM_TYPES = Map.of(
        "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
        "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
        "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
        "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
        "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
    );

    @BeforeEach
    void setUp() {
        converter = new DDI4toDDI3ConverterServiceImpl(ITEM_TYPES, xmlWriter);
    }

    @Test
    void shouldConvertPhysicalInstance() throws XMLStreamException {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "SAPHIR"))),
                new DataRelationshipReference("fr.insee", "saphir-rp99-sas", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, List.of(pi), null, null, null, null);
        when(xmlWriter.buildPhysicalInstanceXml(pi)).thenReturn("<PhysicalInstance/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(1, result.items().size());
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("1", item.version());
        assertEquals("saphir-rp99-sas", item.identifier());
        assertEquals("2025-01-21T13:48:46.363", item.versionDate());
        assertEquals("<PhysicalInstance/>", item.item());
        verify(xmlWriter).buildPhysicalInstanceXml(pi);
    }

    @Test
    void shouldConvertDataRelationship() throws XMLStreamException {
        // Given
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new Label(new Content("fr-FR", "SAPHIR - RP99")),
                new LogicalRecord("true", "urn:...", "fr.insee", "saphir-rp99-sas", "1",
                        new Label(new Content("fr-FR", "SAPHIR - RP99")),
                        new VariablesInRecord(List.of()))
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, List.of(dr), null, null, null);
        when(xmlWriter.buildDataRelationshipXml(dr)).thenReturn("<DataRelationship/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(1, result.items().size());
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("saphir-rp99-sas", item.identifier());
        assertEquals("<DataRelationship/>", item.item());
        verify(xmlWriter).buildDataRelationshipXml(dr);
    }

    @Test
    void shouldConvertVariable() throws XMLStreamException {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "fr.insee", "AGEMEN8", "1",
                null,
                new VariableName(new StringValue("fr-FR", "AGEMEN8")),
                new Label(new Content("fr-FR", "Âge détaillé")),
                null,
                new VariableRepresentation("Demographic",
                        new CodeRepresentation("false", new CodeListReference("fr.insee", "CL_AGEMEN8", "1", "CodeList")),
                        null, null, null),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, List.of(var), null, null);
        when(xmlWriter.buildVariableXml(var)).thenReturn("<Variable/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(1, result.items().size());
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("683889c6-f74b-4d5e-92ed-908c0a42bb2d", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("AGEMEN8", item.identifier());
        assertEquals("<Variable/>", item.item());
        verify(xmlWriter).buildVariableXml(var);
    }

    @Test
    void shouldConvertCodeList() throws XMLStreamException {
        // Given
        Ddi4CodeList cl = new Ddi4CodeList(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1",
                "fr.insee", "CL_AGEMEN8", "1",
                new Label(new Content("fr-FR", "Liste codes")),
                List.of()
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, List.of(cl), null);
        when(xmlWriter.buildCodeListXml(cl)).thenReturn("<CodeList/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(1, result.items().size());
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("CL_AGEMEN8", item.identifier());
        assertEquals("<CodeList/>", item.item());
        verify(xmlWriter).buildCodeListXml(cl);
    }

    @Test
    void shouldConvertCategory() throws XMLStreamException {
        // Given
        Ddi4Category cat = new Ddi4Category(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Category.CAT_0:1",
                "fr.insee", "CAT_0", "1",
                new Label(new Content("fr-FR", "0 an"))
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, null, List.of(cat));
        when(xmlWriter.buildCategoryXml(cat)).thenReturn("<Category/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(1, result.items().size());
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("7e47c269-bcab-40f7-a778-af7bbc4e3d00", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("CAT_0", item.identifier());
        assertEquals("<Category/>", item.item());
        verify(xmlWriter).buildCategoryXml(cat);
    }

    @Test
    void shouldHandleEmptyDdi4Response() {
        // Given
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, null, null);

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(List.of("RegisterOrReplace"), result.options().namedOptions());
        verifyNoInteractions(xmlWriter);
    }

    @Test
    void shouldConvertCompleteDdi4ResponseToDdi3() throws XMLStreamException {
        // Given
        Ddi4Response ddi4 = createCompleteDdi4Response();
        when(xmlWriter.buildPhysicalInstanceXml(any())).thenReturn("<PhysicalInstance/>");
        when(xmlWriter.buildDataRelationshipXml(any())).thenReturn("<DataRelationship/>");
        when(xmlWriter.buildVariableXml(any())).thenReturn("<Variable/>");
        when(xmlWriter.buildCodeListXml(any())).thenReturn("<CodeList/>");
        when(xmlWriter.buildCategoryXml(any())).thenReturn("<Category/>");

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertEquals(7, result.items().size());
        assertEquals(List.of("RegisterOrReplace"), result.options().namedOptions());

        verify(xmlWriter, times(1)).buildPhysicalInstanceXml(any());
        verify(xmlWriter, times(1)).buildDataRelationshipXml(any());
        verify(xmlWriter, times(2)).buildVariableXml(any());
        verify(xmlWriter, times(1)).buildCodeListXml(any());
        verify(xmlWriter, times(2)).buildCategoryXml(any());
    }

    @Test
    void shouldConvertDdi4ToDdi3Xml() throws XMLStreamException {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.test:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Test"))),
                new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
        );
        TopLevelReference topLevelRef = new TopLevelReference("fr.insee", "test-id", "1", "PhysicalInstance");
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", List.of(topLevelRef), List.of(pi), null, null, null, null);
        when(xmlWriter.buildPhysicalInstanceXml(pi)).thenReturn("<PhysicalInstance/>");
        when(xmlWriter.buildFragmentInstanceDocument(any(), eq(topLevelRef))).thenReturn("<FragmentInstance/>");

        // When
        String result = converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        assertEquals("<FragmentInstance/>", result);
        verify(xmlWriter).buildFragmentInstanceDocument(any(), eq(topLevelRef));
    }

    @Test
    void shouldUseFirstTopLevelReferenceWhenMultipleExist() throws XMLStreamException {
        // Given
        TopLevelReference firstRef = new TopLevelReference("fr.insee", "id-1", "1", "Variable");
        TopLevelReference secondRef = new TopLevelReference("fr.insee", "id-2", "1", "Variable");
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", List.of(firstRef, secondRef), null, null, null, null, null);
        when(xmlWriter.buildFragmentInstanceDocument(any(), eq(firstRef))).thenReturn("<FragmentInstance/>");

        // When
        converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        verify(xmlWriter).buildFragmentInstanceDocument(any(), eq(firstRef));
    }

    @Test
    void shouldUseNullTopLevelReferenceWhenListIsEmpty() throws XMLStreamException {
        // Given
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", List.of(), null, null, null, null, null);
        when(xmlWriter.buildFragmentInstanceDocument(any(), isNull())).thenReturn("<FragmentInstance/>");

        // When
        converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        verify(xmlWriter).buildFragmentInstanceDocument(any(), isNull());
    }

    @Test
    void shouldWrapXMLStreamExceptionAsRuntimeExceptionForPhysicalInstance() throws XMLStreamException {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true", "2025-01-21T13:48:46.363", "urn:...",
                "fr.insee", "pi-id", "1",
                null, new Citation(new Title(new StringValue("fr-FR", "Test"))),
                new DataRelationshipReference("fr.insee", "dr", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, List.of(pi), null, null, null, null);
        when(xmlWriter.buildPhysicalInstanceXml(pi)).thenThrow(new XMLStreamException("XML error"));

        // When / Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> converter.convertDdi4ToDdi3(ddi4));
        assertTrue(ex.getCause() instanceof XMLStreamException);
    }

    @Test
    void shouldWrapXMLStreamExceptionAsRuntimeExceptionForVariable() throws XMLStreamException {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true", "2025-01-21T13:48:46.363", "urn:...",
                "fr.insee", "var-id", "1",
                null, null, null, null, null, null
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, List.of(var), null, null);
        when(xmlWriter.buildVariableXml(var)).thenThrow(new XMLStreamException("XML error"));

        // When / Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> converter.convertDdi4ToDdi3(ddi4));
        assertTrue(ex.getCause() instanceof XMLStreamException);
    }

    private Ddi4Response createCompleteDdi4Response() {
        return new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                List.of(new Ddi4PhysicalInstance(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:PhysicalInstance.test:1",
                        "fr.insee", "test", "1",
                        null,
                        new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                        new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
                )),
                List.of(new Ddi4DataRelationship(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:DataRelationship.test:1",
                        "fr.insee", "test", "1",
                        null, null, null
                )),
                List.of(
                        new Ddi4Variable("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR1:1",
                                "fr.insee", "VAR1", "1",
                                null,
                                new VariableName(new StringValue("fr-FR", "VAR1")),
                                new Label(new Content("fr-FR", "Variable 1")),
                                null, null, null),
                        new Ddi4Variable("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR2:1",
                                "fr.insee", "VAR2", "1",
                                null,
                                new VariableName(new StringValue("fr-FR", "VAR2")),
                                new Label(new Content("fr-FR", "Variable 2")),
                                null, null, null)
                ),
                List.of(new Ddi4CodeList(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:CodeList.CL_TEST:1",
                        "fr.insee", "CL_TEST", "1",
                        new Label(new Content("fr-FR", "Test CodeList")),
                        List.of(new Code("true", "urn:...", "fr.insee", "0", "1",
                                new CategoryReference("fr.insee", "CAT_0", "1", "Category"), "0"))
                )),
                List.of(
                        new Ddi4Category("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_0:1",
                                "fr.insee", "CAT_0", "1",
                                new Label(new Content("fr-FR", "Category 0"))),
                        new Ddi4Category("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_1:1",
                                "fr.insee", "CAT_1", "1",
                                new Label(new Content("fr-FR", "Category 1")))
                )
        );
    }
}
