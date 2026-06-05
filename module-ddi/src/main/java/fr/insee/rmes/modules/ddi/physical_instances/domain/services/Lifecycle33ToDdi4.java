package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.group.GroupType;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.logicalproduct.CategoryType;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeListSchemeType;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeListType;
import fr.insee.ddi.lifecycle33.logicalproduct.CodeType;
import fr.insee.ddi.lifecycle33.logicalproduct.DataRelationshipType;
import fr.insee.ddi.lifecycle33.logicalproduct.LogicalRecordType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariableRepresentationType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariableType;
import fr.insee.ddi.lifecycle33.logicalproduct.VariablesInRecordType;
import fr.insee.ddi.lifecycle33.physicalinstance.PhysicalInstanceType;
import fr.insee.ddi.lifecycle33.studyunit.StudyUnitType;
import fr.insee.ddi.lifecycle33.reusable.BasedOnObjectType;
import fr.insee.ddi.lifecycle33.reusable.CitationType;
import fr.insee.ddi.lifecycle33.reusable.CodeRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.ContentType;
import fr.insee.ddi.lifecycle33.reusable.DateTimeRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.LabelType;
import fr.insee.ddi.lifecycle33.reusable.NameType;
import fr.insee.ddi.lifecycle33.reusable.NumberRangeType;
import fr.insee.ddi.lifecycle33.reusable.NumberRangeValueType;
import fr.insee.ddi.lifecycle33.reusable.NumericRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.ReferenceType;
import fr.insee.ddi.lifecycle33.reusable.RepresentationType;
import fr.insee.ddi.lifecycle33.reusable.StringType;
import fr.insee.ddi.lifecycle33.reusable.StructuredStringType;
import fr.insee.ddi.lifecycle33.reusable.TextRepresentationBaseType;
import fr.insee.ddi.lifecycle33.reusable.TypeOfObjectType;
import fr.insee.ddi.lifecycle33.reusable.UserIDType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DateTimeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumberRange;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RangeValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import org.apache.xmlbeans.XmlCursor;

import java.util.ArrayList;
import java.util.List;

public class Lifecycle33ToDdi4 {

