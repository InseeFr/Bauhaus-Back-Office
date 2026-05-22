package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Category;
import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;
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
        PhysicalInstance pi = physicalInstance(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "SAPHIR")),
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
        DataRelationship dr = dataRelationship(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                LangStrings.of("fr-FR", "SAPHIR - RP99"),
                new LogicalRecord("true", "urn:...", "fr.insee", "saphir-rp99-sas", "1",
                        LangStrings.of("fr-FR", "SAPHIR - RP99"),
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
        Variable var = variable(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "fr.insee", "AGEMEN8", "1",
                null,
                LangStrings.of("fr-FR", "AGEMEN8"),
                LangStrings.of("fr-FR", "Âge détaillé"),
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
        CodeList cl = codeList(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1",
                "fr.insee", "CL_AGEMEN8", "1",
                LangStrings.of("fr-FR", "Liste codes"),
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
        Category cat = category(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Category.CAT_0:1",
                "fr.insee", "CAT_0", "1",
                LangStrings.of("fr-FR", "0 an")
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
        PhysicalInstance pi = physicalInstance(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.test:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Test")),
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
        PhysicalInstance pi = physicalInstance(
                "true", "2025-01-21T13:48:46.363", "urn:...",
                "fr.insee", "pi-id", "1",
                null, new Citation(LangStrings.of("fr-FR", "Test")),
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
        Variable var = variable(
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
                List.of(physicalInstance(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:PhysicalInstance.test:1",
                        "fr.insee", "test", "1",
                        null,
                        new Citation(LangStrings.of("fr-FR", "Test Instance")),
                        new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
                )),
                List.of(dataRelationship(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:DataRelationship.test:1",
                        "fr.insee", "test", "1",
                        null, null, null
                )),
                List.of(
                        variable("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR1:1",
                                "fr.insee", "VAR1", "1",
                                null,
                                LangStrings.of("fr-FR", "VAR1"),
                                LangStrings.of("fr-FR", "Variable 1"),
                                null, null, null),
                        variable("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR2:1",
                                "fr.insee", "VAR2", "1",
                                null,
                                LangStrings.of("fr-FR", "VAR2"),
                                LangStrings.of("fr-FR", "Variable 2"),
                                null, null, null)
                ),
                List.of(codeList(
                        "true", "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:CodeList.CL_TEST:1",
                        "fr.insee", "CL_TEST", "1",
                        LangStrings.of("fr-FR", "Test CodeList"),
                        List.of(new Code("true", "urn:...", "fr.insee", "0", "1",
                                new CategoryReference("fr.insee", "CAT_0", "1", "Category"), "0"))
                )),
                List.of(
                        category("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_0:1",
                                "fr.insee", "CAT_0", "1",
                                LangStrings.of("fr-FR", "Category 0")),
                        category("true", "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_1:1",
                                "fr.insee", "CAT_1", "1",
                                LangStrings.of("fr-FR", "Category 1"))
                )
        );
    }

    /** Construit une Category generee equivalente a l'ancien record Ddi4Category (memes arguments). */
    private static Category category(String isUniversallyUnique, String versionDate, String urn,
                                     String agency, String id, String version, List<LangString> label) {
        Category category = new Category();
        category.setURN(urn);
        category.setAgency(agency);
        category.setID(id);
        category.setVersion(version);
        category.setLabel(label);
        category.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        category.putAdditionalProperty("@versionDate", versionDate);
        return category;
    }

    /** Construit une CodeList generee equivalente a l'ancien record Ddi4CodeList (memes arguments). */
    private static CodeList codeList(String isUniversallyUnique, String versionDate, String urn,
                                     String agency, String id, String version, List<LangString> label,
                                     List<Code> codes) {
        CodeList codeList = new CodeList();
        codeList.setURN(urn);
        codeList.setAgency(agency);
        codeList.setID(id);
        codeList.setVersion(version);
        codeList.setLabel(label);
        codeList.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        codeList.putAdditionalProperty("@versionDate", versionDate);
        codeList.putAdditionalProperty("Code", codes);
        return codeList;
    }
    /** Construit une Variable generee equivalente a l'ancien record Ddi4Variable (memes arguments). */
    private static Variable variable(String isUniversallyUnique, String versionDate, String urn, String agency,
                                     String id, String version, BasedOnObject basedOnObject,
                                     List<LangString> variableName, List<LangString> label,
                                     List<LangString> description, VariableRepresentation representation,
                                     String isGeographic) {
        Variable variable = new Variable();
        variable.setURN(urn);
        variable.setAgency(agency);
        variable.setID(id);
        variable.setVersion(version);
        variable.setVariableName(variableName);
        variable.setLabel(label);
        variable.setDescription(description);
        variable.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        variable.putAdditionalProperty("@versionDate", versionDate);
        variable.putAdditionalProperty("@isGeographic", isGeographic);
        if (basedOnObject != null) {
            variable.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (representation != null) {
            variable.putAdditionalProperty("VariableRepresentation", representation);
        }
        return variable;
    }
    /** Construit une DataRelationship generee equivalente a l'ancien record (memes arguments). */
    private static DataRelationship dataRelationship(String isUniversallyUnique, String versionDate, String urn,
                                                     String agency, String id, String version,
                                                     BasedOnObject basedOnObject, List<LangString> label,
                                                     LogicalRecord logicalRecord) {
        DataRelationship dataRelationship = new DataRelationship();
        dataRelationship.setURN(urn);
        dataRelationship.setAgency(agency);
        dataRelationship.setID(id);
        dataRelationship.setVersion(version);
        dataRelationship.setLabel(label);
        dataRelationship.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        dataRelationship.putAdditionalProperty("@versionDate", versionDate);
        if (basedOnObject != null) {
            dataRelationship.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (logicalRecord != null) {
            dataRelationship.putAdditionalProperty("LogicalRecord", logicalRecord);
        }
        return dataRelationship;
    }
    /** Construit une PhysicalInstance generee equivalente a l'ancien record (memes arguments). */
    private static PhysicalInstance physicalInstance(String isUniversallyUnique, String versionDate, String urn,
                                                     String agency, String id, String version,
                                                     BasedOnObject basedOnObject, Citation citation,
                                                     DataRelationshipReference dataRelationshipReference) {
        PhysicalInstance physicalInstance = new PhysicalInstance();
        physicalInstance.setURN(urn);
        physicalInstance.setAgency(agency);
        physicalInstance.setID(id);
        physicalInstance.setVersion(version);
        physicalInstance.putAdditionalProperty("@isUniversallyUnique", isUniversallyUnique);
        physicalInstance.putAdditionalProperty("@versionDate", versionDate);
        if (basedOnObject != null) {
            physicalInstance.putAdditionalProperty("BasedOnObject", basedOnObject);
        }
        if (citation != null) {
            physicalInstance.putAdditionalProperty("Citation", citation);
        }
        if (dataRelationshipReference != null) {
            physicalInstance.putAdditionalProperty("DataRelationshipReference", dataRelationshipReference);
        }
        return physicalInstance;
    }
}
