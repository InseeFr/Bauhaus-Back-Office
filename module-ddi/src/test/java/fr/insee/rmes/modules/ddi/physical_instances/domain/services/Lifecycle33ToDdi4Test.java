package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.GROUP_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.LOGICAL_PRODUCT_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.PHYSICAL_INSTANCE_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.STUDY_UNIT_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.fragment;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.inNamespace;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.reference;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.sentinelValuesMmvrFragment;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.universallyUnique;
import static fr.insee.rmes.modules.ddi.physical_instances.domain.services.Lifecycle33TestFixtures.versionable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Level;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

class Lifecycle33ToDdi4Test {

    private static final String VERSION_DATE = "2025-12-23T09:52:06.355Z";
    private static final String SCHEME_VERSION_DATE = "2026-04-03T12:00:00Z";
    private static final String TEST_INSTANCE_CITATION =
            "<r:Citation><r:Title><r:String xml:lang=\"fr-FR\">Test Instance</r:String></r:Title></r:Citation>";

    private final Lifecycle33ToDdi4 converter = new Lifecycle33ToDdi4();

    /**
     * Le {@code VersionResponsibility} du fragment DDI 3.3 doit ressortir dans le JSON DDI 4 :
     * c'est là que le front le lit.
     */
    @Test
    void shouldExposeVersionResponsibilityInTheDdi4Json() throws Exception {
        FragmentDocument doc = physicalInstance(
                "2026-12-23T09:52:06.355Z",
                "<r:VersionResponsibility>responsable-configure</r:VersionResponsibility>" + TEST_INSTANCE_CITATION);

        Ddi4PhysicalInstance pi = converter.toPhysicalInstance(doc);

        assertThat(new ObjectMapper().writeValueAsString(pi))
                .contains("\"VersionResponsibility\":\"responsable-configure\"");
    }

    @Test
    void shouldParsePhysicalInstanceWithoutBasedOnObject() throws XmlException {
        FragmentDocument doc = physicalInstance(VERSION_DATE, TEST_INSTANCE_CITATION);

        Ddi4PhysicalInstance pi = converter.toPhysicalInstance(doc);

        assertThat(pi.versionDate().dateTime()).isEqualTo("2025-12-23T09:52:06.355Z");
        assertThat(pi.urn()).isEqualTo("urn:ddi:fr.insee:pi-id:1");
        assertThat(pi.agency()).isEqualTo("fr.insee");
        assertThat(pi.id()).isEqualTo("pi-id");
        assertThat(pi.basedOnObject()).isNull();
        assertThat(pi.dataRelationshipReference()).isNull();
        assertThat(pi.citation().title().get(0).value()).isEqualTo("Test Instance");
    }

