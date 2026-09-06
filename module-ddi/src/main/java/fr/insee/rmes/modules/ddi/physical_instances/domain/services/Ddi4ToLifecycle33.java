package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeType;
import fr.insee.ddi.lifecycle33.logicalproduct.LogicalProductType;
import fr.insee.ddi.lifecycle33.reusable.BasedOnObjectType;
import fr.insee.ddi.lifecycle33.reusable.CategoryRelationCodeType;
import fr.insee.ddi.lifecycle33.reusable.CodeRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.ContentType;
import fr.insee.ddi.lifecycle33.reusable.DateTimeRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.NumberRangeType;
import fr.insee.ddi.lifecycle33.reusable.NumberRangeValueType;
import fr.insee.ddi.lifecycle33.reusable.NumericRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.ReferenceType;
import fr.insee.ddi.lifecycle33.reusable.RepresentationType;
import fr.insee.ddi.lifecycle33.reusable.TextRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.TypeOfObjectType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
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
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Level;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RangeValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class Ddi4ToLifecycle33 {

    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";

    /** #1592 : représentation Text sans aucun attribut, utilisée comme repli à l'export. */
    private static final TextRepresentation EMPTY_TEXT_REPRESENTATION =
            new TextRepresentation(TextRepresentation.TYPE, null, null, null, null);

    public FragmentDocument toPhysicalInstance(Ddi4PhysicalInstance pi) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var piType = doc.addNewFragment().addNewPhysicalInstance();

        piType.setIsUniversallyUnique(true);
        piType.setVersionDate(pi.versionDate() != null ? pi.versionDate().dateTime() : null);
        piType.addNewURN().setStringValue(pi.urn());
        piType.addAgency(pi.agency());
        piType.addNewID().setStringValue(pi.id());
        piType.addVersion(pi.version());

        if (pi.basedOnObject() != null) {
            populateBasedOnObject(piType.addNewBasedOnObject(), pi.basedOnObject());
        }

        Citation citation = pi.citation();
        if (citation != null && citation.title() != null) {
            LangString first = citation.title().get(0);
            var titleString = piType.addNewCitation().addNewTitle().addNewString();
            titleString.setLang(first.language());
            titleString.setStringValue(first.value());
        }

        if (pi.dataRelationshipReference() != null) {
            for (Reference ref : pi.dataRelationshipReference()) {
                populateReference(piType.addNewDataRelationshipReference(), ref);
            }
        }

        return doc;
    }

    public FragmentDocument toDataRelationship(Ddi4DataRelationship dr) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var drType = doc.addNewFragment().addNewDataRelationship();

        drType.setIsUniversallyUnique(true);
        drType.setVersionDate(dr.versionDate() != null ? dr.versionDate().dateTime() : null);
        drType.addNewURN().setStringValue(dr.urn());
        drType.addAgency(dr.agency());
        drType.addNewID().setStringValue(dr.id());
        drType.addVersion(dr.version());

        if (dr.basedOnObject() != null) {
            populateBasedOnObject(drType.addNewBasedOnObject(), dr.basedOnObject());
        }

        if (dr.label() != null && !dr.label().isEmpty()) {
            LangString first = dr.label().get(0);
            var nameString = drType.addNewDataRelationshipName().addNewString();
            nameString.setLang(first.language());
            nameString.setStringValue(first.value());
            writeLabelContent(drType.addNewLabel().addNewContent(), first);
        }

        if (dr.logicalRecord() != null) {
            for (LogicalRecord lr : dr.logicalRecord()) {
                var lrType = drType.addNewLogicalRecord();
                lrType.setIsUniversallyUnique(true);
                lrType.addNewURN().setStringValue(lr.urn());
                lrType.addAgency(lr.agency());
                lrType.addNewID().setStringValue(lr.id());
                lrType.addVersion(lr.version());

                if (lr.label() != null && !lr.label().isEmpty()) {
                    LangString firstLrLabel = lr.label().get(0);
                    var nameString = lrType.addNewLogicalRecordName().addNewString();
                    nameString.setLang(firstLrLabel.language());
                    nameString.setStringValue(firstLrLabel.value());
                    writeLabelContent(lrType.addNewLabel().addNewContent(), firstLrLabel);
                }

                if (lr.variablesInRecord() != null
                        && lr.variablesInRecord().variableUsedReference() != null) {
                    var virType = lrType.addNewVariablesInRecord();
                    for (Reference ref : lr.variablesInRecord().variableUsedReference()) {
                        populateReference(virType.addNewVariableUsedReference(), ref);
                    }
                }
            }
        }

        return doc;
    }

    public FragmentDocument toVariable(Ddi4Variable var) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var varType = doc.addNewFragment().addNewVariable();

        varType.setIsUniversallyUnique(true);
        varType.setVersionDate(var.versionDate() != null ? var.versionDate().dateTime() : null);
        if (var.isGeographic() != null) {
            varType.setIsGeographic(var.isGeographic());
        }
        varType.addNewURN().setStringValue(var.urn());
        varType.addAgency(var.agency());
        varType.addNewID().setStringValue(var.id());
        varType.addVersion(var.version());

        if (var.basedOnObject() != null) {
            populateBasedOnObject(varType.addNewBasedOnObject(), var.basedOnObject());
        }

        if (var.variableName() != null && !var.variableName().isEmpty()) {
            LangString first = var.variableName().get(0);
            var nameString = varType.addNewVariableName().addNewString();
            nameString.setLang(first.language());
            nameString.setStringValue(first.value());
        }

        if (var.label() != null && !var.label().isEmpty()) {
            writeLabelContent(varType.addNewLabel().addNewContent(), var.label().get(0));
        }

        if (var.description() != null && !var.description().isEmpty()) {
            writeLabelContent(varType.addNewDescription().addNewContent(), var.description().get(0));
        }

        var varRepType = varType.addNewVariableRepresentation();
        VariableRepresentation rep = var.variableRepresentation();
        boolean hasValueRepresentation = false;
        if (rep != null) {
            if (rep.codeRepresentation() != null) {
                populateCodeRepresentation(varRepType.addNewValueRepresentation(),
                        rep.codeRepresentation());
                hasValueRepresentation = true;
            }
            if (rep.numericRepresentation() != null) {
                populateNumericRepresentation(varRepType.addNewValueRepresentation(),
                        rep.numericRepresentation());
                hasValueRepresentation = true;
            }
            if (rep.dateTimeRepresentation() != null) {
                populateDateTimeRepresentation(varRepType.addNewValueRepresentation(),
                        rep.dateTimeRepresentation());
                hasValueRepresentation = true;
            }
            if (rep.textRepresentation() != null) {
                populateTextRepresentation(varRepType.addNewValueRepresentation(),
                        rep.textRepresentation());
                hasValueRepresentation = true;
            }
        }

        // #1592 : sans ValueRepresentation, l'export ne produit qu'un <VariableRepresentation/>
        // vide et le type de la variable est perdu. Text étant le type par défaut côté
        // application (cf. getVariableType), on l'écrit explicitement.
        if (!hasValueRepresentation) {
            populateTextRepresentation(varRepType.addNewValueRepresentation(),
                    EMPTY_TEXT_REPRESENTATION);
        }

        // MissingValuesReference se place après la ValueRepresentation dans le schéma.
        if (rep != null && rep.missingValuesReference() != null) {
            populateReference(varRepType.addNewMissingValuesReference(),
                    rep.missingValuesReference());
        }

        return doc;
    }

    public FragmentDocument toCodeList(Ddi4CodeList cl) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var clType = doc.addNewFragment().addNewCodeList();

        clType.setIsUniversallyUnique(true);
        clType.setVersionDate(cl.versionDate() != null ? cl.versionDate().dateTime() : null);
        clType.addNewURN().setStringValue(urnOf(cl.urn(), cl.agency(), cl.id(), cl.version()));
        clType.addAgency(cl.agency());
        clType.addNewID().setStringValue(cl.id());
        clType.addVersion(cl.version());

        // Variante d'une liste partagée : référence DDI vers la liste d'origine.
        if (cl.basedOnObject() != null) {
            populateBasedOnObject(clType.addNewBasedOnObject(), cl.basedOnObject());
        }

        if (cl.label() != null && !cl.label().isEmpty()) {
            writeLabelContent(clType.addNewLabel().addNewContent(), cl.label().get(0));
        }

        if (cl.level() != null) {
            for (Level level : cl.level()) {
                var levelType = clType.addNewLevel();
                if (level.levelNumber() != null) {
                    levelType.setLevelNumber(BigInteger.valueOf(level.levelNumber()));
                }
                if (level.levelName() != null && !level.levelName().isEmpty()) {
                    LangString first = level.levelName().get(0);
                    var nameString = levelType.addNewLevelName().addNewString();
                    nameString.setLang(first.language());
                    nameString.setStringValue(first.value());
                }
                if (level.categoryRelationship() != null) {
                    levelType.setCategoryRelationship(
                            CategoryRelationCodeType.Enum.forString(level.categoryRelationship()));
                }
            }
        }

        if (cl.code() != null) {
            for (Code code : cl.code()) {
                populateCode(clType.addNewCode(), code);
            }
        }

        return doc;
    }

    private void populateCode(CodeType codeType, Code code) {
        codeType.setIsUniversallyUnique(true);
        codeType.addNewURN().setStringValue(
                urnOf(code.urn(), code.agency(), code.id(), code.version()));
        codeType.addAgency(code.agency());
        codeType.addNewID().setStringValue(code.id());
        codeType.addVersion(code.version());

        if (code.categoryReference() != null) {
            populateReference(codeType.addNewCategoryReference(), code.categoryReference());
        }

        if (code.value() != null && code.value().stringValue() != null
                && !code.value().stringValue().isEmpty()) {
            codeType.addNewValue().setStringValue(code.value().stringValue());
        }

        if (code.code() != null) {
            for (Code child : code.code()) {
                populateCode(codeType.addNewCode(), child);
            }
        }
    }

    public FragmentDocument toCodeListScheme(Ddi4CodeListScheme scheme) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var schemeType = doc.addNewFragment().addNewCodeListScheme();

        schemeType.setIsUniversallyUnique(true);
        schemeType.setVersionDate(scheme.versionDate() != null ? scheme.versionDate().dateTime() : null);
        schemeType.addNewURN().setStringValue(scheme.urn());
        schemeType.addAgency(scheme.agency());
        schemeType.addNewID().setStringValue(scheme.id());
        schemeType.addVersion(scheme.version());

        if (scheme.label() != null && !scheme.label().isEmpty()) {
            writeLabelContent(schemeType.addNewLabel().addNewContent(), scheme.label().get(0));
        }

        if (scheme.codeListReference() != null) {
            for (Reference ref : scheme.codeListReference()) {
                populateReference(schemeType.addNewCodeListReference(), ref);
            }
        }

        return doc;
    }

    public FragmentDocument toCategoryScheme(Ddi4CategoryScheme scheme) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var schemeType = doc.addNewFragment().addNewCategoryScheme();

        schemeType.setIsUniversallyUnique(true);
        schemeType.setVersionDate(scheme.versionDate() != null ? scheme.versionDate().dateTime() : null);
        schemeType.addNewURN().setStringValue(scheme.urn());
        schemeType.addAgency(scheme.agency());
        schemeType.addNewID().setStringValue(scheme.id());
        schemeType.addVersion(scheme.version());

        if (scheme.label() != null && !scheme.label().isEmpty()) {
            writeLabelContent(schemeType.addNewLabel().addNewContent(), scheme.label().get(0));
        }

        if (scheme.categoryReference() != null) {
            for (Reference ref : scheme.categoryReference()) {
                populateReference(schemeType.addNewCategoryReference(), ref);
            }
        }

        return doc;
    }

    public FragmentDocument toVariableScheme(Ddi4VariableScheme scheme) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var schemeType = doc.addNewFragment().addNewVariableScheme();

        schemeType.setIsUniversallyUnique(true);
        schemeType.setVersionDate(scheme.versionDate() != null ? scheme.versionDate().dateTime() : null);
        schemeType.addNewURN().setStringValue(scheme.urn());
        schemeType.addAgency(scheme.agency());
        schemeType.addNewID().setStringValue(scheme.id());
        schemeType.addVersion(scheme.version());

        if (scheme.label() != null && !scheme.label().isEmpty()) {
            writeLabelContent(schemeType.addNewLabel().addNewContent(), scheme.label().get(0));
        }

        if (scheme.variableReference() != null) {
            for (Reference ref : scheme.variableReference()) {
                populateReference(schemeType.addNewVariableReference(), ref);
            }
        }

        return doc;
    }

    public FragmentDocument toManagedRepresentationScheme(Ddi4ManagedRepresentationScheme scheme) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var schemeType = doc.addNewFragment().addNewManagedRepresentationScheme();

        schemeType.setIsUniversallyUnique(true);
        schemeType.setVersionDate(scheme.versionDate() != null ? scheme.versionDate().dateTime() : null);
        schemeType.addNewURN().setStringValue(scheme.urn());
        schemeType.addAgency(scheme.agency());
        schemeType.addNewID().setStringValue(scheme.id());
        schemeType.addVersion(scheme.version());

        if (scheme.label() != null && !scheme.label().isEmpty()) {
            writeLabelContent(schemeType.addNewLabel().addNewContent(), scheme.label().get(0));
        }

        if (scheme.managedRepresentationReference() != null) {
            for (Reference ref : scheme.managedRepresentationReference()) {
                ReferenceType refType = schemeType.addNewManagedRepresentationReference();
                populateReference(refType, ref);
                renameToConcreteMemberReference(refType, ref.type());
            }
        }

        return doc;
    }

    /**
     * Éléments concrets du groupe de substitution dont {@code ManagedRepresentationReference} est la
     * tête abstraite : un membre d'un ManagedRepresentationScheme doit être référencé par l'élément
     * correspondant à son type, sinon Colectica ignore la référence (membre absent du scheme dans le
     * portail et relation bysubject non indexée).
     */
    private static final List<String> MANAGED_REPRESENTATION_REFERENCE_MEMBERS = List.of(
            "ManagedTextRepresentationReference",
            "ManagedNumericRepresentationReference",
            "ManagedDateTimeRepresentationReference",
            "ManagedScaleRepresentationReference",
            "ManagedMissingValuesRepresentationReference");

    /**
     * Renomme une référence membre écrite sous la tête abstraite {@code ManagedRepresentationReference}
     * (seul élément que l'API générée sait créer) en l'élément concret dérivé du {@code TypeOfObject}
     * de la référence (ex. {@code ManagedMissingValuesRepresentationReference}). Type absent ou hors
     * du groupe de substitution → l'élément générique est conservé.
     */
    private static void renameToConcreteMemberReference(ReferenceType refType, String typeOfObject) {
        if (typeOfObject == null) {
            return;
        }
        String memberElement = typeOfObject + "Reference";
        if (!MANAGED_REPRESENTATION_REFERENCE_MEMBERS.contains(memberElement)) {
            return;
        }
        try (XmlCursor cursor = refType.newCursor()) {
            cursor.setName(new QName(DDI_REUSABLE_NS, memberElement));
        }
    }

    public FragmentDocument toManagedMissingValuesRepresentation(Ddi4ManagedMissingValuesRepresentation mmvr) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var mmvrType = doc.addNewFragment().addNewManagedMissingValuesRepresentation();

        mmvrType.setIsUniversallyUnique(true);
        // Attribut posé seulement s'il est connu : une MMVR seulement réutilisée arrive sans
        // VersionDate (l'aperçu du front n'en invente pas), et `setVersionDate(null)` écrirait un
        // `versionDate=""` que le schéma DDI 3.3 rejette — et que la relecture ne sait pas parser.
        if (mmvr.versionDate() != null) {
            mmvrType.setVersionDate(mmvr.versionDate().dateTime());
        }
        mmvrType.addNewURN().setStringValue(mmvr.urn());
        mmvrType.addAgency(mmvr.agency());
        mmvrType.addNewID().setStringValue(mmvr.id());
        mmvrType.addVersion(mmvr.version());

        if (mmvr.label() != null && !mmvr.label().isEmpty()) {
            writeLabelContent(mmvrType.addNewLabel().addNewContent(), mmvr.label().get(0));
        }

        if (mmvr.missingCodeRepresentation() != null) {
            for (CodeRepresentation rep : mmvr.missingCodeRepresentation()) {
                CodeRepresentationBaseType missingRep = mmvrType.addNewMissingCodeRepresentation();
                missingRep.setBlankIsMissingValue(Boolean.TRUE.equals(rep.blankIsMissingValue()));
                if (rep.codeListReference() != null) {
                    populateReference(missingRep.addNewCodeListReference(), rep.codeListReference());
                }
            }
        }

        return doc;
    }

    public FragmentDocument toCategory(Ddi4Category cat) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var catType = doc.addNewFragment().addNewCategory();

        catType.setIsUniversallyUnique(true);
        catType.setVersionDate(cat.versionDate() != null ? cat.versionDate().dateTime() : null);
        catType.setIsMissing(false);
        catType.addNewURN().setStringValue(urnOf(cat.urn(), cat.agency(), cat.id(), cat.version()));
        catType.addAgency(cat.agency());
        catType.addNewID().setStringValue(cat.id());
        catType.addVersion(cat.version());

        // Variante d'une catégorie partagée : référence DDI vers la catégorie d'origine.
        if (cat.basedOnObject() != null) {
            populateBasedOnObject(catType.addNewBasedOnObject(), cat.basedOnObject());
        }

        if (cat.label() != null && !cat.label().isEmpty()) {
            writeLabelContent(catType.addNewLabel().addNewContent(), cat.label().get(0));
        }

        return doc;
    }

    public FragmentDocument toGroup(Ddi4Group group) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var groupType = doc.addNewFragment().addNewGroup();

        groupType.setIsUniversallyUnique(true);
        groupType.setVersionDate(group.versionDate() != null ? group.versionDate().dateTime() : null);
        groupType.addNewURN().setStringValue(group.urn());
        groupType.addAgency(group.agency());
        groupType.addNewID().setStringValue(group.id());
        groupType.addVersion(group.version());

        if (group.seriesIris() != null) {
            for (String seriesIri : group.seriesIris()) {
                var userId = groupType.addNewUserID();
                userId.setTypeOfUserID("URI");
                userId.setStringValue(seriesIri);
            }
        }

        if (group.typeOfGroup() != null && !group.typeOfGroup().isEmpty()) {
            groupType.addNewTypeOfGroup().setStringValue(group.typeOfGroup());
        }

        if (group.citation() != null && group.citation().title() != null) {
            LangString first = group.citation().title().get(0);
            var titleString = groupType.addNewCitation().addNewTitle().addNewString();
            titleString.setLang(first.language());
            titleString.setStringValue(first.value());
        }

        if (group.studyUnitReference() != null) {
            for (Reference suRef : group.studyUnitReference()) {
                populateReference(groupType.addNewStudyUnitReference(), suRef);
            }
        }

        if (group.logicalProductReference() != null) {
            for (Reference lpRef : group.logicalProductReference()) {
                populateReference(groupType.addNewLogicalProductReference(), lpRef);
            }
        }

        return doc;
    }

    /**
     * Serializes a {@link Ddi4LogicalProduct} as a DDI 3.3 {@code <LogicalProduct>} fragment.
     * <p>
     * {@code LogicalProduct} is a substitution-group member of the {@code BaseLogicalProduct} head
     * element, which is the only logical-product element the generated {@code FragmentType} exposes;
     * we therefore add the base element and re-type it to {@code LogicalProduct} through a cursor —
     * the same approach used for representation elements elsewhere in this converter.
     */
    public FragmentDocument toLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var base = doc.addNewFragment().addNewBaseLogicalProduct();
        LogicalProductType lpType;
        try (XmlCursor cursor = base.newCursor()) {
            cursor.setName(new QName(DDI_LOGICAL_PRODUCT_NS, "LogicalProduct"));
            lpType = (LogicalProductType) cursor.getObject().changeType(LogicalProductType.type);
        }

        lpType.setIsUniversallyUnique(true);
        lpType.setVersionDate(logicalProduct.versionDate() != null ? logicalProduct.versionDate().dateTime() : null);
        lpType.addNewURN().setStringValue(logicalProduct.urn());
        lpType.addAgency(logicalProduct.agency());
        lpType.addNewID().setStringValue(logicalProduct.id());
        lpType.addVersion(logicalProduct.version());

        if (logicalProduct.label() != null && !logicalProduct.label().isEmpty()) {
            LangString first = logicalProduct.label().get(0);
            var nameString = lpType.addNewLogicalProductName().addNewString();
            nameString.setLang(first.language());
            nameString.setStringValue(first.value());
            writeLabelContent(lpType.addNewLabel().addNewContent(), first);
        }

        if (logicalProduct.codeListSchemeReference() != null) {
            for (Reference ref : logicalProduct.codeListSchemeReference()) {
                populateReference(lpType.addNewCodeListSchemeReference(), ref);
            }
        }

        if (logicalProduct.categorySchemeReference() != null) {
            for (Reference ref : logicalProduct.categorySchemeReference()) {
                populateReference(lpType.addNewCategorySchemeReference(), ref);
            }
        }

        if (logicalProduct.variableSchemeReference() != null) {
            for (Reference ref : logicalProduct.variableSchemeReference()) {
                populateReference(lpType.addNewVariableSchemeReference(), ref);
            }
        }

        if (logicalProduct.managedRepresentationSchemeReference() != null) {
            for (Reference ref : logicalProduct.managedRepresentationSchemeReference()) {
                populateReference(lpType.addNewManagedRepresentationSchemeReference(), ref);
            }
        }

        return doc;
    }

    public FragmentDocument toStudyUnit(Ddi4StudyUnit studyUnit) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var suType = doc.addNewFragment().addNewStudyUnit();

        suType.setIsUniversallyUnique(true);
        suType.setVersionDate(studyUnit.versionDate() != null ? studyUnit.versionDate().dateTime() : null);
        suType.addNewURN().setStringValue(studyUnit.urn());
        suType.addAgency(studyUnit.agency());
        suType.addNewID().setStringValue(studyUnit.id());
        suType.addVersion(studyUnit.version());

        if (studyUnit.operationIri() != null && !studyUnit.operationIri().isEmpty()) {
            var userId = suType.addNewUserID();
            userId.setTypeOfUserID("URI");
            userId.setStringValue(studyUnit.operationIri());
        }

        if (studyUnit.citation() != null && studyUnit.citation().title() != null) {
            LangString first = studyUnit.citation().title().get(0);
            var titleString = suType.addNewCitation().addNewTitle().addNewString();
            titleString.setLang(first.language());
            titleString.setStringValue(first.value());
        }

        if (studyUnit.logicalProductReferences() != null) {
            for (Reference lpRef : studyUnit.logicalProductReferences()) {
                populateReference(suType.addNewLogicalProductReference(), lpRef);
            }
        }

        if (studyUnit.physicalInstanceReferences() != null) {
            for (Reference piRef : studyUnit.physicalInstanceReferences()) {
                populateReference(suType.addNewPhysicalInstanceReference(), piRef);
            }
        }

        return doc;
    }

    private static void writeLabelContent(ContentType target, LangString value) {
        target.setLang(value.language());
        try (XmlCursor cursor = target.newCursor()) {
            cursor.toEndToken();
            cursor.insertChars(value.value());
        }
    }

    private static void populateBasedOnObject(BasedOnObjectType target, BasedOnObject source) {
        if (source.basedOnReferences() == null) return;
        for (Reference ref : source.basedOnReferences()) {
            populateReference(target.addNewBasedOnReference(), ref);
        }
    }

    private static void populateCodeRepresentation(RepresentationType rep, CodeRepresentation source) {
        CodeRepresentationBaseType codeRep;
        try (XmlCursor cursor = rep.newCursor()) {
            cursor.setName(new QName(DDI_REUSABLE_NS, "CodeRepresentation"));
            codeRep = (CodeRepresentationBaseType)
                    cursor.getObject().changeType(CodeRepresentationBaseType.type);
        }
        codeRep.setBlankIsMissingValue(Boolean.TRUE.equals(source.blankIsMissingValue()));

        if (source.codeListReference() != null) {
            populateReference(codeRep.addNewCodeListReference(), source.codeListReference());
        }
    }

    /**
     * URN de l'item, dérivée de son identité quand elle n'est pas fournie — l'URN DDI étant une
     * pure fonction de {@code agence/id/version}, elle n'a pas à être fabriquée par l'appelant.
     * Pendant de {@code AbstractDDIItemConverter#buildReference} sur le chemin de lecture.
     */
    private static String urnOf(String urn, String agency, String id, String version) {
        return urn != null && !urn.isBlank() ? urn : Reference.synthesizeUrn(agency, id, version);
    }

    private static void populateReference(ReferenceType target, Reference source) {
        target.addAgency(source.agency());
        target.addNewID().setStringValue(source.id());
        target.addVersion(source.version());
        if (source.type() != null) {
            target.setTypeOfObject(TypeOfObjectType.Enum.forString(source.type()));
        }
    }

    private static void populateNumericRepresentation(RepresentationType rep, NumericRepresentation source) {
        NumericRepresentationBaseType numRep;
        try (XmlCursor cursor = rep.newCursor()) {
            cursor.setName(new QName(DDI_REUSABLE_NS, "NumericRepresentation"));
            numRep = (NumericRepresentationBaseType)
                    cursor.getObject().changeType(NumericRepresentationBaseType.type);
        }
        numRep.setBlankIsMissingValue(false);

        if (source.numberRange() != null) {
            NumberRangeType nrt = numRep.addNewNumberRange();
            if (source.numberRange().low() != null) {
                populateRangeValue(nrt.addNewLow(), source.numberRange().low());
            }
            if (source.numberRange().high() != null) {
                populateRangeValue(nrt.addNewHigh(), source.numberRange().high());
            }
        }

        if (source.numericTypeCode() != null) {
            numRep.addNewNumericTypeCode().setStringValue(source.numericTypeCode());
        }
    }

    private static void populateRangeValue(NumberRangeValueType target, RangeValue source) {
        if (source.isInclusive() != null) {
            target.setIsInclusive(source.isInclusive());
        }
        if (source.value() != null) {
            target.setStringValue(formatRangeValue(source.value()));
        }
    }

    /**
     * Les bornes sont des {@code xs:decimal} : la notation exponentielle que produit
     * {@link Double#toString(double)} hors de [1e-3, 1e7[ n'y est pas un lexical valide.
     */
    private static String formatRangeValue(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static void populateDateTimeRepresentation(RepresentationType rep, DateTimeRepresentation source) {
        DateTimeRepresentationBaseType dateTimeRep;
        try (XmlCursor cursor = rep.newCursor()) {
            cursor.setName(new QName(DDI_REUSABLE_NS, "DateTimeRepresentation"));
            dateTimeRep = (DateTimeRepresentationBaseType)
                    cursor.getObject().changeType(DateTimeRepresentationBaseType.type);
        }
        if (source.dateTypeCode() != null) {
            dateTimeRep.addNewDateTypeCode().setStringValue(source.dateTypeCode());
        }
        if (source.dateFieldFormat() != null) {
            dateTimeRep.addNewDateFieldFormat().setStringValue(source.dateFieldFormat());
        }
    }

    private static void populateTextRepresentation(RepresentationType rep, TextRepresentation source) {
        TextRepresentationBaseType textRep;
        try (XmlCursor cursor = rep.newCursor()) {
            cursor.setName(new QName(DDI_REUSABLE_NS, "TextRepresentation"));
            textRep = (TextRepresentationBaseType)
                    cursor.getObject().changeType(TextRepresentationBaseType.type);
        }
        if (source.blankIsMissingValue() != null) {
            textRep.setBlankIsMissingValue(source.blankIsMissingValue());
        }
        if (source.maxLength() != null) {
            textRep.setMaxLength(BigInteger.valueOf(source.maxLength()));
        }
        if (source.minLength() != null) {
            textRep.setMinLength(BigInteger.valueOf(source.minLength()));
        }
        if (source.regExp() != null) {
            textRep.setRegExp(source.regExp());
        }
    }
}
