package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(cl.code().get(0).value()).isEqualTo("01");
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
