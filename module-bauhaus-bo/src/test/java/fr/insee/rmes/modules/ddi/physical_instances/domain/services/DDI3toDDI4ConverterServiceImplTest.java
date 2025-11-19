package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DDI3toDDI4ConverterServiceImplTest {

    private static final String SCHEMA_URL = "http://localhost:8080/ddi/schema";
    private DDI3toDDI4ConverterServiceImpl converter;

    @BeforeEach
    void setUp() {
        converter = new DDI3toDDI4ConverterServiceImpl();
    }

    @Test
    void shouldConvertCompleteDdi3ResponseToDdi4() {
        // Given - DDI3 Response with all types
        Ddi3Response ddi3 = createCompleteDdi3Response();
        String schemaUrl = "http://localhost:8080/ddi/schema";

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, schemaUrl);

        // Then
        assertNotNull(result);
        assertEquals(schemaUrl, result.schema());

        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());

        assertNotNull(result.dataRelationship());
        assertEquals(1, result.dataRelationship().size());

        assertNotNull(result.variable());
        assertEquals(2, result.variable().size());

        assertNotNull(result.codeList());
        assertEquals(1, result.codeList().size());

        assertNotNull(result.category());
        assertEquals(2, result.category().size());
    }

    @Test
    void shouldConvertPhysicalInstanceFromDdi3() {
        // Given
        String physicalInstanceXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>saphir-rp99-sas</r:ID>
                        <r:Version>1</r:Version>
                        <r:Citation>
                            <r:Title>
                                <r:String xml:lang="fr-FR">SAPHIR - Fichier Individus RP99 (.sas7bdat)</r:String>
                            </r:Title>
                        </r:Citation>
                        <DataRelationshipReference>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>saphir-rp99-sas</r:ID>
                            <r:Version>1</r:Version>
                            <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                        </DataRelationshipReference>
                    </PhysicalInstance>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "a51e85bb-6259-4488-8df2-f08cb43485f8",
                        "fr.insee",
                        "1",
                        "saphir-rp99-sas",
                        physicalInstanceXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());

        Ddi4PhysicalInstance pi = result.physicalInstance().get(0);
        assertEquals("true", pi.isUniversallyUnique());
        assertEquals("2025-01-21T13:48:46.363", pi.versionDate());
        assertEquals("urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1", pi.urn());
        assertEquals("fr.insee", pi.agency());
        assertEquals("saphir-rp99-sas", pi.id());
        assertEquals("1", pi.version());

        assertNotNull(pi.citation());
        assertNotNull(pi.citation().title());
        assertEquals("fr-FR", pi.citation().title().string().xmlLang());
        assertEquals("SAPHIR - Fichier Individus RP99 (.sas7bdat)", pi.citation().title().string().text());

        assertNotNull(pi.dataRelationshipReference());
        assertEquals("fr.insee", pi.dataRelationshipReference().agency());
        assertEquals("saphir-rp99-sas", pi.dataRelationshipReference().id());
        assertEquals("1", pi.dataRelationshipReference().version());
        assertEquals("DataRelationship", pi.dataRelationshipReference().typeOfObject());
    }

    @Test
    void shouldConvertDataRelationshipFromDdi3() {
        // Given
        String dataRelationshipXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>saphir-rp99-sas</r:ID>
                        <r:Version>1</r:Version>
                        <DataRelationshipName>
                            <r:String xml:lang="fr-FR">SAPHIR - RP99</r:String>
                        </DataRelationshipName>
                        <LogicalRecord isUniversallyUnique="true">
                            <r:URN>urn:ddi:fr.insee:LogicalRecord.saphir-rp99-sas:1</r:URN>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>saphir-rp99-sas</r:ID>
                            <r:Version>1</r:Version>
                            <LogicalRecordName>
                                <r:String xml:lang="fr-FR">SAPHIR - RP99</r:String>
                            </LogicalRecordName>
                            <VariablesInRecord>
                                <VariableUsedReference>
                                    <r:Agency>fr.insee</r:Agency>
                                    <r:ID>AGEMEN8</r:ID>
                                    <r:Version>1</r:Version>
                                    <r:TypeOfObject>Variable</r:TypeOfObject>
                                </VariableUsedReference>
                            </VariablesInRecord>
                        </LogicalRecord>
                    </DataRelationship>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "f39ff278-8500-45fe-a850-3906da2d242b",
                        "fr.insee",
                        "1",
                        "saphir-rp99-sas",
                        dataRelationshipXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.dataRelationship());
        assertEquals(1, result.dataRelationship().size());

        Ddi4DataRelationship dr = result.dataRelationship().get(0);
        assertEquals("true", dr.isUniversallyUnique());
        assertEquals("2025-01-21T13:48:46.363", dr.versionDate());
        assertEquals("urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1", dr.urn());
        assertEquals("fr.insee", dr.agency());
        assertEquals("saphir-rp99-sas", dr.id());
        assertEquals("1", dr.version());

        assertNotNull(dr.dataRelationshipName());
        assertEquals("SAPHIR - RP99", dr.dataRelationshipName().string().text());

        assertNotNull(dr.logicalRecord());
        assertEquals("true", dr.logicalRecord().isUniversallyUnique());
        assertNotNull(dr.logicalRecord().variablesInRecord());
        assertEquals(1, dr.logicalRecord().variablesInRecord().variableUsedReference().size());
        assertEquals("AGEMEN8", dr.logicalRecord().variablesInRecord().variableUsedReference().get(0).id());
    }

    @Test
    void shouldConvertVariableWithCodeRepresentationFromDdi3() {
        // Given
        String variableXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:Variable.AGEMEN8:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>AGEMEN8</r:ID>
                        <r:Version>1</r:Version>
                        <VariableName>
                            <r:String xml:lang="fr-FR">AGEMEN8</r:String>
                        </VariableName>
                        <r:Label>
                            <r:Content xml:lang="fr-FR">Âge détaillé</r:Content>
                        </r:Label>
                        <r:Description>
                            <r:Content xml:lang="fr-FR">Âge de l'individu en années révolues</r:Content>
                        </r:Description>
                        <VariableRepresentation>
                            <VariableRole>Demographic</VariableRole>
                            <r:CodeRepresentation blankIsMissingValue="false">
                                <r:CodeListReference>
                                    <r:Agency>fr.insee</r:Agency>
                                    <r:ID>CL_AGEMEN8</r:ID>
                                    <r:Version>1</r:Version>
                                    <r:TypeOfObject>CodeList</r:TypeOfObject>
                                </r:CodeListReference>
                            </r:CodeRepresentation>
                        </VariableRepresentation>
                    </Variable>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                        "fr.insee",
                        "1",
                        "AGEMEN8",
                        variableXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.variable());
        assertEquals(1, result.variable().size());

        Ddi4Variable var = result.variable().get(0);
        assertEquals("true", var.isUniversallyUnique());
        assertEquals("2025-01-21T13:48:46.363", var.versionDate());
        assertEquals("urn:ddi:fr.insee:Variable.AGEMEN8:1", var.urn());
        assertEquals("fr.insee", var.agency());
        assertEquals("AGEMEN8", var.id());
        assertEquals("1", var.version());

        assertNotNull(var.variableName());
        assertEquals("AGEMEN8", var.variableName().string().text());

        assertNotNull(var.label());
        assertEquals("Âge détaillé", var.label().content().text());

        assertNotNull(var.description());
        assertEquals("Âge de l'individu en années révolues", var.description().content().text());

        assertNotNull(var.variableRepresentation());
        assertEquals("Demographic", var.variableRepresentation().variableRole());
        assertNotNull(var.variableRepresentation().codeRepresentation());
        assertEquals("false", var.variableRepresentation().codeRepresentation().blankIsMissingValue());
        assertEquals("CL_AGEMEN8", var.variableRepresentation().codeRepresentation().codeListReference().id());
    }

    @Test
    void shouldConvertVariableWithNumericRepresentationFromDdi3() {
        // Given
        String variableXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:Variable.AGE:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>AGE</r:ID>
                        <r:Version>1</r:Version>
                        <VariableName>
                            <r:String xml:lang="fr-FR">AGE</r:String>
                        </VariableName>
                        <r:Label>
                            <r:Content xml:lang="fr-FR">Âge</r:Content>
                        </r:Label>
                        <VariableRepresentation>
                            <r:NumericRepresentation blankIsMissingValue="false">
                                <r:NumberRange>
                                    <r:Low isInclusive="true">0</r:Low>
                                    <r:High isInclusive="true">120</r:High>
                                </r:NumberRange>
                                <r:NumericTypeCode>Integer</r:NumericTypeCode>
                            </r:NumericRepresentation>
                        </VariableRepresentation>
                    </Variable>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                        "fr.insee",
                        "1",
                        "AGE",
                        variableXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.variable());
        assertEquals(1, result.variable().size());

        Ddi4Variable var = result.variable().get(0);
        assertNotNull(var.variableRepresentation());
        assertNotNull(var.variableRepresentation().numericRepresentation());

        NumericRepresentation numRep = var.variableRepresentation().numericRepresentation();
        assertEquals("Integer", numRep.numericTypeCode());
        assertNotNull(numRep.numberRange());
        assertEquals("true", numRep.numberRange().low().isInclusive());
        assertEquals("0", numRep.numberRange().low().text());
        assertEquals("true", numRep.numberRange().high().isInclusive());
        assertEquals("120", numRep.numberRange().high().text());
    }

    @Test
    void shouldConvertCodeListFromDdi3() {
        // Given
        String codeListXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                        <r:URN>urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>CL_AGEMEN8</r:ID>
                        <r:Version>1</r:Version>
                        <r:Label>
                            <r:Content xml:lang="fr-FR">Liste de codes - Âge détaillé</r:Content>
                        </r:Label>
                        <Code isUniversallyUnique="true">
                            <r:URN>urn:ddi:fr.insee:Code.CL_AGEMEN8.0:1</r:URN>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>0</r:ID>
                            <r:Version>1</r:Version>
                            <r:CategoryReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>CAT_0</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>Category</r:TypeOfObject>
                            </r:CategoryReference>
                            <r:Value>0</r:Value>
                        </Code>
                        <Code isUniversallyUnique="true">
                            <r:URN>urn:ddi:fr.insee:Code.CL_AGEMEN8.1:1</r:URN>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>1</r:ID>
                            <r:Version>1</r:Version>
                            <r:CategoryReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>CAT_1</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>Category</r:TypeOfObject>
                            </r:CategoryReference>
                            <r:Value>1</r:Value>
                        </Code>
                    </CodeList>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                        "fr.insee",
                        "1",
                        "CL_AGEMEN8",
                        codeListXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.codeList());
        assertEquals(1, result.codeList().size());

        Ddi4CodeList cl = result.codeList().get(0);
        assertEquals("true", cl.isUniversallyUnique());
        assertEquals("urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1", cl.urn());
        assertEquals("fr.insee", cl.agency());
        assertEquals("CL_AGEMEN8", cl.id());
        assertEquals("1", cl.version());

        assertNotNull(cl.label());
        assertEquals("Liste de codes - Âge détaillé", cl.label().content().text());

        assertNotNull(cl.code());
        assertEquals(2, cl.code().size());

        Code code0 = cl.code().get(0);
        assertEquals("0", code0.id());
        assertEquals("0", code0.value());
        assertEquals("CAT_0", code0.categoryReference().id());

        Code code1 = cl.code().get(1);
        assertEquals("1", code1.id());
        assertEquals("1", code1.value());
        assertEquals("CAT_1", code1.categoryReference().id());
    }

    @Test
    void shouldConvertCategoryFromDdi3() {
        // Given
        String categoryXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                    <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363" isMissing="false">
                        <r:URN>urn:ddi:fr.insee:Category.CAT_0:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>CAT_0</r:ID>
                        <r:Version>1</r:Version>
                        <r:Label>
                            <r:Content xml:lang="fr-FR">0 an</r:Content>
                        </r:Label>
                    </Category>
                </Fragment>
                """;

        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
                        "fr.insee",
                        "1",
                        "CAT_0",
                        categoryXml,
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.category());
        assertEquals(1, result.category().size());

        Ddi4Category cat = result.category().get(0);
        assertEquals("true", cat.isUniversallyUnique());
        assertEquals("2025-01-21T13:48:46.363", cat.versionDate());
        assertEquals("urn:ddi:fr.insee:Category.CAT_0:1", cat.urn());
        assertEquals("fr.insee", cat.agency());
        assertEquals("CAT_0", cat.id());
        assertEquals("1", cat.version());

        assertNotNull(cat.label());
        assertEquals("0 an", cat.label().content().text());
    }

    @Test
    void shouldHandleEmptyDdi3Response() {
        // Given
        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of()
        );

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertEquals(SCHEMA_URL, result.schema());
        assertNull(result.topLevelReference());
        assertNull(result.physicalInstance());
        assertNull(result.dataRelationship());
        assertNull(result.variable());
        assertNull(result.codeList());
        assertNull(result.category());
    }

    @Test
    void shouldThrowExceptionForMalformedXml() {
        // Given
        Ddi3Response ddi3 = new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(new Ddi3Response.Ddi3Item(
                        "a51e85bb-6259-4488-8df2-f08cb43485f8",
                        "fr.insee",
                        "1",
                        "test",
                        "<invalid>xml<not-closed>",
                        "2025-01-21T13:48:46.363",
                        "abcde",
                        false,
                        false,
                        false,
                        "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                ))
        );

        // When & Then
        assertThrows(RuntimeException.class, () -> converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL));
    }

    private Ddi3Response createCompleteDdi3Response() {
        return new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(
                        // PhysicalInstance
                        new Ddi3Response.Ddi3Item(
                                "a51e85bb-6259-4488-8df2-f08cb43485f8",
                                "fr.insee",
                                "1",
                                "test-pi",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                                                <r:URN>urn:ddi:fr.insee:PhysicalInstance.test-pi:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>test-pi</r:ID>
                                                <r:Version>1</r:Version>
                                                <r:Citation>
                                                    <r:Title>
                                                        <r:String xml:lang="fr-FR">Test Instance</r:String>
                                                    </r:Title>
                                                </r:Citation>
                                            </PhysicalInstance>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // DataRelationship
                        new Ddi3Response.Ddi3Item(
                                "f39ff278-8500-45fe-a850-3906da2d242b",
                                "fr.insee",
                                "1",
                                "test-dr",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                                                <r:URN>urn:ddi:fr.insee:DataRelationship.test-dr:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>test-dr</r:ID>
                                                <r:Version>1</r:Version>
                                            </DataRelationship>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // Variable 1
                        new Ddi3Response.Ddi3Item(
                                "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                                "fr.insee",
                                "1",
                                "VAR1",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                                                <r:URN>urn:ddi:fr.insee:Variable.VAR1:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>VAR1</r:ID>
                                                <r:Version>1</r:Version>
                                                <VariableName>
                                                    <r:String xml:lang="fr-FR">VAR1</r:String>
                                                </VariableName>
                                            </Variable>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // Variable 2
                        new Ddi3Response.Ddi3Item(
                                "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                                "fr.insee",
                                "1",
                                "VAR2",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                                                <r:URN>urn:ddi:fr.insee:Variable.VAR2:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>VAR2</r:ID>
                                                <r:Version>1</r:Version>
                                                <VariableName>
                                                    <r:String xml:lang="fr-FR">VAR2</r:String>
                                                </VariableName>
                                            </Variable>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // CodeList
                        new Ddi3Response.Ddi3Item(
                                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                                "fr.insee",
                                "1",
                                "CL_TEST",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                                                <r:URN>urn:ddi:fr.insee:CodeList.CL_TEST:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>CL_TEST</r:ID>
                                                <r:Version>1</r:Version>
                                            </CodeList>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // Category 1
                        new Ddi3Response.Ddi3Item(
                                "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
                                "fr.insee",
                                "1",
                                "CAT_0",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363" isMissing="false">
                                                <r:URN>urn:ddi:fr.insee:Category.CAT_0:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>CAT_0</r:ID>
                                                <r:Version>1</r:Version>
                                                <r:Label>
                                                    <r:Content xml:lang="fr-FR">Category 0</r:Content>
                                                </r:Label>
                                            </Category>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        ),
                        // Category 2
                        new Ddi3Response.Ddi3Item(
                                "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
                                "fr.insee",
                                "1",
                                "CAT_1",
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                                            <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363" isMissing="false">
                                                <r:URN>urn:ddi:fr.insee:Category.CAT_1:1</r:URN>
                                                <r:Agency>fr.insee</r:Agency>
                                                <r:ID>CAT_1</r:ID>
                                                <r:Version>1</r:Version>
                                                <r:Label>
                                                    <r:Content xml:lang="fr-FR">Category 1</r:Content>
                                                </r:Label>
                                            </Category>
                                        </Fragment>
                                        """,
                                "2025-01-21T13:48:46.363",
                                "abcde",
                                false,
                                false,
                                false,
                                "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
                        )
                )
        );
    }
}