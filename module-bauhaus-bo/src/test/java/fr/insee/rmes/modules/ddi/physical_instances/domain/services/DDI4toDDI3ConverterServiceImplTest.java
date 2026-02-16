package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DDI4toDDI3ConverterServiceImplTest {

    private DDI4toDDI3ConverterServiceImpl converter;

    @BeforeEach
    void setUp() {
        converter = new DDI4toDDI3ConverterServiceImpl();
    }

    @Test
    void shouldConvertCompleteDdi4ResponseToDdi3() {
        // Given - DDI4 Response with all types
        Ddi4Response ddi4 = createCompleteDdi4Response();

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertNotNull(result.items());
        assertEquals(7, result.items().size());

        // Verify each item type
        long physicalInstanceCount = result.items().stream()
                .filter(item -> "a51e85bb-6259-4488-8df2-f08cb43485f8".equals(item.itemType()))
                .count();
        assertEquals(1, physicalInstanceCount);

        long dataRelationshipCount = result.items().stream()
                .filter(item -> "f39ff278-8500-45fe-a850-3906da2d242b".equals(item.itemType()))
                .count();
        assertEquals(1, dataRelationshipCount);

        long variableCount = result.items().stream()
                .filter(item -> "683889c6-f74b-4d5e-92ed-908c0a42bb2d".equals(item.itemType()))
                .count();
        assertEquals(2, variableCount);

        long codeListCount = result.items().stream()
                .filter(item -> "8b108ef8-b642-4484-9c49-f88e4bf7cf1d".equals(item.itemType()))
                .count();
        assertEquals(1, codeListCount);

        long categoryCount = result.items().stream()
                .filter(item -> "7e47c269-bcab-40f7-a778-af7bbc4e3d00".equals(item.itemType()))
                .count();
        assertEquals(2, categoryCount);

        // Verify options
        assertNotNull(result.options());
        assertEquals(List.of("RegisterOrReplace"), result.options().namedOptions());
    }

    @Test
    void shouldConvertPhysicalInstance() {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "SAPHIR - Fichier Individus RP99 (.sas7bdat)"))),
                new DataRelationshipReference("fr.insee", "saphir-rp99-sas", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                List.of(pi),
                null,
                null,
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertNotNull(result.items());
        assertEquals(1, result.items().size());

        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", item.itemType());
        assertEquals("fr.insee", item.agencyId());
        assertEquals("1", item.version());
        assertEquals("saphir-rp99-sas", item.identifier());
        assertEquals("2025-01-21T13:48:46.363", item.versionDate());

        // Verify XML contains expected elements
        String xml = item.item();
        assertTrue(xml.contains("<PhysicalInstance"));
        assertTrue(xml.contains("isUniversallyUnique=\"true\""));
        assertTrue(xml.contains("versionDate=\"2025-01-21T13:48:46.363\""));
        assertTrue(xml.contains("<r:URN>urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1</r:URN>"));
        assertTrue(xml.contains("<r:Agency>fr.insee</r:Agency>"));
        assertTrue(xml.contains("<r:ID>saphir-rp99-sas</r:ID>"));
        assertTrue(xml.contains("<r:Version>1</r:Version>"));
        assertTrue(xml.contains("SAPHIR - Fichier Individus RP99 (.sas7bdat)"));
        assertTrue(xml.contains("<r:DataRelationshipReference"));
    }

    @Test
    void shouldConvertDataRelationship() {
        // Given
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new DataRelationshipName(new StringValue("fr-FR", "SAPHIR - RP99")),
                null,
                new LogicalRecord(
                        "true",
                        "urn:ddi:fr.insee:LogicalRecord.saphir-rp99-sas:1",
                        "fr.insee",
                        "saphir-rp99-sas",
                        "1",
                        new LogicalRecordName(new StringValue("fr-FR", "SAPHIR - RP99")),
                        null,
                        new VariablesInRecord(List.of(
                                new VariableUsedReference("fr.insee", "var1", "1", "Variable"),
                                new VariableUsedReference("fr.insee", "var2", "1", "Variable")
                        ))
                )
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                List.of(dr),
                null,
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", item.itemType());

        String xml = item.item();
        assertTrue(xml.contains("<DataRelationship"));
        assertTrue(xml.contains("SAPHIR - RP99"));
        assertTrue(xml.contains("<LogicalRecord"));
        assertTrue(xml.contains("<VariablesInRecord"));
        assertTrue(xml.contains("<VariableUsedReference"));
    }

    @Test
    void shouldConvertVariable() {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "fr.insee",
                "AGEMEN8",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "AGEMEN8")),
                new Label(new Content("fr-FR", "Âge détaillé")),
                new Description(new Content("fr-FR", "Âge de l'individu en années révolues")),
                new VariableRepresentation(
                        "Demographic",
                        new CodeRepresentation(
                                "false",
                                new CodeListReference("fr.insee", "CL_AGEMEN8", "1", "CodeList")
                        ),
                        null,
                        null,
                        null
                ),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("683889c6-f74b-4d5e-92ed-908c0a42bb2d", item.itemType());

        String xml = item.item();
        assertTrue(xml.contains("<Variable"));
        assertTrue(xml.contains("AGEMEN8"));
        assertTrue(xml.contains("Âge détaillé"));
        assertTrue(xml.contains("Âge de l'individu en années révolues"));
        assertTrue(xml.contains("<VariableRepresentation"));
        assertTrue(xml.contains("<r:CodeRepresentation"));
        assertTrue(xml.contains("Demographic"));
    }

    @Test
    void shouldConvertCodeList() {
        // Given
        Ddi4CodeList cl = new Ddi4CodeList(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1",
                "fr.insee",
                "CL_AGEMEN8",
                "1",
                new Label(new Content("fr-FR", "Liste de codes - Âge détaillé")),
                List.of(
                        new Code(
                                "true",
                                "urn:ddi:fr.insee:Code.CL_AGEMEN8.0:1",
                                "fr.insee",
                                "0",
                                "1",
                                new CategoryReference("fr.insee", "CAT_0", "1", "Category"),
                                "0"
                        ),
                        new Code(
                                "true",
                                "urn:ddi:fr.insee:Code.CL_AGEMEN8.1:1",
                                "fr.insee",
                                "1",
                                "1",
                                new CategoryReference("fr.insee", "CAT_1", "1", "Category"),
                                "1"
                        )
                )
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                null,
                List.of(cl),
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", item.itemType());

        String xml = item.item();
        assertTrue(xml.contains("<CodeList"));
        assertTrue(xml.contains("Liste de codes - Âge détaillé"));
        assertTrue(xml.contains("<Code"));
        assertTrue(xml.contains("<r:CategoryReference"));
        assertTrue(xml.contains("<r:Value>0</r:Value>"));
        assertTrue(xml.contains("<r:Value>1</r:Value>"));
    }

    @Test
    void shouldConvertCategory() {
        // Given
        Ddi4Category cat = new Ddi4Category(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Category.CAT_0:1",
                "fr.insee",
                "CAT_0",
                "1",
                new Label(new Content("fr-FR", "0 an"))
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                null,
                null,
                List.of(cat)
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertEquals("7e47c269-bcab-40f7-a778-af7bbc4e3d00", item.itemType());

        String xml = item.item();
        assertTrue(xml.contains("<Category"));
        assertTrue(xml.contains("0 an"));
        assertTrue(xml.contains("isMissing=\"false\""));
    }

    @Test
    void shouldHandleEmptyDdi4Response() {
        // Given
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                null,
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertNotNull(result.items());
        assertTrue(result.items().isEmpty());
        assertNotNull(result.options());
        assertEquals(List.of("RegisterOrReplace"), result.options().namedOptions());
    }

    @Test
    void shouldConvertNumericVariable() {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGE:1",
                "fr.insee",
                "AGE",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "AGE")),
                new Label(new Content("fr-FR", "Âge")),
                null,
                new VariableRepresentation(
                        "Demographic",
                        null,
                        new NumericRepresentation(
                                "Integer",
                                new NumberRange(
                                        new RangeValue("true", "0"),
                                        new RangeValue("true", "120")
                                )
                        ),
                        null,
                        null
                ),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        assertTrue(xml.contains("<r:NumericRepresentation"));
        assertTrue(xml.contains("<r:NumberRange"));
        assertTrue(xml.contains("<r:Low"));
        assertTrue(xml.contains("<r:High"));
        assertTrue(xml.contains("Integer"));
    }

    @Test
    void shouldConvertDateTimeVariable() {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-12-04T10:35:54.610Z",
                "urn:ddi:fr.insee:local-1764843880960:1",
                "fr.insee",
                "local-1764843880960",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "qq")),
                new Label(new Content("fr-FR", "bc")),
                null,
                new VariableRepresentation(
                        "Mesure",
                        null,
                        null,
                        new DateTimeRepresentation("Date", null),
                        null
                ),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify DateTimeRepresentation elements are present
        assertTrue(xml.contains("<r:DateTimeRepresentation"));
        assertTrue(xml.contains("<r:DateTypeCode>Date</r:DateTypeCode>"));
        assertTrue(xml.contains("</r:DateTimeRepresentation>"));
        assertTrue(xml.contains("<VariableRole>Mesure</VariableRole>"));
    }

    @Test
    void shouldConvertGeographicVariable() {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-12-04T11:15:00.000Z",
                "urn:ddi:fr.insee:local-geographic-var:1",
                "fr.insee",
                "local-geographic-var",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "GEO_VAR")),
                new Label(new Content("fr-FR", "Geographic Variable")),
                new Description(new Content("fr-FR", "A geographic variable")),
                new VariableRepresentation(
                        "Geographic",
                        new CodeRepresentation(
                                "false",
                                new CodeListReference("fr.insee", "CL_GEO", "1", "CodeList")
                        ),
                        null,
                        null,
                        null
                ),
                "true"
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify isGeographic attribute is present
        assertTrue(xml.contains("isGeographic=\"true\""));
        assertTrue(xml.contains("<VariableRole>Geographic</VariableRole>"));
        assertTrue(xml.contains("GEO_VAR"));
    }

    @Test
    void shouldConvertTextVariable() {
        // Given
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-12-04T10:35:54.610Z",
                "urn:ddi:fr.insee:local-text-var:1",
                "fr.insee",
                "local-text-var",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "TEXT_VAR")),
                new Label(new Content("fr-FR", "Text Variable")),
                null,
                new VariableRepresentation(
                        "Description",
                        null,
                        null,
                        null,
                        new TextRepresentation(100, 10, "[A-Za-z0-9]+", "false")
                ),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify TextRepresentation elements are present
        assertTrue(xml.contains("<r:TextRepresentation"));
        assertTrue(xml.contains("<r:MaxLength>100</r:MaxLength>"));
        assertTrue(xml.contains("<r:MinLength>10</r:MinLength>"));
        assertTrue(xml.contains("<r:RegExp>[A-Za-z0-9]+</r:RegExp>"));
        assertTrue(xml.contains("blankIsMissingValue=\"false\""));
        assertTrue(xml.contains("</r:TextRepresentation>"));
    }

    @Test
    void shouldConvertDdi4ToDdi3Xml() {
        // Given
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.test:1",
                "fr.insee",
                "test-id",
                "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                List.of(pi),
                null,
                null,
                null,
                null
        );

        // When
        String xml = converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        assertNotNull(xml);

        // Verify XML declaration
        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));

        // Verify FragmentInstance structure
        assertTrue(xml.contains("<ddi:FragmentInstance"));
        assertTrue(xml.contains("xmlns:r=\"ddi:reusable:3_3\""));
        assertTrue(xml.contains("xmlns:ddi=\"ddi:instance:3_3\""));

        // Verify TopLevelReference
        assertTrue(xml.contains("<ddi:TopLevelReference>"));
        assertTrue(xml.contains("<r:Agency>fr.insee</r:Agency>"));
        assertTrue(xml.contains("<r:ID>test-id</r:ID>"));
        assertTrue(xml.contains("<r:Version>1</r:Version>"));
        assertTrue(xml.contains("<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"));
        assertTrue(xml.contains("</ddi:TopLevelReference>"));

        // Verify Fragment
        assertTrue(xml.contains("<ddi:Fragment"));
        assertTrue(xml.contains("</ddi:Fragment>"));

        // Verify PhysicalInstance content
        assertTrue(xml.contains("<PhysicalInstance"));
        assertTrue(xml.contains("Test Instance"));

        // Verify closing tag
        assertTrue(xml.contains("</ddi:FragmentInstance>"));
    }

    @Test
    void shouldConvertCompleteDdi4ResponseToXml() {
        // Given
        Ddi4Response ddi4 = createCompleteDdi4Response();

        // When
        String xml = converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        assertNotNull(xml);

        // Verify XML structure
        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));
        assertTrue(xml.contains("<ddi:FragmentInstance"));
        assertTrue(xml.contains("<ddi:TopLevelReference>"));
        assertTrue(xml.contains("</ddi:FragmentInstance>"));

        // Verify multiple fragments exist (7 items: 1 PI, 1 DR, 2 Variables, 1 CodeList, 2 Categories)
        int fragmentCount = countOccurrences(xml, "<ddi:Fragment");
        assertEquals(8, fragmentCount);

        // Verify different element types
        assertTrue(xml.contains("<PhysicalInstance"));
        assertTrue(xml.contains("<DataRelationship"));
        assertTrue(xml.contains("<Variable"));
        assertTrue(xml.contains("<CodeList"));
        assertTrue(xml.contains("<Category"));
    }

    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    @Test
    void shouldConvertPhysicalInstanceWithBasedOnObject() {
        // Given - PhysicalInstance with BasedOnObject
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-pi-id",
                "1",
                "PhysicalInstance"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.copy:1",
                "fr.insee",
                "copy-id",
                "1",
                basedOnObject,
                new Citation(new Title(new StringValue("fr-FR", "Copy of Original"))),
                new DataRelationshipReference("fr.insee", "dr-id", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                List.of(pi),
                null,
                null,
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify BasedOnObject structure is present
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:Agency>fr.insee</r:Agency>"));
        assertTrue(xml.contains("<r:ID>original-pi-id</r:ID>"));
        assertTrue(xml.contains("<r:Version>1</r:Version>"));
        assertTrue(xml.contains("<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldConvertDataRelationshipWithBasedOnObject() {
        // Given - DataRelationship with BasedOnObject
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-dr-id",
                "1",
                "DataRelationship"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:DataRelationship.copy:1",
                "fr.insee",
                "copy-dr-id",
                "1",
                basedOnObject,
                new DataRelationshipName(new StringValue("fr-FR", "Copy DR")),
                null,
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                List.of(dr),
                null,
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify BasedOnObject structure is present
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:ID>original-dr-id</r:ID>"));
        assertTrue(xml.contains("<r:TypeOfObject>DataRelationship</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldConvertVariableWithBasedOnObject() {
        // Given - Variable with BasedOnObject
        BasedOnReference basedOnRef = new BasedOnReference(
                "fr.insee",
                "original-var-id",
                "1",
                "Variable"
        );
        BasedOnObject basedOnObject = new BasedOnObject(basedOnRef);

        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.copy:1",
                "fr.insee",
                "copy-var-id",
                "1",
                basedOnObject,
                new VariableName(new StringValue("fr-FR", "COPY_VAR")),
                new Label(new Content("fr-FR", "Copy Variable")),
                null,
                null,
                null
        );
        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());

        String xml = result.items().get(0).item();
        // Verify BasedOnObject structure is present
        assertTrue(xml.contains("<r:BasedOnObject>"));
        assertTrue(xml.contains("<r:BasedOnReference>"));
        assertTrue(xml.contains("<r:ID>original-var-id</r:ID>"));
        assertTrue(xml.contains("<r:TypeOfObject>Variable</r:TypeOfObject>"));
        assertTrue(xml.contains("</r:BasedOnReference>"));
        assertTrue(xml.contains("</r:BasedOnObject>"));
    }

    @Test
    void shouldUseVariableInTopLevelReferenceWhenVariableIsRoot() {
        // Given - DDI4 Response with a Variable as root object
        Ddi4Variable var = new Ddi4Variable(
                "true",
                "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "test-agency",
                "0755bbcb-3356-43fc-997d-76fdf015e1a4",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "AGEMEN8")),
                new Label(new Content("fr-FR", "Âge détaillé")),
                null,
                null,
                null
        );

        TopLevelReference topLevelRef = new TopLevelReference(
                "test-agency",
                "0755bbcb-3356-43fc-997d-76fdf015e1a4",
                "1",
                "Variable"
        );

        Ddi4Response ddi4 = new Ddi4Response(
                "file:/jsonSchema.json",
                List.of(topLevelRef),
                null,
                null,
                List.of(var),
                null,
                null
        );

        // When
        String xml = converter.convertDdi4ToDdi3Xml(ddi4);

        // Then
        assertNotNull(xml);

        // Verify TopLevelReference uses Variable's ID and Agency
        assertTrue(xml.contains("<ddi:TopLevelReference>"));
        assertTrue(xml.contains("<r:Agency>test-agency</r:Agency>"));
        assertTrue(xml.contains("<r:ID>0755bbcb-3356-43fc-997d-76fdf015e1a4</r:ID>"));
        assertTrue(xml.contains("<r:Version>1</r:Version>"));
        assertTrue(xml.contains("<r:TypeOfObject>Variable</r:TypeOfObject>"));
        assertTrue(xml.contains("</ddi:TopLevelReference>"));

        // Verify Variable content is also present
        assertTrue(xml.contains("<Variable"));
        assertTrue(xml.contains("AGEMEN8"));
    }

    private Ddi4Response createCompleteDdi4Response() {
        return new Ddi4Response(
                "file:/jsonSchema.json",
                null,
                List.of(new Ddi4PhysicalInstance(
                        "true",
                        "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:PhysicalInstance.test:1",
                        "fr.insee",
                        "test",
                        "1",
                        null,
                        new Citation(new Title(new StringValue("fr-FR", "Test Instance"))),
                        new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
                )),
                List.of(new Ddi4DataRelationship(
                        "true",
                        "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:DataRelationship.test:1",
                        "fr.insee",
                        "test",
                        "1",
                        null,
                        new DataRelationshipName(new StringValue("fr-FR", "Test")),
                        null,
                        null
                )),
                List.of(
                        new Ddi4Variable(
                                "true",
                                "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR1:1",
                                "fr.insee",
                                "VAR1",
                                "1",
                                null,
                                new VariableName(new StringValue("fr-FR", "VAR1")),
                                new Label(new Content("fr-FR", "Variable 1")),
                                null,
                                null,
                                null
                        ),
                        new Ddi4Variable(
                                "true",
                                "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Variable.VAR2:1",
                                "fr.insee",
                                "VAR2",
                                "1",
                                null,
                                new VariableName(new StringValue("fr-FR", "VAR2")),
                                new Label(new Content("fr-FR", "Variable 2")),
                                null,
                                null,
                                null
                        )
                ),
                List.of(new Ddi4CodeList(
                        "true",
                        "2025-01-21T13:48:46.363",
                        "urn:ddi:fr.insee:CodeList.CL_TEST:1",
                        "fr.insee",
                        "CL_TEST",
                        "1",
                        new Label(new Content("fr-FR", "Test CodeList")),
                        List.of(new Code(
                                "true",
                                "urn:ddi:fr.insee:Code.CL_TEST.0:1",
                                "fr.insee",
                                "0",
                                "1",
                                new CategoryReference("fr.insee", "CAT_0", "1", "Category"),
                                "0"
                        ))
                )),
                List.of(
                        new Ddi4Category(
                                "true",
                                "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_0:1",
                                "fr.insee",
                                "CAT_0",
                                "1",
                                new Label(new Content("fr-FR", "Category 0"))
                        ),
                        new Ddi4Category(
                                "true",
                                "2025-01-21T13:48:46.363",
                                "urn:ddi:fr.insee:Category.CAT_1:1",
                                "fr.insee",
                                "CAT_1",
                                "1",
                                new Label(new Content("fr-FR", "Category 1"))
                        )
                )
        );
    }
}