    public Ddi4PhysicalInstance toPhysicalInstance(FragmentDocument doc) {
        PhysicalInstanceType pi = doc.getFragment().getPhysicalInstance();
        if (pi == null) {
            throw new IllegalArgumentException("Fragment does not contain a PhysicalInstance");
        }
        return new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime(pi.xgetVersionDate().getStringValue()),
                pi.getURNArray(0).getStringValue(),
                pi.getAgencyArray(0),
                pi.getIDArray(0).getStringValue(),
                pi.getVersionArray(0),
                readBasedOnObject(pi.isSetBasedOnObject() ? pi.getBasedOnObject() : null),
                readCitation(pi.getCitation()),
                readDataRelationshipReferences(pi.getDataRelationshipReferenceArray())
        );
    }

    public Ddi4DataRelationship toDataRelationship(FragmentDocument doc) {
        DataRelationshipType dr = doc.getFragment().getDataRelationship();
        if (dr == null) {
            throw new IllegalArgumentException("Fragment does not contain a DataRelationship");
        }
        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime(dr.xgetVersionDate().getStringValue()),
                dr.getURNArray(0).getStringValue(),
                dr.getAgencyArray(0),
                dr.getIDArray(0).getStringValue(),
                dr.getVersionArray(0),
                readBasedOnObject(dr.isSetBasedOnObject() ? dr.getBasedOnObject() : null),
                readLabelOrName(
                        dr.sizeOfLabelArray() > 0 ? dr.getLabelArray(0) : null,
                        dr.sizeOfDataRelationshipNameArray() > 0
                                ? dr.getDataRelationshipNameArray(0) : null),
                readLogicalRecords(dr.getLogicalRecordArray())
        );
    }

    public Ddi4Variable toVariable(FragmentDocument doc) {
        VariableType var = doc.getFragment().getVariable();
        if (var == null) {
            throw new IllegalArgumentException("Fragment does not contain a Variable");
        }
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                CogsDate.ofDateTime(var.xgetVersionDate().getStringValue()),
                var.getURNArray(0).getStringValue(),
                var.getAgencyArray(0),
                var.getIDArray(0).getStringValue(),
                var.getVersionArray(0),
                readBasedOnObject(var.isSetBasedOnObject() ? var.getBasedOnObject() : null),
                var.sizeOfVariableNameArray() > 0
                        ? readName(var.getVariableNameArray(0)) : null,
                var.sizeOfLabelArray() > 0
                        ? readLabel(var.getLabelArray(0)) : null,
                var.isSetDescription()
                        ? readStructuredString(var.getDescription()) : null,
                readVariableRepresentation(var.getVariableRepresentation()),
                var.isSetIsGeographic() ? var.getIsGeographic() : null
        );
    }

    public Ddi4CodeList toCodeList(FragmentDocument doc) {
        CodeListType cl = doc.getFragment().getCodeList();
        if (cl == null) {
            throw new IllegalArgumentException("Fragment does not contain a CodeList");
        }
        List<Code> codes = new ArrayList<>();
        for (CodeType c : cl.getCodeArray()) {
            Reference catRef = readReference(c.getCategoryReference());
            codes.add(new Code(
                    Code.TYPE,
                    c.getURNArray(0).getStringValue(),
                    c.getAgencyArray(0),
                    c.getIDArray(0).getStringValue(),
                    c.getVersionArray(0),
                    catRef,
                    c.getValue() != null ? ValueType.of(c.getValue().getStringValue()) : null));
        }
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime(cl.xgetVersionDate().getStringValue()),
                cl.getURNArray(0).getStringValue(),
                cl.getAgencyArray(0),
                cl.getIDArray(0).getStringValue(),
                cl.getVersionArray(0),
                cl.sizeOfLabelArray() > 0 ? readLabel(cl.getLabelArray(0)) : null,
                codes.isEmpty() ? null : codes
        );
    }

    public Ddi4CodeListScheme toCodeListScheme(FragmentDocument doc) {
        CodeListSchemeType scheme = doc.getFragment().getCodeListScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Fragment does not contain a CodeListScheme");
        }
        List<Reference> codeListReferences = new ArrayList<>();
        for (ReferenceType ref : scheme.getCodeListReferenceArray()) {
            codeListReferences.add(readReference(ref));
        }
        return new Ddi4CodeListScheme(
                Ddi4CodeListScheme.TYPE,
                CogsDate.ofDateTime(scheme.xgetVersionDate().getStringValue()),
                scheme.getURNArray(0).getStringValue(),
                scheme.getAgencyArray(0),
                scheme.getIDArray(0).getStringValue(),
                scheme.getVersionArray(0),
                scheme.sizeOfLabelArray() > 0 ? readLabel(scheme.getLabelArray(0)) : null,
                codeListReferences.isEmpty() ? null : codeListReferences
        );
    }

    public Ddi4Category toCategory(FragmentDocument doc) {
        CategoryType cat = doc.getFragment().getCategory();
        if (cat == null) {
            throw new IllegalArgumentException("Fragment does not contain a Category");
        }
        return new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime(cat.xgetVersionDate().getStringValue()),
                cat.getURNArray(0).getStringValue(),
                cat.getAgencyArray(0),
                cat.getIDArray(0).getStringValue(),
                cat.getVersionArray(0),
                cat.sizeOfLabelArray() > 0 ? readLabel(cat.getLabelArray(0)) : null
        );
    }

    public Ddi4Group toGroup(FragmentDocument doc) {
        GroupType group = doc.getFragment().getGroup();
        if (group == null) {
            throw new IllegalArgumentException("Fragment does not contain a Group");
        }
        List<String> seriesIris = new ArrayList<>();
        for (UserIDType userId : group.getUserIDList()) {
            seriesIris.add(userId.getStringValue());
        }
        List<Reference> suRefs = new ArrayList<>();
        for (ReferenceType ref : group.getStudyUnitReferenceArray()) {
            suRefs.add(readReference(ref));
        }
        return new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime(group.xgetVersionDate().getStringValue()),
                group.getURNArray(0).getStringValue(),
                group.getAgencyArray(0),
                group.getIDArray(0).getStringValue(),
                group.getVersionArray(0),
                null,
                readCitation(group.getCitation()),
                suRefs,
                seriesIris.isEmpty() ? null : seriesIris,
                group.getTypeOfGroup() != null ? group.getTypeOfGroup().getStringValue() : null
        );
    }

    public Ddi4StudyUnit toStudyUnit(FragmentDocument doc) {
        StudyUnitType su = doc.getFragment().getStudyUnit();
        if (su == null) {
            throw new IllegalArgumentException("Fragment does not contain a StudyUnit");
        }
        String operationIri = su.sizeOfUserIDArray() > 0
                ? su.getUserIDArray(0).getStringValue() : null;
        List<Reference> piRefs = new ArrayList<>();
        for (ReferenceType ref : su.getPhysicalInstanceReferenceArray()) {
            piRefs.add(readReference(ref));
        }
        return new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime(su.xgetVersionDate().getStringValue()),
                su.getURNArray(0).getStringValue(),
                su.getAgencyArray(0),
                su.getIDArray(0).getStringValue(),
                su.getVersionArray(0),
                readCitation(su.getCitation()),
                operationIri,
                piRefs.isEmpty() ? null : piRefs
        );
    }

    private static BasedOnObject readBasedOnObject(BasedOnObjectType source) {
        if (source == null || source.sizeOfBasedOnReferenceArray() == 0) return null;
        List<Reference> refs = new ArrayList<>();
        for (ReferenceType ref : source.getBasedOnReferenceArray()) {
            refs.add(readReference(ref));
        }
        return BasedOnObject.of(refs);
    }

    private static List<Reference> readDataRelationshipReferences(ReferenceType[] refs) {
        if (refs == null || refs.length == 0) return null;
        List<Reference> result = new ArrayList<>();
        for (ReferenceType ref : refs) {
            result.add(readReference(ref));
        }
        return result;
    }

    private static String typeOfObjectAsString(ReferenceType ref) {
        TypeOfObjectType.Enum typeEnum = ref.getTypeOfObject();
        return typeEnum != null ? typeEnum.toString() : null;
    }

    private static VariableRepresentation readVariableRepresentation(VariableRepresentationType vr) {
        if (vr == null) return null;
        RepresentationType rep = vr.isSetValueRepresentation() ? vr.getValueRepresentation() : null;
        String repName = elementLocalName(rep);
        return new VariableRepresentation(
                vr.isSetVariableRole() && vr.getVariableRole() != null
                        ? vr.getVariableRole().getStringValue() : null,
                "CodeRepresentation".equals(repName) ? readCodeRepresentation(rep) : null,
                "NumericRepresentation".equals(repName) ? readNumericRepresentation(rep) : null,
                "DateTimeRepresentation".equals(repName) ? readDateTimeRepresentation(rep) : null,
                "TextRepresentation".equals(repName) ? readTextRepresentation(rep) : null
        );
    }

    private static String elementLocalName(RepresentationType rep) {
        if (rep == null) return null;
        try (XmlCursor cursor = rep.newCursor()) {
            return cursor.getName() != null ? cursor.getName().getLocalPart() : null;
        }
    }

    private static CodeRepresentation readCodeRepresentation(RepresentationType rep) {
        CodeRepresentationBaseType codeRep =
                (CodeRepresentationBaseType) rep.changeType(CodeRepresentationBaseType.type);
        Reference clRef = codeRep.isSetCodeListReference()
                ? readReference(codeRep.getCodeListReference())
                : null;
        return new CodeRepresentation(CodeRepresentation.TYPE, codeRep.getBlankIsMissingValue(), clRef);
    }

    private static Reference readReference(ReferenceType ref) {
        if (ref == null) return null;
        String agency = ref.sizeOfAgencyArray() > 0 ? ref.getAgencyArray(0) : null;
        String id = ref.sizeOfIDArray() > 0 ? ref.getIDArray(0).getStringValue() : null;
        String version = ref.sizeOfVersionArray() > 0 ? ref.getVersionArray(0) : null;
        String type = typeOfObjectAsString(ref);
        return Reference.of(agency, id, version, type);
    }

    private static NumericRepresentation readNumericRepresentation(RepresentationType rep) {
        NumericRepresentationBaseType numRep =
                (NumericRepresentationBaseType) rep.changeType(NumericRepresentationBaseType.type);
        NumberRange numberRange = null;
        if (numRep.sizeOfNumberRangeArray() > 0) {
            NumberRangeType nrt = numRep.getNumberRangeArray(0);
            RangeValue low = nrt.isSetLow() ? toRangeValue(nrt.getLow()) : null;
            RangeValue high = nrt.isSetHigh() ? toRangeValue(nrt.getHigh()) : null;
            numberRange = new NumberRange(low, high);
        }
        String typeCode = numRep.isSetNumericTypeCode() && numRep.getNumericTypeCode() != null
                ? numRep.getNumericTypeCode().getStringValue() : null;
        return new NumericRepresentation(NumericRepresentation.TYPE,typeCode, numberRange);
    }

    private static RangeValue toRangeValue(NumberRangeValueType v) {
        String text = v.getStringValue();
        Double value = (text == null || text.isEmpty()) ? null : Double.parseDouble(text);
        return new RangeValue(v.getIsInclusive(), value);
    }

    private static DateTimeRepresentation readDateTimeRepresentation(RepresentationType rep) {
        DateTimeRepresentationBaseType dt =
                (DateTimeRepresentationBaseType) rep.changeType(DateTimeRepresentationBaseType.type);
        String typeCode = dt.getDateTypeCode() != null ? dt.getDateTypeCode().getStringValue() : null;
        String format = dt.isSetDateFieldFormat() && dt.getDateFieldFormat() != null
                ? dt.getDateFieldFormat().getStringValue() : null;
        return new DateTimeRepresentation(DateTimeRepresentation.TYPE,typeCode, format);
    }

    private static TextRepresentation readTextRepresentation(RepresentationType rep) {
        TextRepresentationBaseType txt =
                (TextRepresentationBaseType) rep.changeType(TextRepresentationBaseType.type);
        Integer minLength = txt.isSetMinLength() && txt.getMinLength() != null
                ? txt.getMinLength().intValueExact() : null;
        Integer maxLength = txt.isSetMaxLength() && txt.getMaxLength() != null
                ? txt.getMaxLength().intValueExact() : null;
        String regExp = txt.isSetRegExp() ? txt.getRegExp() : null;
        return new TextRepresentation(TextRepresentation.TYPE, maxLength, minLength, regExp, txt.getBlankIsMissingValue());
    }

    private static List<LangString> readStructuredString(StructuredStringType struct) {
        if (struct == null || struct.sizeOfContentArray() == 0) return null;
        List<LangString> langStrings = new ArrayList<>();
        for (ContentType content : struct.getContentArray()) {
            langStrings.add(new LangString(content.getLang(), readContentText(content)));
        }
        return langStrings;
    }

    private static LogicalRecord readLogicalRecord(LogicalRecordType lr) {
        if (lr == null) return null;
        return new LogicalRecord(
                LogicalRecord.TYPE,
                lr.getURNArray(0).getStringValue(),
                lr.getAgencyArray(0),
                lr.getIDArray(0).getStringValue(),
                lr.getVersionArray(0),
                readLabelOrName(
                        lr.sizeOfLabelArray() > 0 ? lr.getLabelArray(0) : null,
                        lr.sizeOfLogicalRecordNameArray() > 0
                                ? lr.getLogicalRecordNameArray(0) : null),
                readVariablesInRecord(lr.getVariablesInRecord())
        );
    }

    private static List<LogicalRecord> readLogicalRecords(LogicalRecordType[] lrs) {
        if (lrs == null || lrs.length == 0) return null;
        List<LogicalRecord> result = new ArrayList<>();
        for (LogicalRecordType lr : lrs) {
            result.add(readLogicalRecord(lr));
        }
        return result;
    }

    private static VariablesInRecord readVariablesInRecord(VariablesInRecordType vir) {
        if (vir == null || vir.sizeOfVariableUsedReferenceArray() == 0) return null;
        List<Reference> refs = new ArrayList<>();
        for (ReferenceType ref : vir.getVariableUsedReferenceArray()) {
            refs.add(readReference(ref));
        }
        return new VariablesInRecord(refs);
    }

    private static List<LangString> readLabelOrName(LabelType label, NameType name) {
        List<LangString> fromLabel = readLabel(label);
        if (fromLabel != null) return fromLabel;
        return readName(name);
    }

    private static List<LangString> readLabel(LabelType label) {
        if (label == null || label.sizeOfContentArray() == 0) return null;
        List<LangString> langStrings = new ArrayList<>();
        for (ContentType content : label.getContentArray()) {
            langStrings.add(new LangString(content.getLang(), readContentText(content)));
        }
        return langStrings;
    }

    private static List<LangString> readName(NameType name) {
        if (name == null || name.sizeOfStringArray() == 0) return null;
        StringType s = name.getStringArray(0);
        return LangStrings.of(s.getLang(), s.getStringValue());
    }

    private static String readContentText(ContentType content) {
        try (XmlCursor cursor = content.newCursor()) {
            cursor.toFirstContentToken();
            return cursor.isText() ? cursor.getChars() : "";
        }
    }

    private static Citation readCitation(CitationType citation) {
        if (citation == null || !citation.isSetTitle()) return null;
        List<LangString> titles = new ArrayList<>();
        for (StringType s : citation.getTitle().getStringList()) {
            titles.add(new LangString(s.getLang(), s.getStringValue()));
        }
        return titles.isEmpty() ? null : new Citation(titles);
    }
}
