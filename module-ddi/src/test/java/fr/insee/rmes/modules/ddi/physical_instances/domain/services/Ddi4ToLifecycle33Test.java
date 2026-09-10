package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DateTimeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Level;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumberRange;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RangeValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import org.apache.xmlbeans.XmlOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Ddi4ToLifecycle33Test {

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";

    private final Ddi4ToLifecycle33 converter = new Ddi4ToLifecycle33();

    @Test
    void shouldBuildPhysicalInstanceWithoutBasedOnObject() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
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
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:new-pi-id:1",
                "fr.insee",
                "new-pi-id",
                "1",
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
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                null,
                null,
                null);

        String xml = converter.toDataRelationship(dr).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:DataRelationship")
                .contains(">urn:ddi:fr.insee:dr-id:1<")
                .doesNotContain("BasedOnObject")
                .doesNotContain("DataRelationshipName");
    }

    @Test
    void shouldBuildDataRelationshipWithLabelAndLogicalRecord() {
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:dr-id:1",
                "fr.insee",
                "dr-id",
                "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-dr", "1", "DataRelationship"))),
                LangStrings.of("fr-FR", "DR Label"),
                List.of(new LogicalRecord(
                        LogicalRecord.TYPE,
                        "urn:ddi:fr.insee:lr-id:1",
                        "fr.insee",
                        "lr-id",
                        "1",
                        LangStrings.of("fr-FR", "LR Label"),
                        null)));

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
        Ddi4Variable variable = new Ddi4Variable(
                Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:var-id:1",
                "fr.insee",
                "var-id",
                "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-var", "1", "Variable"))),
                LangStrings.of("fr-FR", "TEST_VAR"),
                LangStrings.of("fr-FR", "Test Variable"),
                null,
                null,
                null);

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:Variable")
                .contains(">original-var<")
                .contains("<ddi:VariableName")
                .contains(">TEST_VAR<")
                .contains("<r:Label")
                .contains(">Test Variable<")
                // #1592 : la VariableRepresentation n'est plus vide, elle porte le repli Text.
                .contains("<ddi:VariableRepresentation><r:TextRepresentation");
    }

    @Test
    void shouldBuildVariableWithCodeRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null,
                new CodeRepresentation(
                        CodeRepresentation.TYPE, true, Reference.of("fr.insee", "cl-id", "1", "CodeList")),
                null,
                null,
                null,
                null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:CodeRepresentation")
                .contains("blankIsMissingValue=\"true\"")
                .contains("<r:CodeListReference")
                .contains(">cl-id<");
    }

    /**
     * Valeurs sentinelles (#1566) : la référence MMVR est portée par le wrapper
     * {@code VariableRepresentation} — élément {@code MissingValuesReference} du namespace
     * <b>logicalproduct</b> (élément local du XSD, pas {@code r:}) — quel que soit le type de
     * représentation.
     */
    @Test
    void shouldWriteMissingValuesReferenceOnVariableRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null,
                new CodeRepresentation(
                        CodeRepresentation.TYPE, false, Reference.of("fr.insee", "cl-id", "1", "CodeList")),
                null,
                null,
                null,
                Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation")));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:MissingValuesReference")
                .contains(">mmvr-1<")
                .contains(">ManagedMissingValuesRepresentation<");
    }

    @Test
    void shouldBuildVariableWithNumericRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null,
                null,
                new NumericRepresentation(
                        NumericRepresentation.TYPE,
                        "Integer",
                        new NumberRange(new RangeValue(false, 0.0), new RangeValue(true, 100.0))),
                null,
                null,
                null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:NumericRepresentation")
                .contains("<r:NumberRange")
                .contains("<r:Low isInclusive=\"false\">0</r:Low>")
                .contains("<r:High isInclusive=\"true\">100</r:High>")
                .contains("<r:NumericTypeCode>Integer</r:NumericTypeCode>");
    }

    @Test
    void shouldBuildVariableWithDecimalNumericBounds() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null,
                null,
                new NumericRepresentation(
                        NumericRepresentation.TYPE,
                        "Decimal",
                        new NumberRange(new RangeValue(true, 0.0001), new RangeValue(true, 12345678.5))),
                null,
                null,
                null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:Low isInclusive=\"true\">0.0001</r:Low>")
                .contains("<r:High isInclusive=\"true\">12345678.5</r:High>");
    }

    @Test
    void shouldBuildVariableWithDateTimeRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null,
                null,
                null,
                new DateTimeRepresentation(DateTimeRepresentation.TYPE, "Date", "yyyy-MM-dd"),
                null,
                null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:DateTimeRepresentation")
                .contains("<r:DateTypeCode>Date</r:DateTypeCode>")
                .contains("<r:DateFieldFormat>yyyy-MM-dd</r:DateFieldFormat>");
    }

    @Test
    void shouldBuildVariableWithTextRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null, null, null, null, new TextRepresentation(TextRepresentation.TYPE, 255, 1, "[A-Z]+", true), null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:TextRepresentation")
                .contains("maxLength=\"255\"")
                .contains("minLength=\"1\"")
                .contains("regExp=\"[A-Z]+\"")
                .contains("blankIsMissingValue=\"true\"");
    }

    /**
     * #1592 : une variable Text sans longueur ni expression reguliere doit conserver
     * l'element {@code <r:TextRepresentation/>} ; sans lui, le type Text est perdu a l'export.
     */
    @Test
    void shouldBuildTextRepresentationWithoutAnyAttribute() {
        Ddi4Variable variable = variableWithRepresentation(new VariableRepresentation(
                null, null, null, null, new TextRepresentation(TextRepresentation.TYPE, null, null, null, null), null));

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml).contains("<r:TextRepresentation");
    }

    /**
     * #1592 : les variables enregistrees avant le correctif n'ont aucune representation dans le
     * DDI4 stocke. A l'export, elles doivent retomber sur TextRepresentation (comme le fait
     * l'affichage), plutot que de produire un {@code <ddi:VariableRepresentation/>} vide.
     */
    @Test
    void shouldFallBackToTextRepresentationWhenNoValueRepresentation() {
        Ddi4Variable variable = variableWithRepresentation(null);

        String xml = converter.toVariable(variable).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml).contains("<r:TextRepresentation");
    }

    @Test
    void shouldBuildCodeListWithCodes() {
        Ddi4CodeList cl = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cl-id:1",
                "fr.insee",
                "cl-id",
                "1",
                LangStrings.of("fr-FR", "CodeList Label"),
                null,
                List.of(new Code(
                        Code.TYPE,
                        "urn:ddi:fr.insee:code-id:1",
                        "fr.insee",
                        "code-id",
                        "1",
                        Reference.of("fr.insee", "cat-id", "1", "Category"),
                        ValueType.of("01"),
                        null)));

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
    void shouldBuildCategoryWithBasedOnObject() {
        // Variante d'une categorie partagee : l'attribut DDI BasedOn reference la categorie source.
        Ddi4Category cat = new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:variant-cat:1",
                "fr.insee",
                "variant-cat",
                "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-cat", "3", "Category"))),
                LangStrings.of("fr-FR", "Europe variante"));

        String xml = converter.toCategory(cat).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:Category")
                .contains("<r:BasedOnObject")
                .contains(">original-cat<")
                .contains(">Category</r:TypeOfObject>");
    }

    @Test
    void shouldBuildCodeListWithBasedOnObject() {
        // Variante d'une liste partagée : l'attribut DDI BasedOn référence la liste d'origine.
        Ddi4CodeList cl = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:variant-id:1",
                "fr.insee",
                "variant-id",
                "1",
                BasedOnObject.of(List.of(Reference.of("fr.insee", "original-cl-id", "2", "CodeList"))),
                LangStrings.of("fr-FR", "Variante"),
                null,
                null);

        String xml = converter.toCodeList(cl).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:CodeList")
                .contains("<r:BasedOnObject")
                .contains(">original-cl-id<")
                .contains(">CodeList</r:TypeOfObject>");
    }

    /**
     * L'URN est une fonction de l'identité DDI ({@code urn:ddi:agence:id:version}) : c'est au back
     * de la produire. Le front qui crée une liste (variante d'une liste partagée, par exemple)
     * envoie donc l'identité seule, sans réimplémenter la règle de nommage de son côté.
     */
    @Test
    void shouldSynthesizeCodeListUrnWhenAbsent() {
        Ddi4CodeList cl = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                null,
                "fr.insee",
                "variant-id",
                "1",
                LangStrings.of("fr-FR", "Variante"),
                null,
                List.of(new Code(
                        Code.TYPE,
                        null,
                        "fr.insee",
                        "code-id",
                        "1",
                        Reference.of("fr.insee", "cat-id", "1", "Category"),
                        ValueType.of("01"),
                        null)));

        String xml = converter.toCodeList(cl).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml).contains(">urn:ddi:fr.insee:variant-id:1<").contains(">urn:ddi:fr.insee:code-id:1<");
    }

    @Test
    void shouldSynthesizeCategoryUrnWhenAbsent() {
        Ddi4Category cat = new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                null,
                "fr.insee",
                "variant-cat",
                "1",
                LangStrings.of("fr-FR", "Europe variante"));

        String xml = converter.toCategory(cat).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml).contains(">urn:ddi:fr.insee:variant-cat:1<");
    }

    @Test
    void shouldBuildCodeListWithLevels() {
        Ddi4CodeList cl = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cl-id:1",
                "fr.insee",
                "cl-id",
                "1",
                LangStrings.of("fr-FR", "NUTS"),
                List.of(
                        new Level(Level.TYPE, 0, LangStrings.of("fr-FR", "NUTS 0"), "Nominal"),
                        new Level(Level.TYPE, 1, LangStrings.of("fr-FR", "NUTS 1"), "Ordinal")),
                null);

        var codeList = converter.toCodeList(cl).getFragment().getCodeList();

        Assertions.assertThat(codeList.getLevelArray()).hasSize(2);
        var level0 = codeList.getLevelArray(0);
        Assertions.assertThat(level0.getLevelNumber()).isEqualTo(BigInteger.ZERO);
        Assertions.assertThat(level0.getLevelNameArray(0).getStringArray(0).getStringValue())
                .isEqualTo("NUTS 0");
        Assertions.assertThat(level0.getLevelNameArray(0).getStringArray(0).getLang())
                .isEqualTo("fr-FR");
        Assertions.assertThat(level0.getCategoryRelationship().toString()).isEqualTo("Nominal");
        var level1 = codeList.getLevelArray(1);
        Assertions.assertThat(level1.getLevelNumber()).isEqualTo(BigInteger.ONE);
        Assertions.assertThat(level1.getCategoryRelationship().toString()).isEqualTo("Ordinal");
    }

    @Test
    void shouldBuildHierarchicalCodeListWithNestedCodes() {
        Ddi4CodeList cl = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cl-id:1",
                "fr.insee",
                "cl-id",
                "1",
                LangStrings.of("fr-FR", "NUTS"),
                null,
                List.of(new Code(
                        Code.TYPE,
                        "urn:ddi:fr.insee:code-at:1",
                        "fr.insee",
                        "code-at",
                        "1",
                        Reference.of("fr.insee", "cat-at", "1", "Category"),
                        ValueType.of("AT"),
                        List.of(new Code(
                                Code.TYPE,
                                "urn:ddi:fr.insee:code-at2:1",
                                "fr.insee",
                                "code-at2",
                                "1",
                                Reference.of("fr.insee", "cat-at2", "1", "Category"),
                                ValueType.of("AT2"),
                                null)))));

        var codeList = converter.toCodeList(cl).getFragment().getCodeList();

        Assertions.assertThat(codeList.getCodeArray()).hasSize(1);
        var at = codeList.getCodeArray(0);
        Assertions.assertThat(at.getIDArray(0).getStringValue()).isEqualTo("code-at");
        Assertions.assertThat(at.getCodeArray()).hasSize(1);
        var at2 = at.getCodeArray(0);
        Assertions.assertThat(at2.getIDArray(0).getStringValue()).isEqualTo("code-at2");
        Assertions.assertThat(at2.getValue().getStringValue()).isEqualTo("AT2");
        Assertions.assertThat(at2.getCategoryReference().getIDArray(0).getStringValue())
                .isEqualTo("cat-at2");
        Assertions.assertThat(at2.getCodeArray()).isEmpty();
    }

    @Test
    void shouldBuildCodeListSchemeWithCodeListReferences() {
        Ddi4CodeListScheme scheme = new Ddi4CodeListScheme(
                Ddi4CodeListScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:cls-id:1",
                "fr.insee",
                "cls-id",
                "1",
                LangStrings.of("fr-FR", "CodeListScheme Label"),
                List.of(
                        Reference.of("fr.insee", "cl-1", "1", "CodeList"),
                        Reference.of("fr.insee", "cl-2", "1", "CodeList")));

        String xml = converter.toCodeListScheme(scheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:CodeListScheme")
                .contains(">urn:ddi:fr.insee:cls-id:1<")
                .contains("<r:Label")
                .contains(">CodeListScheme Label<")
                .contains("<r:CodeListReference")
                .contains(">cl-1<")
                .contains(">cl-2<");
    }

    @Test
    void shouldBuildLogicalProductWithCodeListSchemeReferences() {
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:lp-id:1",
                "fr.insee",
                "lp-id",
                "1",
                LangStrings.of("fr-FR", "LogicalProduct Label"),
                List.of(Reference.of("fr.insee", "cls-1", "1", "CodeListScheme")));

        String xml = converter.toLogicalProduct(logicalProduct).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:LogicalProduct")
                .contains(">urn:ddi:fr.insee:lp-id:1<")
                .contains("<r:Label")
                .contains(">LogicalProduct Label<")
                .contains("<r:CodeListSchemeReference")
                .contains(">cls-1<");
    }

    @Test
    void shouldBuildCategorySchemeWithCategoryReferences() {
        Ddi4CategoryScheme scheme = new Ddi4CategoryScheme(
                Ddi4CategoryScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:cats-id:1",
                "fr.insee",
                "cats-id",
                "1",
                LangStrings.of("fr-FR", "CategoryScheme Label"),
                List.of(
                        Reference.of("fr.insee", "cat-1", "1", "Category"),
                        Reference.of("fr.insee", "cat-2", "1", "Category")));

        String xml = converter.toCategoryScheme(scheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:CategoryScheme")
                .contains(">urn:ddi:fr.insee:cats-id:1<")
                .contains("<r:Label")
                .contains(">CategoryScheme Label<")
                .contains("<r:CategoryReference")
                .contains(">cat-1<")
                .contains(">cat-2<");
    }

    @Test
    void shouldBuildVariableSchemeWithVariableReferences() {
        Ddi4VariableScheme scheme = new Ddi4VariableScheme(
                Ddi4VariableScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:vars-id:1",
                "fr.insee",
                "vars-id",
                "1",
                LangStrings.of("fr-FR", "VariableScheme Label"),
                List.of(
                        Reference.of("fr.insee", "var-1", "1", "Variable"),
                        Reference.of("fr.insee", "var-2", "1", "Variable")));

        String xml = converter.toVariableScheme(scheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<ddi:VariableScheme")
                .contains(">urn:ddi:fr.insee:vars-id:1<")
                .contains("<r:Label")
                .contains(">VariableScheme Label<")
                .contains("<r:VariableReference")
                .contains(">var-1<")
                .contains(">var-2<");
    }

    @Test
    void shouldBuildEmptyCategoryAndVariableSchemes() {
        Ddi4CategoryScheme categoryScheme = new Ddi4CategoryScheme(
                Ddi4CategoryScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:cats-id:1",
                "fr.insee",
                "cats-id",
                "1",
                LangStrings.of("fr-FR", "Empty CategoryScheme"),
                List.of());
        Ddi4VariableScheme variableScheme = new Ddi4VariableScheme(
                Ddi4VariableScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:vars-id:1",
                "fr.insee",
                "vars-id",
                "1",
                LangStrings.of("fr-FR", "Empty VariableScheme"),
                List.of());

        String categoryXml = converter.toCategoryScheme(categoryScheme).xmlText(logicalProductXmlOptions());
        String variableXml = converter.toVariableScheme(variableScheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(categoryXml).contains("<ddi:CategoryScheme").doesNotContain("CategoryReference");
        Assertions.assertThat(variableXml).contains("<ddi:VariableScheme").doesNotContain("VariableReference");
    }

    @Test
    void shouldBuildManagedRepresentationSchemeWithTypeSpecificMemberReferences() {
        // L'élément générique ManagedRepresentationReference est une tête de groupe de substitution
        // abstraite : Colectica ignore la référence (ni indexée, ni affichée) si elle n'est pas
        // écrite avec l'élément concret correspondant au type visé.
        Ddi4ManagedRepresentationScheme scheme = new Ddi4ManagedRepresentationScheme(
                Ddi4ManagedRepresentationScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:mrs-id:1",
                "fr.insee",
                "mrs-id",
                "1",
                LangStrings.of("fr-FR", "ManagedRepresentationScheme Label"),
                List.of(
                        Reference.of("fr.insee", "mr-1", "1", "ManagedTextRepresentation"),
                        Reference.of("fr.insee", "mr-2", "1", "ManagedNumericRepresentation"),
                        Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation")));

        String xml = converter.toManagedRepresentationScheme(scheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:ManagedRepresentationScheme")
                .contains(">urn:ddi:fr.insee:mrs-id:1<")
                .contains("<r:Label")
                .contains(">ManagedRepresentationScheme Label<")
                .contains("<r:ManagedTextRepresentationReference")
                .contains("<r:ManagedNumericRepresentationReference")
                .contains("<r:ManagedMissingValuesRepresentationReference")
                .doesNotContain("<r:ManagedRepresentationReference>")
                .contains(">mr-1<")
                .contains(">mr-2<")
                .contains(">mmvr-1<");
    }

    @Test
    void shouldBuildManagedMissingValuesRepresentationWithMissingCodeRepresentation() {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:mmvr-id:1",
                "fr.insee",
                "mmvr-id",
                "1",
                LangStrings.of("fr-FR", "Valeurs manquantes standard"),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE,
                        Boolean.FALSE,
                        Reference.of("fr.insee", "cl-sentinel", "1", "CodeList"))));

        String xml = converter.toManagedMissingValuesRepresentation(mmvr).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:ManagedMissingValuesRepresentation")
                .contains(">urn:ddi:fr.insee:mmvr-id:1<")
                .contains("<r:Label")
                .contains(">Valeurs manquantes standard<")
                .contains("<r:MissingCodeRepresentation")
                .contains("<r:CodeListReference")
                .contains(">cl-sentinel<");
    }

    @Test
    void shouldBuildEmptyManagedRepresentationScheme() {
        Ddi4ManagedRepresentationScheme scheme = new Ddi4ManagedRepresentationScheme(
                Ddi4ManagedRepresentationScheme.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:mrs-id:1",
                "fr.insee",
                "mrs-id",
                "1",
                LangStrings.of("fr-FR", "Empty ManagedRepresentationScheme"),
                List.of());

        String xml = converter.toManagedRepresentationScheme(scheme).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:ManagedRepresentationScheme")
                .doesNotContain("ManagedRepresentationReference");
    }

    @Test
    void shouldBuildLogicalProductWithCategoryVariableAndManagedRepresentationSchemeReferences() {
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:lp-id:1",
                "fr.insee",
                "lp-id",
                "1",
                LangStrings.of("fr-FR", "LogicalProduct Label"),
                List.of(Reference.of("fr.insee", "cls-1", "1", "CodeListScheme")),
                List.of(Reference.of("fr.insee", "cats-1", "1", "CategoryScheme")),
                List.of(Reference.of("fr.insee", "vars-1", "1", "VariableScheme")),
                List.of(Reference.of("fr.insee", "mrs-1", "1", "ManagedRepresentationScheme")));

        String xml = converter.toLogicalProduct(logicalProduct).xmlText(logicalProductXmlOptions());

        Assertions.assertThat(xml)
                .contains("<r:CodeListSchemeReference")
                .contains(">cls-1<")
                .contains("<r:CategorySchemeReference")
                .contains(">cats-1<")
                .contains("<r:VariableSchemeReference")
                .contains(">vars-1<")
                .contains("<r:ManagedRepresentationSchemeReference")
                .contains(">mrs-1<");
    }

    @Test
    void shouldBuildGroupWithLogicalProductReference() {
        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1",
                "fr.insee",
                "group-id",
                "1",
                "bauhaus",
                new Citation(LangStrings.of("fr-FR", "Test Group")),
                List.of(Reference.of("fr.insee", "su-id-1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries",
                List.of(Reference.of("fr.insee", "lp-id-1", "1", "LogicalProduct")));

        String xml = converter.toGroup(group).xmlText(groupXmlOptions());

        Assertions.assertThat(xml).contains("<r:LogicalProductReference").contains(">lp-id-1<");
    }

    @Test
    void shouldBuildCategory() {
        Ddi4Category cat = new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:cat-id:1",
                "fr.insee",
                "cat-id",
                "1",
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
        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1",
                "fr.insee",
                "group-id",
                "1",
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
        Ddi4Group group = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:group-id:1",
                "fr.insee",
                "group-id",
                "1",
                "bauhaus",
                new Citation(LangStrings.of("fr-FR", "Test")),
                List.of(),
                null,
                null);

        String xml = converter.toGroup(group).xmlText(groupXmlOptions());

        Assertions.assertThat(xml)
                .contains(">Test<")
                .doesNotContain("typeOfUserID")
                .doesNotContain("TypeOfGroup");
    }

    @Test
    void shouldBuildStudyUnitWithOperationIri() {
        Ddi4StudyUnit su = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1",
                "fr.insee",
                "su-id",
                "1",
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
    void shouldBuildStudyUnitWithLogicalProductReference() {
        Ddi4StudyUnit su = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1",
                "fr.insee",
                "su-id",
                "1",
                new Citation(LangStrings.of("fr-FR", "Test SU")),
                "http://id.insee.fr/operations/operation/op1",
                List.of(Reference.of("fr.insee", "pi-id", "1", "PhysicalInstance")),
                List.of(Reference.of("fr.insee", "lp-id", "1", "LogicalProduct")));

        String xml = converter.toStudyUnit(su).xmlText(studyUnitXmlOptions());

        Assertions.assertThat(xml).contains("<r:LogicalProductReference").contains(">lp-id<");
    }

    @Test
    void shouldBuildStudyUnitWithoutOperationIri() {
        Ddi4StudyUnit su = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:su-id:1",
                "fr.insee",
                "su-id",
                "1",
                new Citation(LangStrings.of("fr-FR", "Test SU")),
                null,
                null);

        String xml = converter.toStudyUnit(su).xmlText(studyUnitXmlOptions());

        Assertions.assertThat(xml).contains(">Test SU<").doesNotContain("typeOfUserID");
    }

    private static Ddi4Variable variableWithRepresentation(VariableRepresentation rep) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:var-rep:1",
                "fr.insee",
                "var-rep",
                "1",
                null,
                LangStrings.of("fr-FR", "VAR_REP"),
                LangStrings.of("fr-FR", "Variable avec representation"),
                null,
                rep,
                null);
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
