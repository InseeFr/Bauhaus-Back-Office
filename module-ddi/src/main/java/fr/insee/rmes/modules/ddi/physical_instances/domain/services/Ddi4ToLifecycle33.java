package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.reusable.BasedOnObjectType;
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
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DataRelationshipReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DateTimeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;
import java.math.BigInteger;

public class Ddi4ToLifecycle33 {

    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";

    public FragmentDocument toPhysicalInstance(Ddi4PhysicalInstance pi) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var piType = doc.addNewFragment().addNewPhysicalInstance();

        piType.setIsUniversallyUnique(Boolean.parseBoolean(pi.isUniversallyUnique()));
        piType.setVersionDate(pi.versionDate());
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

        DataRelationshipReference dataRelationshipReference = pi.dataRelationshipReference();
        if (dataRelationshipReference != null) {
            ReferenceType refType = piType.addNewDataRelationshipReference();
            refType.addAgency(dataRelationshipReference.agency());
            refType.addNewID().setStringValue(dataRelationshipReference.id());
            refType.addVersion(dataRelationshipReference.version());
            refType.setTypeOfObject(
                    TypeOfObjectType.Enum.forString(dataRelationshipReference.typeOfObject()));
        }

        return doc;
    }

    public FragmentDocument toDataRelationship(Ddi4DataRelationship dr) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var drType = doc.addNewFragment().addNewDataRelationship();

        drType.setIsUniversallyUnique(Boolean.parseBoolean(dr.isUniversallyUnique()));
        drType.setVersionDate(dr.versionDate());
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
            LogicalRecord lr = dr.logicalRecord();
            var lrType = drType.addNewLogicalRecord();
            lrType.setIsUniversallyUnique(Boolean.parseBoolean(lr.isUniversallyUnique()));
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

        return doc;
    }

    public FragmentDocument toVariable(Ddi4Variable var) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var varType = doc.addNewFragment().addNewVariable();

        varType.setIsUniversallyUnique(Boolean.parseBoolean(var.isUniversallyUnique()));
        varType.setVersionDate(var.versionDate());
        if (var.isGeographic() != null && !var.isGeographic().isEmpty()) {
            varType.setIsGeographic(Boolean.parseBoolean(var.isGeographic()));
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
        if (rep != null) {
            if (rep.codeRepresentation() != null) {
                populateCodeRepresentation(varRepType.addNewValueRepresentation(),
                        rep.codeRepresentation());
            }
            if (rep.numericRepresentation() != null) {
                populateNumericRepresentation(varRepType.addNewValueRepresentation(),
                        rep.numericRepresentation());
            }
            if (rep.dateTimeRepresentation() != null) {
                populateDateTimeRepresentation(varRepType.addNewValueRepresentation(),
                        rep.dateTimeRepresentation());
            }
            if (rep.textRepresentation() != null) {
                populateTextRepresentation(varRepType.addNewValueRepresentation(),
                        rep.textRepresentation());
            }
        }

        return doc;
    }

    public FragmentDocument toCodeList(Ddi4CodeList cl) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var clType = doc.addNewFragment().addNewCodeList();

        clType.setIsUniversallyUnique(Boolean.parseBoolean(cl.isUniversallyUnique()));
        clType.setVersionDate(cl.versionDate());
        clType.addNewURN().setStringValue(cl.urn());
        clType.addAgency(cl.agency());
        clType.addNewID().setStringValue(cl.id());
        clType.addVersion(cl.version());

        if (cl.label() != null && !cl.label().isEmpty()) {
            writeLabelContent(clType.addNewLabel().addNewContent(), cl.label().get(0));
        }

        if (cl.code() != null) {
            for (Code code : cl.code()) {
                var codeType = clType.addNewCode();
                codeType.setIsUniversallyUnique(Boolean.parseBoolean(code.isUniversallyUnique()));
                codeType.addNewURN().setStringValue(code.urn());
                codeType.addAgency(code.agency());
                codeType.addNewID().setStringValue(code.id());
                codeType.addVersion(code.version());

                if (code.categoryReference() != null) {
                    populateReference(codeType.addNewCategoryReference(), code.categoryReference());
                }

                if (code.value() != null && !code.value().isEmpty()) {
                    codeType.addNewValue().setStringValue(code.value());
                }
            }
        }

        return doc;
    }

    public FragmentDocument toCategory(Ddi4Category cat) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var catType = doc.addNewFragment().addNewCategory();

        catType.setIsUniversallyUnique(Boolean.parseBoolean(cat.isUniversallyUnique()));
        catType.setVersionDate(cat.versionDate());
        catType.setIsMissing(false);
        catType.addNewURN().setStringValue(cat.urn());
        catType.addAgency(cat.agency());
        catType.addNewID().setStringValue(cat.id());
        catType.addVersion(cat.version());

        if (cat.label() != null && !cat.label().isEmpty()) {
            writeLabelContent(catType.addNewLabel().addNewContent(), cat.label().get(0));
        }

        return doc;
    }

    public FragmentDocument toGroup(Ddi4Group group) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var groupType = doc.addNewFragment().addNewGroup();

        groupType.setIsUniversallyUnique(Boolean.parseBoolean(group.isUniversallyUnique()));
        groupType.setVersionDate(group.versionDate());
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

        return doc;
    }

    public FragmentDocument toStudyUnit(Ddi4StudyUnit studyUnit) {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var suType = doc.addNewFragment().addNewStudyUnit();

        suType.setIsUniversallyUnique(Boolean.parseBoolean(studyUnit.isUniversallyUnique()));
        suType.setVersionDate(studyUnit.versionDate());
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
        codeRep.setBlankIsMissingValue(Boolean.parseBoolean(source.blankIsMissingValue()));

        if (source.codeListReference() != null) {
            populateReference(codeRep.addNewCodeListReference(), source.codeListReference());
        }
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
                NumberRangeValueType low = nrt.addNewLow();
                low.setIsInclusive(Boolean.parseBoolean(source.numberRange().low().isInclusive()));
                low.setStringValue(source.numberRange().low().text());
            }
            if (source.numberRange().high() != null) {
                NumberRangeValueType high = nrt.addNewHigh();
                high.setIsInclusive(Boolean.parseBoolean(source.numberRange().high().isInclusive()));
                high.setStringValue(source.numberRange().high().text());
            }
        }

        if (source.numericTypeCode() != null) {
            numRep.addNewNumericTypeCode().setStringValue(source.numericTypeCode());
        }
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
            textRep.setBlankIsMissingValue(Boolean.parseBoolean(source.blankIsMissingValue()));
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