    @Test
    void shouldParsePhysicalInstanceWithBasedOnObjectAndDataRelationshipReference() throws XmlException {
        FragmentDocument doc = physicalInstance(VERSION_DATE, """
                <r:BasedOnObject><r:BasedOnReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>original-pi</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                </r:BasedOnReference></r:BasedOnObject>
                <r:Citation><r:Title><r:String xml:lang="fr-FR">Test</r:String></r:Title></r:Citation>
                <r:DataRelationshipReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>dr-id</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                </r:DataRelationshipReference>
                """);

        Ddi4PhysicalInstance pi = converter.toPhysicalInstance(doc);

        assertThat(pi.basedOnObject().type()).isEqualTo("BasedOnObjectType");
        assertThat(pi.basedOnObject().basedOnReferences()).hasSize(1);
        assertThat(pi.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-pi");
        assertThat(pi.basedOnObject().basedOnReferences().get(0).type()).isEqualTo("PhysicalInstance");
        assertThat(pi.dataRelationshipReference()).hasSize(1);
        assertThat(pi.dataRelationshipReference().get(0).id()).isEqualTo("dr-id");
        assertThat(pi.dataRelationshipReference().get(0).type()).isEqualTo("DataRelationship");
    }

    @Test
    void shouldParseDataRelationshipWithLabelFallbackOnName() throws XmlException {
        FragmentDocument doc = logicalProductItem("DataRelationship", "dr-id", """
                <DataRelationshipName><r:String xml:lang="fr-FR">DR Name</r:String></DataRelationshipName>
                """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.label().get(0).value()).isEqualTo("DR Name");
        assertThat(dr.logicalRecord()).isNull();
    }

    @Test
    void shouldParseDataRelationshipWithExplicitLabelOverridingName() throws XmlException {
        FragmentDocument doc = logicalProductItem("DataRelationship", "dr-id", """
                <DataRelationshipName><r:String xml:lang="fr-FR">DR Name</r:String></DataRelationshipName>
                <r:Label><r:Content xml:lang="fr-FR">DR Label</r:Content></r:Label>
                """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.label().get(0).value()).isEqualTo("DR Label");
    }

    @Test
    void shouldParseDataRelationshipWithLogicalRecordAndVariablesInRecord() throws XmlException {
        FragmentDocument doc = logicalProductItem("DataRelationship", "dr-id", """
                <LogicalRecord isUniversallyUnique="true">
                    <r:URN>urn:ddi:fr.insee:lr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>lr-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">LR Label</r:Content></r:Label>
                    <VariablesInRecord>
                        <VariableUsedReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>var-1</r:ID><r:Version>1</r:Version>
                            <r:TypeOfObject>Variable</r:TypeOfObject>
                        </VariableUsedReference>
                    </VariablesInRecord>
                </LogicalRecord>
                """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.logicalRecord()).hasSize(1);
        assertThat(dr.logicalRecord().get(0).id()).isEqualTo("lr-id");
        assertThat(dr.logicalRecord().get(0).label().get(0).value()).isEqualTo("LR Label");
        assertThat(dr.logicalRecord().get(0).variablesInRecord().variableUsedReference())
                .hasSize(1);
        assertThat(dr.logicalRecord()
                        .get(0)
                        .variablesInRecord()
                        .variableUsedReference()
                        .get(0)
                        .id())
                .isEqualTo("var-1");
    }

    @Test
    void shouldParseVariableWithBasedOnObjectAndLabel() throws XmlException {
        FragmentDocument doc = logicalProductItem("Variable", "var-id", """
                <r:BasedOnObject><r:BasedOnReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>original-var</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>Variable</r:TypeOfObject>
                </r:BasedOnReference></r:BasedOnObject>
                <VariableName><r:String xml:lang="fr-FR">VAR_NAME</r:String></VariableName>
                <r:Label><r:Content xml:lang="fr-FR">Variable Label</r:Content></r:Label>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-var");
        assertThat(variable.variableName().get(0).value()).isEqualTo("VAR_NAME");
        assertThat(variable.label().get(0).value()).isEqualTo("Variable Label");
        assertThat(variable.description()).isNull();
    }

    @Test
    void shouldParseVariableWithDescriptionInEveryLanguage() throws XmlException {
        FragmentDocument doc = logicalProductItem("Variable", "var-id", """
                <r:Description>
                    <r:Content xml:lang="en-IE">English description</r:Content>
                    <r:Content xml:lang="fr-FR">Description française</r:Content>
                </r:Description>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.description())
                .extracting(LangString::language, LangString::value)
                .containsExactly(tuple("en-IE", "English description"), tuple("fr-FR", "Description française"));
    }

    @Test
    void shouldParseVariableWithCodeRepresentation() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("<r:CodeRepresentation blankIsMissingValue=\"true\">"
                + reference("r:CodeListReference", "cl-id", "CodeList")
                + "</r:CodeRepresentation>");

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().codeRepresentation().blankIsMissingValue())
                .isEqualTo(true);
        assertThat(variable.variableRepresentation()
                        .codeRepresentation()
                        .codeListReference()
                        .id())
                .isEqualTo("cl-id");
        assertThat(variable.variableRepresentation().numericRepresentation()).isNull();
    }

    /**
     * Valeurs sentinelles (#1566) : {@code MissingValuesReference} (élément local du namespace
     * logicalproduct, frère de la représentation) est lu sur le wrapper {@code VariableRepresentation}.
     */
    @Test
    void shouldParseVariableWithMissingValuesReference() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("<r:CodeRepresentation blankIsMissingValue=\"false\">"
                + reference("r:CodeListReference", "cl-id", "CodeList")
                + "</r:CodeRepresentation>"
                + reference("MissingValuesReference", "mmvr-1", "ManagedMissingValuesRepresentation"));

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().missingValuesReference())
                .isEqualTo(Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation"));
        assertThat(variable.variableRepresentation()
                        .codeRepresentation()
                        .codeListReference()
                        .id())
                .isEqualTo("cl-id");
    }

    /** Une variable peut déclarer un rôle sans aucune ValueRepresentation : aucun lecteur ne s'applique. */
    @Test
    void shouldParseVariableWithoutValueRepresentation() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("""
                <VariableRole>Identifier</VariableRole>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().variableRole()).isEqualTo("Identifier");
        assertThat(variable.variableRepresentation().codeRepresentation()).isNull();
        assertThat(variable.variableRepresentation().numericRepresentation()).isNull();
        assertThat(variable.variableRepresentation().dateTimeRepresentation()).isNull();
        assertThat(variable.variableRepresentation().textRepresentation()).isNull();
    }

    @Test
    void shouldParseVariableWithNumericRepresentation() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("""
                <r:NumericRepresentation blankIsMissingValue="false">
                    <r:NumberRange>
                        <r:Low isInclusive="false">0</r:Low>
                        <r:High isInclusive="true">100</r:High>
                    </r:NumberRange>
                    <r:NumericTypeCode>Integer</r:NumericTypeCode>
                </r:NumericRepresentation>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().numericRepresentation().numericTypeCode())
                .isEqualTo("Integer");
        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .low()
                        .isInclusive())
                .isEqualTo(false);
        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .low()
                        .value())
                .isEqualTo(0.0);
        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .high()
                        .isInclusive())
                .isEqualTo(true);
        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .high()
                        .value())
                .isEqualTo(100.0);
    }

    @Test
    void shouldParseVariableWithDecimalNumericBounds() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("""
                <r:NumericRepresentation blankIsMissingValue="false">
                    <r:NumberRange>
                        <r:Low isInclusive="true">0.0001</r:Low>
                        <r:High isInclusive="true">12345678.5</r:High>
                    </r:NumberRange>
                    <r:NumericTypeCode>Decimal</r:NumericTypeCode>
                </r:NumericRepresentation>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .low()
                        .value())
                .isEqualTo(0.0001);
        assertThat(variable.variableRepresentation()
                        .numericRepresentation()
                        .numberRange()
                        .high()
                        .value())
                .isEqualTo(12345678.5);
    }

    @Test
    void shouldParseVariableWithDateTimeRepresentation() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("""
                <r:DateTimeRepresentation>
                    <r:DateTypeCode>Date</r:DateTypeCode>
                    <r:DateFieldFormat>yyyy-MM-dd</r:DateFieldFormat>
                </r:DateTimeRepresentation>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().dateTimeRepresentation().dateTypeCode())
                .isEqualTo("Date");
        assertThat(variable.variableRepresentation().dateTimeRepresentation().dateFieldFormat())
                .isEqualTo("yyyy-MM-dd");
    }

    @Test
    void shouldParseVariableWithTextRepresentation() throws XmlException {
        FragmentDocument doc = variableWithRepresentation("""
                <r:TextRepresentation blankIsMissingValue="true" minLength="1" maxLength="255" regExp="[A-Z]+"/>
                """);

        Ddi4Variable variable = converter.toVariable(doc);

        assertThat(variable.variableRepresentation().textRepresentation().minLength())
                .isEqualTo(1);
        assertThat(variable.variableRepresentation().textRepresentation().maxLength())
                .isEqualTo(255);
        assertThat(variable.variableRepresentation().textRepresentation().regExp())
                .isEqualTo("[A-Z]+");
        assertThat(variable.variableRepresentation().textRepresentation().blankIsMissingValue())
                .isEqualTo(true);
    }

    @Test
    void shouldParseCodeListWithCodes() throws XmlException {
        FragmentDocument doc = logicalProductItem("CodeList", "cl-id", """
                <r:Label><r:Content xml:lang="fr-FR">CodeList Label</r:Content></r:Label>
                <Code isUniversallyUnique="true">
                    <r:URN>urn:ddi:fr.insee:code-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>code-id</r:ID><r:Version>1</r:Version>
                    <r:CategoryReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cat-id</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>Category</r:TypeOfObject>
                    </r:CategoryReference>
                    <r:Value>01</r:Value>
                </Code>
                """);

        Ddi4CodeList cl = converter.toCodeList(doc);

        assertThat(cl.id()).isEqualTo("cl-id");
        assertThat(cl.label().get(0).value()).isEqualTo("CodeList Label");
        assertThat(cl.code()).hasSize(1);
        assertThat(cl.code().get(0).categoryReference().id()).isEqualTo("cat-id");
        assertThat(cl.code().get(0).value().stringValue()).isEqualTo("01");
    }

    @Test
    void shouldParseCategoryBasedOnObject() throws XmlException {
        FragmentDocument doc = logicalProductItem("Category", "variant-cat", """
                <r:BasedOnObject>
                    <r:BasedOnReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>original-cat</r:ID><r:Version>3</r:Version>
                        <r:TypeOfObject>Category</r:TypeOfObject>
                    </r:BasedOnReference>
                </r:BasedOnObject>
                """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.basedOnObject()).isNotNull();
        assertThat(cat.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cat");
        assertThat(cat.basedOnObject().basedOnReferences().get(0).version()).isEqualTo("3");
    }

    @Test
    void shouldParseCodeListBasedOnObject() throws XmlException {
        FragmentDocument doc = logicalProductItem("CodeList", "variant-id", """
                <r:BasedOnObject>
                    <r:BasedOnReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>original-cl-id</r:ID><r:Version>2</r:Version>
                        <r:TypeOfObject>CodeList</r:TypeOfObject>
                    </r:BasedOnReference>
                </r:BasedOnObject>
                """);

        Ddi4CodeList cl = converter.toCodeList(doc);

        assertThat(cl.basedOnObject()).isNotNull();
        assertThat(cl.basedOnObject().basedOnReferences()).hasSize(1);
        assertThat(cl.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cl-id");
        assertThat(cl.basedOnObject().basedOnReferences().get(0).version()).isEqualTo("2");
    }

    @Test
    void shouldParseCodeListLevels() throws XmlException {
        FragmentDocument doc = logicalProductItem("CodeList", "cl-id", """
                <r:Label><r:Content xml:lang="fr-FR">NUTS</r:Content></r:Label>
                <Level levelNumber="0">
                    <LevelName><r:String xml:lang="fr-FR">NUTS 0</r:String></LevelName>
                    <CategoryRelationship>Nominal</CategoryRelationship>
                </Level>
                <Level levelNumber="1">
                    <LevelName><r:String xml:lang="fr-FR">NUTS 1</r:String></LevelName>
                    <CategoryRelationship>Ordinal</CategoryRelationship>
                </Level>
                """);

        Ddi4CodeList cl = converter.toCodeList(doc);

        assertThat(cl.level()).hasSize(2);
        Level level0 = cl.level().get(0);
        assertThat(level0.levelNumber()).isEqualTo(0);
        assertThat(level0.levelName()).extracting(LangString::value).containsExactly("NUTS 0");
        assertThat(level0.levelName()).extracting(LangString::language).containsExactly("fr-FR");
        assertThat(level0.categoryRelationship()).isEqualTo("Nominal");
        Level level1 = cl.level().get(1);
        assertThat(level1.levelNumber()).isEqualTo(1);
        assertThat(level1.levelName()).extracting(LangString::value).containsExactly("NUTS 1");
        assertThat(level1.categoryRelationship()).isEqualTo("Ordinal");
    }

    @Test
    void shouldParseHierarchicalCodeListWithNestedCodes() throws XmlException {
        FragmentDocument doc = logicalProductItem("CodeList", "cl-id", """
                <r:Label><r:Content xml:lang="fr-FR">NUTS</r:Content></r:Label>
                <Code isUniversallyUnique="true">
                    <r:URN>urn:ddi:fr.insee:code-at:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>code-at</r:ID><r:Version>1</r:Version>
                    <r:CategoryReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cat-at</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>Category</r:TypeOfObject>
                    </r:CategoryReference>
                    <r:Value>AT</r:Value>
                    <Code isUniversallyUnique="true">
                        <r:URN>urn:ddi:fr.insee:code-at2:1</r:URN>
                        <r:Agency>fr.insee</r:Agency><r:ID>code-at2</r:ID><r:Version>1</r:Version>
                        <r:CategoryReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>cat-at2</r:ID><r:Version>1</r:Version>
                            <r:TypeOfObject>Category</r:TypeOfObject>
                        </r:CategoryReference>
                        <r:Value>AT2</r:Value>
                        <Code isUniversallyUnique="true">
                            <r:URN>urn:ddi:fr.insee:code-at21:1</r:URN>
                            <r:Agency>fr.insee</r:Agency><r:ID>code-at21</r:ID><r:Version>1</r:Version>
                            <r:CategoryReference>
                                <r:Agency>fr.insee</r:Agency><r:ID>cat-at21</r:ID><r:Version>1</r:Version>
                                <r:TypeOfObject>Category</r:TypeOfObject>
                            </r:CategoryReference>
                            <r:Value>AT21</r:Value>
                        </Code>
                    </Code>
                </Code>
                <Code isUniversallyUnique="true">
                    <r:URN>urn:ddi:fr.insee:code-be:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>code-be</r:ID><r:Version>1</r:Version>
                    <r:CategoryReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cat-be</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>Category</r:TypeOfObject>
                    </r:CategoryReference>
                    <r:Value>BE</r:Value>
                </Code>
                """);

        Ddi4CodeList cl = converter.toCodeList(doc);

        assertThat(cl.code()).extracting(c -> c.value().stringValue()).containsExactly("AT", "BE");
        Code at = cl.code().get(0);
        assertThat(at.code()).hasSize(1);
        Code at2 = at.code().get(0);
        assertThat(at2.value().stringValue()).isEqualTo("AT2");
        assertThat(at2.categoryReference().id()).isEqualTo("cat-at2");
        assertThat(at2.code()).hasSize(1);
        Code at21 = at2.code().get(0);
        assertThat(at21.value().stringValue()).isEqualTo("AT21");
        assertThat(at21.urn()).isEqualTo("urn:ddi:fr.insee:code-at21:1");
        assertThat(at21.code()).isNull();
        assertThat(cl.code().get(1).code()).isNull();
    }

    @Test
    void shouldParseCodeListSchemeWithCodeListReferences() throws XmlException {
        FragmentDocument doc = parse(
                "CodeListScheme",
                LOGICAL_PRODUCT_NS,
                SCHEME_VERSION_DATE,
                "cls-id",
                "<r:Label><r:Content xml:lang=\"fr-FR\">CodeListScheme Label</r:Content></r:Label>"
                        + reference("r:CodeListReference", "cl-1", "CodeList")
                        + reference("r:CodeListReference", "cl-2", "CodeList"));

        Ddi4CodeListScheme scheme = converter.toCodeListScheme(doc);

        assertThat(scheme.id()).isEqualTo("cls-id");
        assertThat(scheme.agency()).isEqualTo("fr.insee");
        assertThat(scheme.version()).isEqualTo("1");
        assertThat(scheme.label().get(0).value()).isEqualTo("CodeListScheme Label");
        assertThat(scheme.codeListReference()).extracting(Reference::id).containsExactly("cl-1", "cl-2");
    }

    @Test
    void shouldParseManagedRepresentationSchemeWithMemberReferences() throws XmlException {
        // XML tel que renvoyé par Colectica GET item (le MRS est dans le namespace reusable).
        // Les membres peuvent être référencés par l'élément concret du type
        // (ManagedMissingValuesRepresentationReference…) ou, dans des données historiques, par la
        // tête générique ManagedRepresentationReference : les deux doivent être lus.
        FragmentDocument doc = FragmentDocument.Factory.parse(fragment(versionable(
                "r:ManagedRepresentationScheme",
                universallyUnique(SCHEME_VERSION_DATE),
                "urn:ddi:fr.insee:mrs-id:1",
                "mrs-id",
                """
                <r:Label><r:Content xml:lang="fr-FR">ManagedRepresentationScheme Label</r:Content></r:Label>
                <r:ManagedMissingValuesRepresentationReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>mmvr-1</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>ManagedMissingValuesRepresentation</r:TypeOfObject>
                </r:ManagedMissingValuesRepresentationReference>
                <r:ManagedRepresentationReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>mr-legacy</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>ManagedTextRepresentation</r:TypeOfObject>
                </r:ManagedRepresentationReference>
                """)));

        Ddi4ManagedRepresentationScheme scheme = converter.toManagedRepresentationScheme(doc);

        assertThat(scheme.id()).isEqualTo("mrs-id");
        assertThat(scheme.agency()).isEqualTo("fr.insee");
        assertThat(scheme.version()).isEqualTo("1");
        assertThat(scheme.label().get(0).value()).isEqualTo("ManagedRepresentationScheme Label");
        assertThat(scheme.managedRepresentationReference())
                .extracting(Reference::id)
                .containsExactly("mmvr-1", "mr-legacy");
        assertThat(scheme.managedRepresentationReference().get(0).type())
                .isEqualTo("ManagedMissingValuesRepresentation");
    }

    /**
     * Valeurs sentinelles (#1566) : lecture d'un fragment MMVR (miroir de
     * {@link Ddi4ToLifecycle33#toManagedMissingValuesRepresentation}) — Label et
     * MissingCodeRepresentation in-line avec sa CodeListReference.
     */
    @Test
    void shouldParseManagedMissingValuesRepresentation() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse(sentinelValuesMmvrFragment());

        Ddi4ManagedMissingValuesRepresentation mmvr = converter.toManagedMissingValuesRepresentation(doc);

        assertThat(mmvr).isNotNull();
        assertThat(mmvr.id()).isEqualTo("mmvr-1");
        assertThat(mmvr.agency()).isEqualTo("fr.insee");
        assertThat(mmvr.version()).isEqualTo("1");
        assertThat(mmvr.urn()).isEqualTo("urn:ddi:fr.insee:mmvr-1:1");
        assertThat(mmvr.label().get(0).value()).isEqualTo("Valeurs sentinelles NSP/REF");
        assertThat(mmvr.missingCodeRepresentation()).hasSize(1);
        assertThat(mmvr.missingCodeRepresentation().get(0).codeListReference())
                .isEqualTo(Reference.of("fr.insee", "cl-sentinelles", "1", "CodeList"));
    }

    @Test
    void shouldParseCategory() throws XmlException {
        FragmentDocument doc = logicalProductItem("Category", "cat-id", """
                <r:Label><r:Content xml:lang="fr-FR">Category Label</r:Content></r:Label>
                """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.id()).isEqualTo("cat-id");
        assertThat(cat.label().get(0).value()).isEqualTo("Category Label");
    }

    @Test
    void shouldParseCategoryWithLabelInEveryLanguage() throws XmlException {
        FragmentDocument doc = logicalProductItem("Category", "cat-id", """
                <r:Label>
                    <r:Content xml:lang="en-IE">Growing of non-perennial crops</r:Content>
                    <r:Content xml:lang="fr-FR">Cultures non permanentes</r:Content>
                </r:Label>
                """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.label()).hasSize(2);
        assertThat(cat.label())
                .extracting(LangString::language, LangString::value)
                .containsExactly(
                        tuple("en-IE", "Growing of non-perennial crops"), tuple("fr-FR", "Cultures non permanentes"));
    }

    @Test
    void shouldParseGroupWithAllFields() throws XmlException {
        FragmentDocument doc = parse("Group", GROUP_NS, SCHEME_VERSION_DATE, "group-id", """
                <r:UserID typeOfUserID="URI">http://id.insee.fr/operations/serie/s1001</r:UserID>
                <TypeOfGroup>insee:StatisticalOperationSeries</TypeOfGroup>
                <r:Citation><r:Title><r:String xml:lang="fr-FR">Test Group</r:String></r:Title></r:Citation>
                <r:StudyUnitReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>su-id-1</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>StudyUnit</r:TypeOfObject>
                </r:StudyUnitReference>
                """);

        Ddi4Group group = converter.toGroup(doc);

        assertThat(group.id()).isEqualTo("group-id");
        assertThat(group.typeOfGroup()).isEqualTo("insee:StatisticalOperationSeries");
        assertThat(group.seriesIris()).containsExactly("http://id.insee.fr/operations/serie/s1001");
        assertThat(group.citation().title().get(0).value()).isEqualTo("Test Group");
        assertThat(group.studyUnitReference()).hasSize(1);
        assertThat(group.studyUnitReference().get(0).id()).isEqualTo("su-id-1");
    }

    @Test
    void shouldParseGroupAlternateTitles() throws XmlException {
        FragmentDocument doc = parse("Group", GROUP_NS, SCHEME_VERSION_DATE, "group-id", """
                <r:Citation>
                    <r:Title>
                        <r:String xml:lang="fr-FR">Recensement</r:String>
                        <r:String xml:lang="en-GB">Census</r:String>
                    </r:Title>
                    <r:AlternateTitle><r:String xml:lang="fr-FR">RP</r:String></r:AlternateTitle>
                    <r:AlternateTitle><r:String xml:lang="en-GB">CENS</r:String></r:AlternateTitle>
                </r:Citation>
                """);

        Ddi4Group group = converter.toGroup(doc);

        assertThat(group.citation().title())
                .extracting(LangString::language, LangString::value)
                .containsExactly(tuple("fr-FR", "Recensement"), tuple("en-GB", "Census"));
        assertThat(group.citation().alternateTitle())
                .as("un réenregistrement du groupe repart de cette citation : ce qui n'est pas relu est perdu")
                .extracting(LangString::language, LangString::value)
                .containsExactly(tuple("fr-FR", "RP"), tuple("en-GB", "CENS"));
    }

    @Test
    void shouldParseStudyUnitWithOperationIri() throws XmlException {
        FragmentDocument doc = parse("StudyUnit", STUDY_UNIT_NS, SCHEME_VERSION_DATE, "su-id", """
                <r:UserID typeOfUserID="URI">http://id.insee.fr/operations/operation/op1</r:UserID>
                <r:Citation><r:Title><r:String xml:lang="fr-FR">Test SU</r:String></r:Title></r:Citation>
                <r:PhysicalInstanceReference>
                    <r:Agency>fr.insee</r:Agency><r:ID>pi-id</r:ID><r:Version>1</r:Version>
                    <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                </r:PhysicalInstanceReference>
                """);

        Ddi4StudyUnit su = converter.toStudyUnit(doc);

        assertThat(su.id()).isEqualTo("su-id");
        assertThat(su.operationIri()).isEqualTo("http://id.insee.fr/operations/operation/op1");
        assertThat(su.citation().title().get(0).value()).isEqualTo("Test SU");
        assertThat(su.physicalInstanceReferences()).hasSize(1);
        assertThat(su.physicalInstanceReferences().get(0).id()).isEqualTo("pi-id");
    }

    /** Parses a Fragment holding the fr.insee item {@code id} (URN {@code urn:ddi:fr.insee:<id>:1}). */
    private static FragmentDocument parse(String element, String namespace, String versionDate, String id, String body)
            throws XmlException {
        return FragmentDocument.Factory.parse(fragment(
                versionable(element, inNamespace(namespace, versionDate), "urn:ddi:fr.insee:" + id + ":1", id, body)));
    }

    /** Parses a Fragment holding the pi-id PhysicalInstance. */
    private static FragmentDocument physicalInstance(String versionDate, String body) throws XmlException {
        return parse("PhysicalInstance", PHYSICAL_INSTANCE_NS, versionDate, "pi-id", body);
    }

    /** Parses a Fragment holding a logicalproduct item versioned on {@link #VERSION_DATE}. */
    private static FragmentDocument logicalProductItem(String element, String id, String body) throws XmlException {
        return parse(element, LOGICAL_PRODUCT_NS, VERSION_DATE, id, body);
    }

    /** Parses a Fragment holding the {@code var} Variable with the given VariableRepresentation content. */
    private static FragmentDocument variableWithRepresentation(String representation) throws XmlException {
        return logicalProductItem(
                "Variable", "var", "<VariableRepresentation>" + representation + "</VariableRepresentation>");
    }
}
