package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.GROUP_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.LOGICAL_PRODUCT_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.PHYSICAL_INSTANCE_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.STUDY_UNIT_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.fragment;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.fragmentOptions;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.inNamespace;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.logicalProductReferencingEverySchemeKind;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.reference;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.sentinelValuesMmvrFragment;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.versionable;
import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.XmlOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DDI3toDDI4ConverterServiceImplTest {

    private static final String SCHEMA_URL = "http://localhost:8080/ddi/schema";
    private static final String VERSION_DATE = "2025-01-21T13:48:46.363";

    private static final String PHYSICAL_INSTANCE_TYPE = "a51e85bb-6259-4488-8df2-f08cb43485f8";
    private static final String DATA_RELATIONSHIP_TYPE = "f39ff278-8500-45fe-a850-3906da2d242b";
    private static final String VARIABLE_TYPE = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
    private static final String CODE_LIST_TYPE = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
    private static final String CATEGORY_TYPE = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";
    private static final String MANAGED_MISSING_VALUES_REPRESENTATION_TYPE = "c29c3125-2a53-4179-8fa6-aa3beb2bb5ed";

    private DDI3toDDI4ConverterServiceImpl converter;

    @BeforeEach
    void setUp() {
        converter = new DDI3toDDI4ConverterServiceImpl(Map.of(
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE,
                "Variable", VARIABLE_TYPE,
                "CodeList", CODE_LIST_TYPE,
                "Category", CATEGORY_TYPE,
                "ManagedMissingValuesRepresentation", MANAGED_MISSING_VALUES_REPRESENTATION_TYPE));
    }

    /**
     * Valeurs sentinelles (#1566) : les items {@code ManagedMissingValuesRepresentation} du set
     * DDI 3 sont agrégés dans la {@code Ddi4Response}.
     */
    @Test
    void shouldConvertManagedMissingValuesRepresentationItems() {
        Ddi3Response ddi3 = ddi3Response(ddi3Item(
                MANAGED_MISSING_VALUES_REPRESENTATION_TYPE,
                "mmvr-1",
                sentinelValuesMmvrFragment(),
                "2026-04-03T12:00:00Z"));

        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        assertNotNull(result.managedMissingValuesRepresentation());
        assertEquals(1, result.managedMissingValuesRepresentation().size());
        Ddi4ManagedMissingValuesRepresentation mmvr =
                result.managedMissingValuesRepresentation().get(0);
        assertEquals("mmvr-1", mmvr.id());
        assertEquals(
                "cl-sentinelles",
                mmvr.missingCodeRepresentation().get(0).codeListReference().id());
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
    void shouldParseCodeListSchemeFromFragmentXml() {
        String codeListSchemeXml = fragment(versionable(
                "CodeListScheme",
                inNamespace(LOGICAL_PRODUCT_NS, "2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:cls-id:1",
                "cls-id",
                "<r:Label><r:Content xml:lang=\"fr-FR\">Schéma listes</r:Content></r:Label>"
                        + reference("r:CodeListReference", "cl-1", "CodeList")));

        Ddi4CodeListScheme scheme = converter.toCodeListScheme(codeListSchemeXml);

        assertEquals("cls-id", scheme.id());
        assertEquals("fr.insee", scheme.agency());
        assertEquals("1", scheme.version());
        assertEquals(1, scheme.codeListReference().size());
        assertEquals("cl-1", scheme.codeListReference().get(0).id());
    }

    @Test
    void shouldParseGroupFromFragmentXml() {
        // Build a schema-valid Group fragment with the real serializer, then parse it back:
        // this is the exact round-trip the auto-provision of a group's CodeListScheme relies on.
        Ddi4Group original = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1",
                "fr.insee",
                "group-id",
                "1",
                "resp",
                new Citation(LangStrings.of("fr-FR", "Enquête innovation")),
                List.of(Reference.of("fr.insee", "su-1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries");
        String groupXml = new Ddi4ToLifecycle33("abcde").toGroup(original).xmlText(fragmentOptions(GROUP_NS));

        Ddi4Group group = converter.toGroup(groupXml);

        assertEquals("group-id", group.id());
        assertEquals("fr.insee", group.agency());
        assertEquals("1", group.version());
        assertEquals("insee:StatisticalOperationSeries", group.typeOfGroup());
        assertEquals(List.of("http://id.insee.fr/operations/serie/s1001"), group.seriesIris());
        assertEquals(1, group.studyUnitReference().size());
        assertEquals("su-1", group.studyUnitReference().get(0).id());
    }

    @Test
    void shouldParseCategorySchemeFromFragmentXml() {
        Ddi4CategoryScheme original = new Ddi4CategoryScheme(
                Ddi4CategoryScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:cats-id:1",
                "fr.insee",
                "cats-id",
                "1",
                LangStrings.of("fr-FR", "Schéma catégories"),
                List.of(Reference.of("fr.insee", "cat-1", "1", "Category")));
        String xml = new Ddi4ToLifecycle33("abcde").toCategoryScheme(original).xmlText(logicalProductFragmentOptions());

        Ddi4CategoryScheme scheme = converter.toCategoryScheme(xml);

        assertEquals("cats-id", scheme.id());
        assertEquals("fr.insee", scheme.agency());
        assertEquals(1, scheme.categoryReference().size());
        assertEquals("cat-1", scheme.categoryReference().get(0).id());
    }

    @Test
    void shouldParseVariableSchemeFromFragmentXml() {
        Ddi4VariableScheme original = new Ddi4VariableScheme(
                Ddi4VariableScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:vars-id:1",
                "fr.insee",
                "vars-id",
                "1",
                LangStrings.of("fr-FR", "Schéma variables"),
                List.of(Reference.of("fr.insee", "var-1", "1", "Variable")));
        String xml = new Ddi4ToLifecycle33("abcde").toVariableScheme(original).xmlText(logicalProductFragmentOptions());

        Ddi4VariableScheme scheme = converter.toVariableScheme(xml);

        assertEquals("vars-id", scheme.id());
        assertEquals("fr.insee", scheme.agency());
        assertEquals(1, scheme.variableReference().size());
        assertEquals("var-1", scheme.variableReference().get(0).id());
    }

    @Test
    void shouldParseLogicalProductFromFragmentXml() {
        // Round-trip through the real serializer: this is what reading a group's existing
        // LogicalProduct relies on, to add a scheme reference to it instead of creating a second one.
        Ddi4LogicalProduct original = logicalProductReferencingEverySchemeKind("Produit logique");
        String xml = new Ddi4ToLifecycle33("abcde").toLogicalProduct(original).xmlText(logicalProductFragmentOptions());

        Ddi4LogicalProduct logicalProduct = converter.toLogicalProduct(xml);

        assertEquals("lp-id", logicalProduct.id());
        assertEquals("fr.insee", logicalProduct.agency());
        assertEquals("1", logicalProduct.version());
        assertEquals(1, logicalProduct.codeListSchemeReference().size());
        assertEquals("cls-1", logicalProduct.codeListSchemeReference().get(0).id());
        assertEquals(1, logicalProduct.categorySchemeReference().size());
        assertEquals("cats-1", logicalProduct.categorySchemeReference().get(0).id());
        assertEquals(1, logicalProduct.variableSchemeReference().size());
        assertEquals("vars-1", logicalProduct.variableSchemeReference().get(0).id());
        assertEquals(1, logicalProduct.managedRepresentationSchemeReference().size());
        assertEquals(
                "mrs-1",
                logicalProduct.managedRepresentationSchemeReference().get(0).id());
    }

    @Test
    void shouldParseStudyUnitFromFragmentXml() {
        Ddi4StudyUnit original = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1",
                "fr.insee",
                "su-id",
                "1",
                new Citation(LangStrings.of("fr-FR", "Study Unit")),
                "http://id.insee.fr/operations/operation/op1",
                List.of(Reference.of("fr.insee", "pi-1", "1", "PhysicalInstance")),
                List.of(Reference.of("fr.insee", "lp-1", "1", "LogicalProduct")));
        String xml = new Ddi4ToLifecycle33("abcde").toStudyUnit(original).xmlText(fragmentOptions(STUDY_UNIT_NS));

        Ddi4StudyUnit studyUnit = converter.toStudyUnit(xml);

        assertEquals("su-id", studyUnit.id());
        assertEquals("http://id.insee.fr/operations/operation/op1", studyUnit.operationIri());
        assertEquals(1, studyUnit.physicalInstanceReferences().size());
        assertEquals("pi-1", studyUnit.physicalInstanceReferences().get(0).id());
    }

    private static XmlOptions logicalProductFragmentOptions() {
        return fragmentOptions(LOGICAL_PRODUCT_NS);
    }

    @Test
    void shouldConvertPhysicalInstanceFromDdi3() {
        // Given
        String physicalInstanceXml = ddi3Fragment("PhysicalInstance", PHYSICAL_INSTANCE_NS, "saphir-rp99-sas", """
                <r:Citation>
                    <r:Title>
                        <r:String xml:lang="fr-FR">SAPHIR - Fichier Individus RP99 (.sas7bdat)</r:String>
                    </r:Title>
                </r:Citation>
                <r:DataRelationshipReference>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>saphir-rp99-sas</r:ID>
                    <r:Version>1</r:Version>
                    <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                </r:DataRelationshipReference>
                """);

        Ddi3Response ddi3 = ddi3Response(ddi3Item(PHYSICAL_INSTANCE_TYPE, "saphir-rp99-sas", physicalInstanceXml));

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());

        Ddi4PhysicalInstance pi = result.physicalInstance().get(0);
        assertEquals("2025-01-21T13:48:46.363", pi.versionDate().dateTime());
        assertEquals("urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1", pi.urn());
        assertEquals("fr.insee", pi.agency());
        assertEquals("saphir-rp99-sas", pi.id());
        assertEquals("1", pi.version());

        assertNotNull(pi.citation());
        assertNotNull(pi.citation().title());
        assertEquals("fr-FR", pi.citation().title().get(0).language());
        assertEquals(
                "SAPHIR - Fichier Individus RP99 (.sas7bdat)",
                pi.citation().title().get(0).value());

        assertNotNull(pi.dataRelationshipReference());
        assertEquals(1, pi.dataRelationshipReference().size());
        assertEquals("fr.insee", pi.dataRelationshipReference().get(0).agency());
        assertEquals("saphir-rp99-sas", pi.dataRelationshipReference().get(0).id());
        assertEquals("1", pi.dataRelationshipReference().get(0).version());
        assertEquals("DataRelationship", pi.dataRelationshipReference().get(0).type());
    }

    @Test
    void shouldConvertDataRelationshipFromDdi3() {
        // Given
        String dataRelationshipXml = ddi3Fragment("DataRelationship", LOGICAL_PRODUCT_NS, "saphir-rp99-sas", """
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
                """);

        Ddi3Response ddi3 = ddi3Response(ddi3Item(DATA_RELATIONSHIP_TYPE, "saphir-rp99-sas", dataRelationshipXml));

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.dataRelationship());
        assertEquals(1, result.dataRelationship().size());

        Ddi4DataRelationship dr = result.dataRelationship().get(0);
        assertEquals("2025-01-21T13:48:46.363", dr.versionDate().dateTime());
        assertEquals("urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1", dr.urn());
        assertEquals("fr.insee", dr.agency());
        assertEquals("saphir-rp99-sas", dr.id());
        assertEquals("1", dr.version());

        assertNotNull(dr.label());
        assertEquals("SAPHIR - RP99", dr.label().get(0).value());

        assertNotNull(dr.logicalRecord());
        assertEquals(1, dr.logicalRecord().size());
        assertNotNull(dr.logicalRecord().get(0).variablesInRecord());
        assertEquals(
                1,
                dr.logicalRecord()
                        .get(0)
                        .variablesInRecord()
                        .variableUsedReference()
                        .size());
        assertEquals(
                "AGEMEN8",
                dr.logicalRecord()
                        .get(0)
                        .variablesInRecord()
                        .variableUsedReference()
                        .get(0)
                        .id());
    }

    @Test
    void shouldConvertVariableWithCodeRepresentationFromDdi3() {
        // Given
        String variableXml = ddi3Fragment("Variable", LOGICAL_PRODUCT_NS, "AGEMEN8", """
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
                """);

        // When
        Ddi4Variable variable = convertSingleVariable(ddi3Item(VARIABLE_TYPE, "AGEMEN8", variableXml));

        // Then
        assertEquals("2025-01-21T13:48:46.363", variable.versionDate().dateTime());
        assertEquals("urn:ddi:fr.insee:Variable.AGEMEN8:1", variable.urn());
        assertEquals("fr.insee", variable.agency());
        assertEquals("AGEMEN8", variable.id());
        assertEquals("1", variable.version());

        assertNotNull(variable.variableName());
        assertEquals("AGEMEN8", variable.variableName().get(0).value());

        assertNotNull(variable.label());
        assertEquals("Âge détaillé", variable.label().get(0).value());

        assertNotNull(variable.description());
        assertEquals(
                "Âge de l'individu en années révolues",
                variable.description().get(0).value());

        assertNotNull(variable.variableRepresentation());
        assertEquals("Demographic", variable.variableRepresentation().variableRole());
        assertNotNull(variable.variableRepresentation().codeRepresentation());
        assertEquals(
                false, variable.variableRepresentation().codeRepresentation().blankIsMissingValue());
        assertEquals(
                "CL_AGEMEN8",
                variable.variableRepresentation()
                        .codeRepresentation()
                        .codeListReference()
                        .id());
    }

    @Test
    void shouldConvertVariableWithNumericRepresentationFromDdi3() {
        // Given
        String variableXml = ddi3Fragment("Variable", LOGICAL_PRODUCT_NS, "AGE", """
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
                """);

        // When
        Ddi4Variable variable = convertSingleVariable(ddi3Item(VARIABLE_TYPE, "AGE", variableXml));

        // Then
        assertNotNull(variable.variableRepresentation());
        assertNotNull(variable.variableRepresentation().numericRepresentation());

        NumericRepresentation numRep = variable.variableRepresentation().numericRepresentation();
        assertEquals("Integer", numRep.numericTypeCode());
        assertNotNull(numRep.numberRange());
        assertEquals(true, numRep.numberRange().low().isInclusive());
        assertEquals(0.0, numRep.numberRange().low().value());
        assertEquals(true, numRep.numberRange().high().isInclusive());
        assertEquals(120.0, numRep.numberRange().high().value());
    }

    @Test
    void shouldConvertCodeListFromDdi3() {
        // Given
        String codeListXml = ddi3Fragment("CodeList", LOGICAL_PRODUCT_NS, "CL_AGEMEN8", """
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
                """);

        Ddi3Response ddi3 = ddi3Response(ddi3Item(CODE_LIST_TYPE, "CL_AGEMEN8", codeListXml));

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.codeList());
        assertEquals(1, result.codeList().size());

        Ddi4CodeList cl = result.codeList().get(0);
        assertEquals("urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1", cl.urn());
        assertEquals("fr.insee", cl.agency());
        assertEquals("CL_AGEMEN8", cl.id());
        assertEquals("1", cl.version());

        assertNotNull(cl.label());
        assertEquals("Liste de codes - Âge détaillé", cl.label().get(0).value());

        assertNotNull(cl.code());
        assertEquals(2, cl.code().size());

        Code code0 = cl.code().get(0);
        assertEquals("0", code0.id());
        assertEquals("0", code0.value().stringValue());
        assertEquals("CAT_0", code0.categoryReference().id());

        Code code1 = cl.code().get(1);
        assertEquals("1", code1.id());
        assertEquals("1", code1.value().stringValue());
        assertEquals("CAT_1", code1.categoryReference().id());
    }

    @Test
    void shouldConvertCategoryFromDdi3() {
        // Given
        String categoryXml = categoryFragment("CAT_0", "0 an");

        Ddi3Response ddi3 = ddi3Response(ddi3Item(CATEGORY_TYPE, "CAT_0", categoryXml));

        // When
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL);

        // Then
        assertNotNull(result);
        assertNotNull(result.category());
        assertEquals(1, result.category().size());

        Ddi4Category cat = result.category().get(0);
        assertEquals("2025-01-21T13:48:46.363", cat.versionDate().dateTime());
        assertEquals("urn:ddi:fr.insee:Category.CAT_0:1", cat.urn());
        assertEquals("fr.insee", cat.agency());
        assertEquals("CAT_0", cat.id());
        assertEquals("1", cat.version());

        assertNotNull(cat.label());
        assertEquals("0 an", cat.label().get(0).value());
    }

    @Test
    void shouldHandleEmptyDdi3Response() {
        // Given
        Ddi3Response ddi3 = ddi3Response();

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
    void shouldConvertVariableWithTextRepresentationFromDdi3() {
        // Given
        String variableXml = fragment(versionable(
                "Variable",
                inNamespace(LOGICAL_PRODUCT_NS, VERSION_DATE) + " isGeographic=\"true\"",
                "urn:ddi:fr.insee:Variable.NAME:1",
                "NAME",
                """
                <VariableName>
                    <r:String xml:lang="fr-FR">NAME</r:String>
                </VariableName>
                <r:Label>
                    <r:Content xml:lang="fr-FR">Name</r:Content>
                </r:Label>
                <r:Description>
                    <r:Content xml:lang="fr-FR">Person name</r:Content>
                </r:Description>
                <VariableRepresentation>
                    <r:TextRepresentation blankIsMissingValue="false" minLength="1" maxLength="50" regExp="[A-Za-z ]+"/>
                </VariableRepresentation>
                """));

        // When
        Ddi4Variable variable = convertSingleVariable(ddi3Item(VARIABLE_TYPE, "NAME", variableXml));

        // Then
        assertEquals(true, variable.isGeographic());
        assertEquals("NAME", variable.variableName().get(0).value());
        assertEquals("Name", variable.label().get(0).value());
        assertEquals("Person name", variable.description().get(0).value());

        assertNotNull(variable.variableRepresentation());
        assertNotNull(variable.variableRepresentation().textRepresentation());

        TextRepresentation textRep = variable.variableRepresentation().textRepresentation();
        assertEquals(50, textRep.maxLength());
        assertEquals(1, textRep.minLength());
        assertEquals("[A-Za-z ]+", textRep.regExp());
        assertEquals(false, textRep.blankIsMissingValue());
    }

    @Test
    void shouldConvertVariableWithDateTimeRepresentationFromDdi3() {
        // Given
        String variableXml = ddi3Fragment("Variable", LOGICAL_PRODUCT_NS, "BIRTHDATE", """
                <VariableName>
                    <r:String xml:lang="fr-FR">BIRTHDATE</r:String>
                </VariableName>
                <r:Label>
                    <r:Content xml:lang="fr-FR">Birth Date</r:Content>
                </r:Label>
                <VariableRepresentation>
                    <r:DateTimeRepresentation>
                        <r:DateTypeCode>Date</r:DateTypeCode>
                        <r:DateFieldFormat>YYYY-MM-DD</r:DateFieldFormat>
                    </r:DateTimeRepresentation>
                </VariableRepresentation>
                """);

        // When
        Ddi4Variable variable = convertSingleVariable(ddi3Item(VARIABLE_TYPE, "BIRTHDATE", variableXml));

        // Then
        assertEquals("BIRTHDATE", variable.variableName().get(0).value());
        assertEquals("Birth Date", variable.label().get(0).value());

        assertNotNull(variable.variableRepresentation());
        assertNotNull(variable.variableRepresentation().dateTimeRepresentation());

        DateTimeRepresentation dateTimeRep = variable.variableRepresentation().dateTimeRepresentation();
        assertEquals("Date", dateTimeRep.dateTypeCode());
        assertEquals("YYYY-MM-DD", dateTimeRep.dateFieldFormat());
    }

    @Test
    void shouldThrowExceptionForMalformedXml() {
        // Given
        Ddi3Response ddi3 = ddi3Response(ddi3Item(PHYSICAL_INSTANCE_TYPE, "test", "<invalid>xml<not-closed>"));

        // When & Then
        assertThrows(RuntimeException.class, () -> converter.convertDdi3ToDdi4(ddi3, SCHEMA_URL));
    }

    private Ddi3Response createCompleteDdi3Response() {
        return ddi3Response(
                ddi3Item(
                        PHYSICAL_INSTANCE_TYPE,
                        "test-pi",
                        ddi3Fragment("PhysicalInstance", PHYSICAL_INSTANCE_NS, "test-pi", """
                        <r:Citation>
                            <r:Title>
                                <r:String xml:lang="fr-FR">Test Instance</r:String>
                            </r:Title>
                        </r:Citation>
                        """)),
                ddi3Item(
                        DATA_RELATIONSHIP_TYPE,
                        "test-dr",
                        ddi3Fragment("DataRelationship", LOGICAL_PRODUCT_NS, "test-dr", "")),
                ddi3Item(VARIABLE_TYPE, "VAR1", variableNamedAsItsId("VAR1")),
                ddi3Item(VARIABLE_TYPE, "VAR2", variableNamedAsItsId("VAR2")),
                ddi3Item(CODE_LIST_TYPE, "CL_TEST", ddi3Fragment("CodeList", LOGICAL_PRODUCT_NS, "CL_TEST", "")),
                ddi3Item(CATEGORY_TYPE, "CAT_0", categoryFragment("CAT_0", "Category 0")),
                ddi3Item(CATEGORY_TYPE, "CAT_1", categoryFragment("CAT_1", "Category 1")));
    }

    /** Converts a set holding the given item, which must yield exactly one variable, and returns it. */
    private Ddi4Variable convertSingleVariable(Ddi3Response.Ddi3Item item) {
        Ddi4Response result = converter.convertDdi3ToDdi4(ddi3Response(item), SCHEMA_URL);

        assertNotNull(result);
        assertNotNull(result.variable());
        assertEquals(1, result.variable().size());
        return result.variable().get(0);
    }

    private static Ddi3Response ddi3Response(Ddi3Response.Ddi3Item... items) {
        return new Ddi3Response(new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")), List.of(items));
    }

    private static Ddi3Response.Ddi3Item ddi3Item(String itemType, String identifier, String xml) {
        return ddi3Item(itemType, identifier, xml, VERSION_DATE);
    }

    private static Ddi3Response.Ddi3Item ddi3Item(String itemType, String identifier, String xml, String versionDate) {
        return new Ddi3Response.Ddi3Item(
                itemType,
                "fr.insee",
                "1",
                identifier,
                xml,
                versionDate,
                "abcde",
                false,
                false,
                false,
                "DC337820-AF3A-4C0B-82F9-CF02535CDE83");
    }

    /** Fragment of a fr.insee item versioned on {@link #VERSION_DATE}, with a Colectica-style URN. */
    private static String ddi3Fragment(String element, String namespace, String id, String body) {
        return fragment(versionable(
                element,
                inNamespace(namespace, VERSION_DATE),
                "urn:ddi:fr.insee:" + element + "." + id + ":1",
                id,
                body));
    }

    private static String variableNamedAsItsId(String id) {
        return ddi3Fragment(
                "Variable",
                LOGICAL_PRODUCT_NS,
                id,
                "<VariableName><r:String xml:lang=\"fr-FR\">" + id + "</r:String></VariableName>");
    }

    private static String categoryFragment(String id, String label) {
        return fragment(versionable(
                "Category",
                inNamespace(LOGICAL_PRODUCT_NS, VERSION_DATE) + " isMissing=\"false\"",
                "urn:ddi:fr.insee:Category." + id + ":1",
                id,
                "<r:Label><r:Content xml:lang=\"fr-FR\">" + label + "</r:Content></r:Label>"));
    }
}
