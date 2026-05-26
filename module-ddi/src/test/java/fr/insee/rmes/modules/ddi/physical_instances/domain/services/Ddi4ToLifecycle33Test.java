package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DateTimeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumberRange;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RangeValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import org.apache.xmlbeans.XmlOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Ddi4ToLifecycle33Test {

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";

    private final Ddi4ToLifecycle33 converter = new Ddi4ToLifecycle33();

    @Test
    void shouldBuildPhysicalInstanceWithoutBasedOnObject() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:new-pi-id:1", "fr.insee", "new-pi-id", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Test Instance")),
                null);

        String xml = converter.toPhysicalInstance(pi).xmlText(physicalInstanceXmlOptions());

        assertEquals(
                "<Fragment xmlns=\"ddi:instance:3_3\">"
                        + "<ddi:PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-12-23T09:52:06.355Z\" xmlns:ddi=\"ddi:physicalinstance:3_3\">"
                        + "<r:URN xmlns:r=\"ddi:reusable:3_3\">urn:ddi:fr.insee:new-pi-id:1</r:URN>"
                        + "<r:Agency xmlns:r=\"ddi:reusable:3_3\">fr.insee</r:Agency>"
                        + "<r:ID xmlns:r=\"ddi:reusable:3_3\">new-pi-id</r:ID>"
                        + "<r:Version xmlns:r=\"ddi:reusable:3_3\">1</r:Version>"
                        + "<r:Citation xmlns:r=\"ddi:reusable:3_3\"><r:Title><r:String xml:lang=\"fr-FR\">Test Instance</r:String></r:Title></r:Citation>"
                        + "</ddi:PhysicalInstance></Fragment>",
                xml);
    }

    @Test
    void shouldBuildPhysicalInstanceWithBasedOnObjectAndDataRelationshipReference() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:new-pi-id:1", "fr.insee", "new-pi-id", "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-pi-id", "1", "PhysicalInstance"))),
                new Citation(LangStrings.of("fr-FR", "Test Instance")),
                List.of(Reference.of("fr.insee", "dr-id", "1", "DataRelationship")));

        String xml = converter.toPhysicalInstance(pi).xmlText(physicalInstanceXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:PhysicalInstance")
                .contains("<r:BasedOnObject")
                .contains(">original-pi-id<")
                .contains("<r:DataRelationshipReference")
                .contains(">dr-id<");
    }

    @Test
    void shouldBuildDataRelationshipMinimal() {
        Ddi4DataRelationship dr = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:dr-id:1", "fr.insee", "dr-id", "1",
                null, null, null);

        String xml = converter.toDataRelationship(dr).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:DataRelationship")
                .contains(">urn:ddi:fr.insee:dr-id:1<")
                .doesNotContain("BasedOnObject")
                .doesNotContain("DataRelationshipName");
    }

    @Test
    void shouldBuildDataRelationshipWithLabelAndLogicalRecord() {
        Ddi4DataRelationship dr = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:dr-id:1", "fr.insee", "dr-id", "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-dr", "1", "DataRelationship"))),
                LangStrings.of("fr-FR", "DR Label"),
                new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr-id:1", "fr.insee", "lr-id", "1",
                        LangStrings.of("fr-FR", "LR Label"), null));

        String xml = converter.toDataRelationship(dr).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:BasedOnObject")
                .contains(">original-dr<")
                .contains("<ddi:DataRelationshipName")
                .contains(">DR Label<")
                .contains("<ddi:LogicalRecord")
                .contains(">urn:ddi:fr.insee:lr-id:1<")
                .contains(">LR Label<");
    }

    @Test
    void shouldBuildVariableWithBasedOnObjectAndLabel() {
        Ddi4Variable var = new Ddi4Variable(Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:var-id:1", "fr.insee", "var-id", "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-var", "1", "Variable"))),
                LangStrings.of("fr-FR", "TEST_VAR"),
                LangStrings.of("fr-FR", "Test Variable"),
                null, null, null);

        String xml = converter.toVariable(var).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:Variable")
                .contains(">original-var<")
                .contains("<ddi:VariableName")
                .contains(">TEST_VAR<")
                .contains("<r:Label")
                .contains(">Test Variable<")
                .contains("<ddi:VariableRepresentation/>");
    }

    @Test
    void shouldBuildVariableWithCodeRepresentation() {
        Ddi4Variable var = variableWithRepresentation(new VariableRepresentation(
                null,
                new CodeRepresentation("true",
                        Reference.of("fr.insee", "cl-id", "1", "CodeList")),
                null, null, null));

        String xml = converter.toVariable(var).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:CodeRepresentation")
                .contains("blankIsMissingValue=\"true\"")
                .contains("<r:CodeListReference")
                .contains(">cl-id<");
    }

    @Test
    void shouldBuildVariableWithNumericRepresentation() {
        Ddi4Variable var = variableWithRepresentation(new VariableRepresentation(
                null, null,
                new NumericRepresentation("Integer",
                        new NumberRange(new RangeValue("false", "0"), new RangeValue("true", "100"))),
                null, null));

        String xml = converter.toVariable(var).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:NumericRepresentation")
                .contains("<r:NumberRange")
                .contains("<r:Low isInclusive=\"false\">0</r:Low>")
                .contains("<r:High isInclusive=\"true\">100</r:High>")
                .contains("<r:NumericTypeCode>Integer</r:NumericTypeCode>");
    }

    @Test
    void shouldBuildVariableWithDateTimeRepresentation() {
        Ddi4Variable var = variableWithRepresentation(new VariableRepresentation(
                null, null, null,
                new DateTimeRepresentation("Date", "yyyy-MM-dd"),
                null));

        String xml = converter.toVariable(var).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:DateTimeRepresentation")
                .contains("<r:DateTypeCode>Date</r:DateTypeCode>")
                .contains("<r:DateFieldFormat>yyyy-MM-dd</r:DateFieldFormat>");
    }

    @Test
    void shouldBuildVariableWithTextRepresentation() {
        Ddi4Variable var = variableWithRepresentation(new VariableRepresentation(
                null, null, null, null,
                new TextRepresentation(255, 1, "[A-Z]+", "true")));

        String xml = converter.toVariable(var).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:TextRepresentation")
                .contains("maxLength=\"255\"")
                .contains("minLength=\"1\"")
                .contains("regExp=\"[A-Z]+\"")
                .contains("blankIsMissingValue=\"true\"");
    }

    @Test
    void shouldBuildCodeListWithCodes() {
        Ddi4CodeList cl = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cl-id:1", "fr.insee", "cl-id", "1",
                LangStrings.of("fr-FR", "CodeList Label"),
                List.of(new Code(Code.TYPE,
                        "urn:ddi:fr.insee:code-id:1", "fr.insee", "code-id", "1",
                        Reference.of("fr.insee", "cat-id", "1", "Category"),
                        "01")));

        String xml = converter.toCodeList(cl).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:CodeList")
                .contains("<r:Label")
                .contains(">CodeList Label<")
                .contains("<ddi:Code")
                .contains(">code-id<")
                .contains("<r:CategoryReference")
                .contains(">cat-id<")
                .contains(">01</r:Value>");
    }

    @Test
    void shouldBuildCategory() {
        Ddi4Category cat = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cat-id:1", "fr.insee", "cat-id", "1",
                LangStrings.of("fr-FR", "Category Label"));

        String xml = converter.toCategory(cat).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:Category")
                .contains("isMissing=\"false\"")
                .contains("<r:Label")
                .contains(">Category Label<");
    }

    @Test
    void shouldBuildGroupWithAllFields() {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
                List.of(Reference.of("fr.insee", "su-id-1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries");

        String xml = converter.toGroup(group).xmlText(groupXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:Group")
                .contains("typeOfUserID=\"URI\"")
                .contains(">http://id.insee.fr/operations/serie/s1001<")
                .contains(">insee:StatisticalOperationSeries<")
                .contains(">Test Group<")
                .contains(">su-id-1<");
    }

    @Test
    void shouldBuildGroupWithoutOptionalFields() {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1", "fr.insee", "group-id", "1",
                "bauhaus",
                new Citation(LangStrings.of("fr-FR", "Test")),
                List.of(), null, null);

        String xml = converter.toGroup(group).xmlText(groupXmlOptions());

        Assertions.assertThat(xml)
                .contains(">Test<")
                .doesNotContain("typeOfUserID")
                .doesNotContain("TypeOfGroup");
    }

    @Test
    void shouldBuildStudyUnitWithOperationIri() {
        Ddi4StudyUnit su = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(LangStrings.of("fr-FR", "Test SU")),
                "http://id.insee.fr/operations/operation/op1",
                List.of(Reference.of("fr.insee", "pi-id", "1", "PhysicalInstance")));

        String xml = converter.toStudyUnit(su).xmlText(studyUnitXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:StudyUnit")
                .contains("typeOfUserID=\"URI\"")
                .contains(">http://id.insee.fr/operations/operation/op1<")
                .contains(">Test SU<")
                .contains("<r:PhysicalInstanceReference")
                .contains(">pi-id<");
    }

    @Test
    void shouldBuildStudyUnitWithoutOperationIri() {
        Ddi4StudyUnit su = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1", "fr.insee", "su-id", "1",
                new Citation(LangStrings.of("fr-FR", "Test SU")),
                null, null);

        String xml = converter.toStudyUnit(su).xmlText(studyUnitXmlOptions());

        Assertions.assertThat(xml)
                .contains(">Test SU<")
                .doesNotContain("typeOfUserID");
    }

    private static Ddi4Variable variableWithRepresentation(VariableRepresentation rep) {
        return new Ddi4Variable(Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:var-rep:1", "fr.insee", "var-rep", "1",
                null,
                LangStrings.of("fr-FR", "VAR_REP"),
                LangStrings.of("fr-FR", "Variable avec representation"),
                null, rep, null);
    }

    private static XmlOptions physicalInstanceXmlOptions() {
        return options(DDI_PHYSICAL_INSTANCE_NS);
    }

    private static XmlOptions logicalProductXmlOptions() {
        return options("ddi:logicalproduct:3_3");
    }

    private static XmlOptions groupXmlOptions() {
        return options("ddi:group:3_3");
    }

    private static XmlOptions studyUnitXmlOptions() {
        return options("ddi:studyunit:3_3");
    }

    private static XmlOptions options(String contentNs) {
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "");
        prefixes.put(contentNs, "");
        prefixes.put(DDI_REUSABLE_NS, "r");
        XmlOptions options = new XmlOptions();
        options.setSaveSuggestedPrefixes(prefixes);
        return options;
    }
}
