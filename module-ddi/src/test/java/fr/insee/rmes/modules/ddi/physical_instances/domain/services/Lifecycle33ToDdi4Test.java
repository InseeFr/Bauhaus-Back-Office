package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class Lifecycle33ToDdi4Test {

    private final Lifecycle33ToDdi4 converter = new Lifecycle33ToDdi4();

    @Test
    void shouldParsePhysicalInstanceWithoutBasedOnObject() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:pi-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>pi-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation><r:Title><r:String xml:lang="fr-FR">Test Instance</r:String></r:Title></r:Citation>
                </PhysicalInstance>
            </Fragment>
            """);

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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:pi-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>pi-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:BasedOnObject><r:BasedOnReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>original-pi</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                    </r:BasedOnReference></r:BasedOnObject>
                    <r:Citation><r:Title><r:String xml:lang="fr-FR">Test</r:String></r:Title></r:Citation>
                    <r:DataRelationshipReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>dr-id</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                    </r:DataRelationshipReference>
                </PhysicalInstance>
            </Fragment>
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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName><r:String xml:lang="fr-FR">DR Name</r:String></DataRelationshipName>
                </DataRelationship>
            </Fragment>
            """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.label().get(0).value()).isEqualTo("DR Name");
        assertThat(dr.logicalRecord()).isNull();
    }

    @Test
    void shouldParseDataRelationshipWithExplicitLabelOverridingName() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName><r:String xml:lang="fr-FR">DR Name</r:String></DataRelationshipName>
                    <r:Label><r:Content xml:lang="fr-FR">DR Label</r:Content></r:Label>
                </DataRelationship>
            </Fragment>
            """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.label().get(0).value()).isEqualTo("DR Label");
    }

    @Test
    void shouldParseDataRelationshipWithLogicalRecordAndVariablesInRecord() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:dr-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>dr-id</r:ID>
                    <r:Version>1</r:Version>
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
                </DataRelationship>
            </Fragment>
            """);

        Ddi4DataRelationship dr = converter.toDataRelationship(doc);

        assertThat(dr.logicalRecord()).hasSize(1);
        assertThat(dr.logicalRecord().get(0).id()).isEqualTo("lr-id");
        assertThat(dr.logicalRecord().get(0).label().get(0).value()).isEqualTo("LR Label");
        assertThat(dr.logicalRecord().get(0).variablesInRecord().variableUsedReference()).hasSize(1);
        assertThat(dr.logicalRecord().get(0).variablesInRecord().variableUsedReference().get(0).id()).isEqualTo("var-1");
    }

    @Test
    void shouldParseVariableWithBasedOnObjectAndLabel() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>var-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:BasedOnObject><r:BasedOnReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>original-var</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>Variable</r:TypeOfObject>
                    </r:BasedOnReference></r:BasedOnObject>
                    <VariableName><r:String xml:lang="fr-FR">VAR_NAME</r:String></VariableName>
                    <r:Label><r:Content xml:lang="fr-FR">Variable Label</r:Content></r:Label>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-var");
        assertThat(var.variableName().get(0).value()).isEqualTo("VAR_NAME");
        assertThat(var.label().get(0).value()).isEqualTo("Variable Label");
        assertThat(var.description()).isNull();
    }

    @Test
    void shouldParseVariableWithDescriptionInEveryLanguage() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var-id</r:ID><r:Version>1</r:Version>
                    <r:Description>
                        <r:Content xml:lang="en-IE">English description</r:Content>
                        <r:Content xml:lang="fr-FR">Description française</r:Content>
                    </r:Description>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.description())
                .extracting(LangString::language, LangString::value)
                .containsExactly(
                        tuple("en-IE", "English description"),
                        tuple("fr-FR", "Description française"));
    }

    @Test
    void shouldParseVariableWithCodeRepresentation() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:CodeRepresentation blankIsMissingValue="true">
                            <r:CodeListReference>
                                <r:Agency>fr.insee</r:Agency><r:ID>cl-id</r:ID><r:Version>1</r:Version>
                                <r:TypeOfObject>CodeList</r:TypeOfObject>
                            </r:CodeListReference>
                        </r:CodeRepresentation>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().codeRepresentation().blankIsMissingValue()).isEqualTo(true);
        assertThat(var.variableRepresentation().codeRepresentation().codeListReference().id()).isEqualTo("cl-id");
        assertThat(var.variableRepresentation().numericRepresentation()).isNull();
    }

    /**
     * Valeurs sentinelles (#1566) : {@code MissingValuesReference} (élément local du namespace
     * logicalproduct, frère de la représentation) est lu sur le wrapper {@code VariableRepresentation}.
     */
    @Test
    void shouldParseVariableWithMissingValuesReference() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:CodeRepresentation blankIsMissingValue="false">
                            <r:CodeListReference>
                                <r:Agency>fr.insee</r:Agency><r:ID>cl-id</r:ID><r:Version>1</r:Version>
                                <r:TypeOfObject>CodeList</r:TypeOfObject>
                            </r:CodeListReference>
                        </r:CodeRepresentation>
                        <MissingValuesReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>mmvr-1</r:ID><r:Version>1</r:Version>
                            <r:TypeOfObject>ManagedMissingValuesRepresentation</r:TypeOfObject>
                        </MissingValuesReference>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().missingValuesReference())
                .isEqualTo(Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation"));
        assertThat(var.variableRepresentation().codeRepresentation().codeListReference().id()).isEqualTo("cl-id");
    }

    @Test
    void shouldParseVariableWithNumericRepresentation() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:NumericRepresentation blankIsMissingValue="false">
                            <r:NumberRange>
                                <r:Low isInclusive="false">0</r:Low>
                                <r:High isInclusive="true">100</r:High>
                            </r:NumberRange>
                            <r:NumericTypeCode>Integer</r:NumericTypeCode>
                        </r:NumericRepresentation>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().numericRepresentation().numericTypeCode()).isEqualTo("Integer");
        assertThat(var.variableRepresentation().numericRepresentation().numberRange().low().isInclusive()).isEqualTo(false);
        assertThat(var.variableRepresentation().numericRepresentation().numberRange().low().value()).isEqualTo(0.0);
        assertThat(var.variableRepresentation().numericRepresentation().numberRange().high().isInclusive()).isEqualTo(true);
        assertThat(var.variableRepresentation().numericRepresentation().numberRange().high().value()).isEqualTo(100.0);
    }

    @Test
    void shouldParseVariableWithDecimalNumericBounds() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:NumericRepresentation blankIsMissingValue="false">
                            <r:NumberRange>
                                <r:Low isInclusive="true">0.0001</r:Low>
                                <r:High isInclusive="true">12345678.5</r:High>
                            </r:NumberRange>
                            <r:NumericTypeCode>Decimal</r:NumericTypeCode>
                        </r:NumericRepresentation>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().numericRepresentation().numberRange().low().value()).isEqualTo(0.0001);
        assertThat(var.variableRepresentation().numericRepresentation().numberRange().high().value()).isEqualTo(12345678.5);
    }

    @Test
    void shouldParseVariableWithDateTimeRepresentation() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:DateTimeRepresentation>
                            <r:DateTypeCode>Date</r:DateTypeCode>
                            <r:DateFieldFormat>yyyy-MM-dd</r:DateFieldFormat>
                        </r:DateTimeRepresentation>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().dateTimeRepresentation().dateTypeCode()).isEqualTo("Date");
        assertThat(var.variableRepresentation().dateTimeRepresentation().dateFieldFormat()).isEqualTo("yyyy-MM-dd");
    }

    @Test
    void shouldParseVariableWithTextRepresentation() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:var:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>var</r:ID><r:Version>1</r:Version>
                    <VariableRepresentation>
                        <r:TextRepresentation blankIsMissingValue="true" minLength="1" maxLength="255" regExp="[A-Z]+"/>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """);

        Ddi4Variable var = converter.toVariable(doc);

        assertThat(var.variableRepresentation().textRepresentation().minLength()).isEqualTo(1);
        assertThat(var.variableRepresentation().textRepresentation().maxLength()).isEqualTo(255);
        assertThat(var.variableRepresentation().textRepresentation().regExp()).isEqualTo("[A-Z]+");
        assertThat(var.variableRepresentation().textRepresentation().blankIsMissingValue()).isEqualTo(true);
    }

    @Test
    void shouldParseCodeListWithCodes() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:cl-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cl-id</r:ID><r:Version>1</r:Version>
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
                </CodeList>
            </Fragment>
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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:variant-cat:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>variant-cat</r:ID><r:Version>1</r:Version>
                    <r:BasedOnObject>
                        <r:BasedOnReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>original-cat</r:ID><r:Version>3</r:Version>
                            <r:TypeOfObject>Category</r:TypeOfObject>
                        </r:BasedOnReference>
                    </r:BasedOnObject>
                </Category>
            </Fragment>
            """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.basedOnObject()).isNotNull();
        assertThat(cat.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cat");
        assertThat(cat.basedOnObject().basedOnReferences().get(0).version()).isEqualTo("3");
    }

    @Test
    void shouldParseCodeListBasedOnObject() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:variant-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>variant-id</r:ID><r:Version>1</r:Version>
                    <r:BasedOnObject>
                        <r:BasedOnReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>original-cl-id</r:ID><r:Version>2</r:Version>
                            <r:TypeOfObject>CodeList</r:TypeOfObject>
                        </r:BasedOnReference>
                    </r:BasedOnObject>
                </CodeList>
            </Fragment>
            """);

        Ddi4CodeList cl = converter.toCodeList(doc);

        assertThat(cl.basedOnObject()).isNotNull();
        assertThat(cl.basedOnObject().basedOnReferences()).hasSize(1);
        assertThat(cl.basedOnObject().basedOnReferences().get(0).id()).isEqualTo("original-cl-id");
        assertThat(cl.basedOnObject().basedOnReferences().get(0).version()).isEqualTo("2");
    }

    @Test
    void shouldParseCodeListLevels() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:cl-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cl-id</r:ID><r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">NUTS</r:Content></r:Label>
                    <Level levelNumber="0">
                        <LevelName><r:String xml:lang="fr-FR">NUTS 0</r:String></LevelName>
                        <CategoryRelationship>Nominal</CategoryRelationship>
                    </Level>
                    <Level levelNumber="1">
                        <LevelName><r:String xml:lang="fr-FR">NUTS 1</r:String></LevelName>
                        <CategoryRelationship>Ordinal</CategoryRelationship>
                    </Level>
                </CodeList>
            </Fragment>
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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:cl-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cl-id</r:ID><r:Version>1</r:Version>
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
                </CodeList>
            </Fragment>
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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeListScheme xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2026-04-03T12:00:00Z">
                    <r:URN>urn:ddi:fr.insee:cls-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cls-id</r:ID><r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">CodeListScheme Label</r:Content></r:Label>
                    <r:CodeListReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cl-1</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>CodeList</r:TypeOfObject>
                    </r:CodeListReference>
                    <r:CodeListReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>cl-2</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>CodeList</r:TypeOfObject>
                    </r:CodeListReference>
                </CodeListScheme>
            </Fragment>
            """);

        Ddi4CodeListScheme scheme = converter.toCodeListScheme(doc);

        assertThat(scheme.id()).isEqualTo("cls-id");
        assertThat(scheme.agency()).isEqualTo("fr.insee");
        assertThat(scheme.version()).isEqualTo("1");
        assertThat(scheme.label().get(0).value()).isEqualTo("CodeListScheme Label");
        assertThat(scheme.codeListReference())
                .extracting(fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference::id)
                .containsExactly("cl-1", "cl-2");
    }

    @Test
    void shouldParseManagedRepresentationSchemeWithMemberReferences() throws XmlException {
        // XML tel que renvoyé par Colectica GET item (le MRS est dans le namespace reusable).
        // Les membres peuvent être référencés par l'élément concret du type
        // (ManagedMissingValuesRepresentationReference…) ou, dans des données historiques, par la
        // tête générique ManagedRepresentationReference : les deux doivent être lus.
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <r:ManagedRepresentationScheme isUniversallyUnique="true" versionDate="2026-04-03T12:00:00Z">
                    <r:URN>urn:ddi:fr.insee:mrs-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>mrs-id</r:ID><r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">ManagedRepresentationScheme Label</r:Content></r:Label>
                    <r:ManagedMissingValuesRepresentationReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>mmvr-1</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>ManagedMissingValuesRepresentation</r:TypeOfObject>
                    </r:ManagedMissingValuesRepresentationReference>
                    <r:ManagedRepresentationReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>mr-legacy</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>ManagedTextRepresentation</r:TypeOfObject>
                    </r:ManagedRepresentationReference>
                </r:ManagedRepresentationScheme>
            </Fragment>
            """);

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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <r:ManagedMissingValuesRepresentation isUniversallyUnique="true" versionDate="2026-04-03T12:00:00Z">
                    <r:URN>urn:ddi:fr.insee:mmvr-1:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>mmvr-1</r:ID><r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">Valeurs sentinelles NSP/REF</r:Content></r:Label>
                    <r:MissingCodeRepresentation blankIsMissingValue="false">
                        <r:CodeListReference>
                            <r:Agency>fr.insee</r:Agency><r:ID>cl-sentinelles</r:ID><r:Version>1</r:Version>
                            <r:TypeOfObject>CodeList</r:TypeOfObject>
                        </r:CodeListReference>
                    </r:MissingCodeRepresentation>
                </r:ManagedMissingValuesRepresentation>
            </Fragment>
            """);

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
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:cat-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cat-id</r:ID><r:Version>1</r:Version>
                    <r:Label><r:Content xml:lang="fr-FR">Category Label</r:Content></r:Label>
                </Category>
            </Fragment>
            """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.id()).isEqualTo("cat-id");
        assertThat(cat.label().get(0).value()).isEqualTo("Category Label");
    }

    @Test
    void shouldParseCategoryWithLabelInEveryLanguage() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-12-23T09:52:06.355Z">
                    <r:URN>urn:ddi:fr.insee:cat-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>cat-id</r:ID><r:Version>1</r:Version>
                    <r:Label>
                        <r:Content xml:lang="en-IE">Growing of non-perennial crops</r:Content>
                        <r:Content xml:lang="fr-FR">Cultures non permanentes</r:Content>
                    </r:Label>
                </Category>
            </Fragment>
            """);

        Ddi4Category cat = converter.toCategory(doc);

        assertThat(cat.label()).hasSize(2);
        assertThat(cat.label())
                .extracting(LangString::language, LangString::value)
                .containsExactly(
                        tuple("en-IE", "Growing of non-perennial crops"),
                        tuple("fr-FR", "Cultures non permanentes"));
    }

    @Test
    void shouldParseGroupWithAllFields() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Group xmlns="ddi:group:3_3" isUniversallyUnique="true" versionDate="2026-04-03T12:00:00Z">
                    <r:URN>urn:ddi:fr.insee:group-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>group-id</r:ID><r:Version>1</r:Version>
                    <r:UserID typeOfUserID="URI">http://id.insee.fr/operations/serie/s1001</r:UserID>
                    <TypeOfGroup>insee:StatisticalOperationSeries</TypeOfGroup>
                    <r:Citation><r:Title><r:String xml:lang="fr-FR">Test Group</r:String></r:Title></r:Citation>
                    <r:StudyUnitReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>su-id-1</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>StudyUnit</r:TypeOfObject>
                    </r:StudyUnitReference>
                </Group>
            </Fragment>
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
    void shouldParseStudyUnitWithOperationIri() throws XmlException {
        FragmentDocument doc = FragmentDocument.Factory.parse("""
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <StudyUnit xmlns="ddi:studyunit:3_3" isUniversallyUnique="true" versionDate="2026-04-03T12:00:00Z">
                    <r:URN>urn:ddi:fr.insee:su-id:1</r:URN>
                    <r:Agency>fr.insee</r:Agency><r:ID>su-id</r:ID><r:Version>1</r:Version>
                    <r:UserID typeOfUserID="URI">http://id.insee.fr/operations/operation/op1</r:UserID>
                    <r:Citation><r:Title><r:String xml:lang="fr-FR">Test SU</r:String></r:Title></r:Citation>
                    <r:PhysicalInstanceReference>
                        <r:Agency>fr.insee</r:Agency><r:ID>pi-id</r:ID><r:Version>1</r:Version>
                        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
                    </r:PhysicalInstanceReference>
                </StudyUnit>
            </Fragment>
            """);

        Ddi4StudyUnit su = converter.toStudyUnit(doc);

        assertThat(su.id()).isEqualTo("su-id");
        assertThat(su.operationIri()).isEqualTo("http://id.insee.fr/operations/operation/op1");
        assertThat(su.citation().title().get(0).value()).isEqualTo("Test SU");
        assertThat(su.physicalInstanceReferences()).hasSize(1);
        assertThat(su.physicalInstanceReferences().get(0).id()).isEqualTo("pi-id");
    }
}
