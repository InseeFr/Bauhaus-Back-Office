package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.instance.FragmentInstanceDocument;
import fr.insee.ddi.lifecycle33.reusable.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ddi3XmlWriter {

    private final Map<String, String> itemTypes;

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";
    private static final String DDI_LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";
    private static final String DDI_GROUP_NS = "ddi:group:3_3";
    private static final String DDI_STUDY_UNIT_NS = "ddi:studyunit:3_3";

    public Ddi3XmlWriter(Map<String, String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public String buildGroupXml(Ddi4Group group) throws XMLStreamException {
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
            StringType titleStr = groupType.addNewCitation().addNewTitle().addNewString();
            titleStr.setLang(group.citation().title().string().xmlLang());
            titleStr.setStringValue(group.citation().title().string().text());
        }

        if (group.studyUnitReference() != null) {
            for (StudyUnitReference suRef : group.studyUnitReference()) {
                ReferenceType refType = groupType.addNewStudyUnitReference();
                refType.addAgency(suRef.agency());
                refType.addNewID().setStringValue(suRef.id());
                refType.addVersion(suRef.version());
                refType.setTypeOfObject(TypeOfObjectType.Enum.forString(suRef.typeOfObject()));
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_GROUP_NS));
    }

    public String buildStudyUnitXml(Ddi4StudyUnit studyUnit) throws XMLStreamException {
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
            StringType titleStr = suType.addNewCitation().addNewTitle().addNewString();
            titleStr.setLang(studyUnit.citation().title().string().xmlLang());
            titleStr.setStringValue(studyUnit.citation().title().string().text());
        }

        if (studyUnit.physicalInstanceReferences() != null) {
            for (DDIReference piRef : studyUnit.physicalInstanceReferences()) {
                ReferenceType refType = suType.addNewPhysicalInstanceReference();
                refType.addAgency(piRef.agency());
                refType.addNewID().setStringValue(piRef.id());
                refType.addVersion(piRef.version());
                refType.setTypeOfObject(TypeOfObjectType.Enum.forString("PhysicalInstance"));
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_STUDY_UNIT_NS));
    }

    public String buildPhysicalInstanceXml(Ddi4PhysicalInstance pi) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var piType = doc.addNewFragment().addNewPhysicalInstance();

        piType.setIsUniversallyUnique(Boolean.parseBoolean(pi.isUniversallyUnique()));
        piType.setVersionDate(pi.versionDate());
        piType.addNewURN().setStringValue(pi.urn());
        piType.addAgency(pi.agency());
        piType.addNewID().setStringValue(pi.id());
        piType.addVersion(pi.version());

        populateBasedOnObject(pi.basedOnObject(), () -> piType.addNewBasedOnObject().addNewBasedOnReference());

        if (pi.citation() != null && pi.citation().title() != null) {
            StringType titleStr = piType.addNewCitation().addNewTitle().addNewString();
            titleStr.setLang(pi.citation().title().string().xmlLang());
            titleStr.setStringValue(pi.citation().title().string().text());
        }

        if (pi.dataRelationshipReference() != null) {
            ReferenceType refType = piType.addNewDataRelationshipReference();
            refType.addAgency(pi.dataRelationshipReference().agency());
            refType.addNewID().setStringValue(pi.dataRelationshipReference().id());
            refType.addVersion(pi.dataRelationshipReference().version());
            refType.setTypeOfObject(TypeOfObjectType.Enum.forString(pi.dataRelationshipReference().typeOfObject()));
        }

        return doc.xmlText(fragmentXmlOptions(DDI_PHYSICAL_INSTANCE_NS));
    }

    public String buildDataRelationshipXml(Ddi4DataRelationship dr) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var drType = doc.addNewFragment().addNewDataRelationship();

        drType.setIsUniversallyUnique(Boolean.parseBoolean(dr.isUniversallyUnique()));
        drType.setVersionDate(dr.versionDate());
        drType.addNewURN().setStringValue(dr.urn());
        drType.addAgency(dr.agency());
        drType.addNewID().setStringValue(dr.id());
        drType.addVersion(dr.version());

        populateBasedOnObject(dr.basedOnObject(), () -> drType.addNewBasedOnObject().addNewBasedOnReference());

        if (dr.label() != null && dr.label().content() != null) {
            StringType nameStr = drType.addNewDataRelationshipName().addNewString();
            nameStr.setLang(dr.label().content().xmlLang());
            nameStr.setStringValue(dr.label().content().text());

            ContentType content = drType.addNewLabel().addNewContent();
            content.setLang(dr.label().content().xmlLang());
            setContentText(content, dr.label().content().text());
        }

        if (dr.logicalRecord() != null) {
            LogicalRecord lr = dr.logicalRecord();
            var lrType = drType.addNewLogicalRecord();
            lrType.setIsUniversallyUnique(Boolean.parseBoolean(lr.isUniversallyUnique()));
            lrType.addNewURN().setStringValue(lr.urn());
            lrType.addAgency(lr.agency());
            lrType.addNewID().setStringValue(lr.id());
            lrType.addVersion(lr.version());

            if (lr.label() != null && lr.label().content() != null) {
                StringType nameStr = lrType.addNewLogicalRecordName().addNewString();
                nameStr.setLang(lr.label().content().xmlLang());
                nameStr.setStringValue(lr.label().content().text());

                ContentType content = lrType.addNewLabel().addNewContent();
                content.setLang(lr.label().content().xmlLang());
                setContentText(content, lr.label().content().text());
            }

            if (lr.variablesInRecord() != null && lr.variablesInRecord().variableUsedReference() != null) {
                var virType = lrType.addNewVariablesInRecord();
                for (VariableUsedReference ref : lr.variablesInRecord().variableUsedReference()) {
                    ReferenceType refType = virType.addNewVariableUsedReference();
                    refType.addAgency(ref.agency());
                    refType.addNewID().setStringValue(ref.id());
                    refType.addVersion(ref.version());
                    refType.setTypeOfObject(TypeOfObjectType.Enum.forString(ref.typeOfObject()));
                }
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
    }

    public String buildVariableXml(Ddi4Variable var) throws XMLStreamException {
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

        populateBasedOnObject(var.basedOnObject(), () -> varType.addNewBasedOnObject().addNewBasedOnReference());

        if (var.variableName() != null) {
            StringType nameStr = varType.addNewVariableName().addNewString();
            nameStr.setLang(var.variableName().string().xmlLang());
            nameStr.setStringValue(var.variableName().string().text());
        }

        if (var.label() != null && var.label().content() != null) {
            ContentType content = varType.addNewLabel().addNewContent();
            content.setLang(var.label().content().xmlLang());
            setContentText(content, var.label().content().text());
        }

        if (var.description() != null && var.description().content() != null) {
            ContentType content = varType.addNewDescription().addNewContent();
            content.setLang(var.description().content().xmlLang());
            setContentText(content, var.description().content().text());
        }

        var varRepType = varType.addNewVariableRepresentation();

        if (var.variableRepresentation() != null) {
            if (var.variableRepresentation().variableRole() != null) {
                varRepType.addNewVariableRole().setStringValue(var.variableRepresentation().variableRole());
            }

            if (var.variableRepresentation().numericRepresentation() != null) {
                NumericRepresentation numRepDomain = var.variableRepresentation().numericRepresentation();
                RepresentationType rep = varRepType.addNewValueRepresentation();
                NumericRepresentationBaseType numRep;
                try (XmlCursor cursor = rep.newCursor()) {
                    cursor.setName(new QName(DDI_REUSABLE_NS, "NumericRepresentation"));
                    numRep = (NumericRepresentationBaseType) cursor.getObject().changeType(NumericRepresentationBaseType.type);
                }
                numRep.setBlankIsMissingValue(false);

                if (numRepDomain.numberRange() != null) {
                    NumberRangeType nrt = numRep.addNewNumberRange();
                    if (numRepDomain.numberRange().low() != null) {
                        NumberRangeValueType low = nrt.addNewLow();
                        low.setIsInclusive(Boolean.parseBoolean(numRepDomain.numberRange().low().isInclusive()));
                        low.setStringValue(numRepDomain.numberRange().low().text());
                    }
                    if (numRepDomain.numberRange().high() != null) {
                        NumberRangeValueType high = nrt.addNewHigh();
                        high.setIsInclusive(Boolean.parseBoolean(numRepDomain.numberRange().high().isInclusive()));
                        high.setStringValue(numRepDomain.numberRange().high().text());
                    }
                }

                if (numRepDomain.numericTypeCode() != null) {
                    numRep.addNewNumericTypeCode().setStringValue(numRepDomain.numericTypeCode());
                }
            }

            if (var.variableRepresentation().codeRepresentation() != null) {
                CodeRepresentation codeRepDomain = var.variableRepresentation().codeRepresentation();
                RepresentationType rep = varRepType.addNewValueRepresentation();
                CodeRepresentationBaseType codeRep;
                try (XmlCursor cursor = rep.newCursor()) {
                    cursor.setName(new QName(DDI_REUSABLE_NS, "CodeRepresentation"));
                    codeRep = (CodeRepresentationBaseType) cursor.getObject().changeType(CodeRepresentationBaseType.type);
                }
                codeRep.setBlankIsMissingValue(Boolean.parseBoolean(codeRepDomain.blankIsMissingValue()));

                if (codeRepDomain.codeListReference() != null) {
                    ReferenceType ref = codeRep.addNewCodeListReference();
                    ref.addAgency(codeRepDomain.codeListReference().agency());
                    ref.addNewID().setStringValue(codeRepDomain.codeListReference().id());
                    ref.addVersion(codeRepDomain.codeListReference().version());
                    ref.setTypeOfObject(TypeOfObjectType.Enum.forString(codeRepDomain.codeListReference().typeOfObject()));
                }
            }

            if (var.variableRepresentation().dateTimeRepresentation() != null) {
                DateTimeRepresentation dateTimeDomain = var.variableRepresentation().dateTimeRepresentation();
                RepresentationType rep = varRepType.addNewValueRepresentation();
                DateTimeRepresentationBaseType dateTimeRep;
                try (XmlCursor cursor = rep.newCursor()) {
                    cursor.setName(new QName(DDI_REUSABLE_NS, "DateTimeRepresentation"));
                    dateTimeRep = (DateTimeRepresentationBaseType) cursor.getObject().changeType(DateTimeRepresentationBaseType.type);
                }

                if (dateTimeDomain.dateTypeCode() != null) {
                    dateTimeRep.addNewDateTypeCode().setStringValue(dateTimeDomain.dateTypeCode());
                }
                if (dateTimeDomain.dateFieldFormat() != null) {
                    dateTimeRep.addNewDateFieldFormat().setStringValue(dateTimeDomain.dateFieldFormat());
                }
            }

            if (var.variableRepresentation().textRepresentation() != null) {
                TextRepresentation textDomain = var.variableRepresentation().textRepresentation();
                RepresentationType rep = varRepType.addNewValueRepresentation();
                TextRepresentationBaseType textRep;
                try (XmlCursor cursor = rep.newCursor()) {
                    cursor.setName(new QName(DDI_REUSABLE_NS, "TextRepresentation"));
                    textRep = (TextRepresentationBaseType) cursor.getObject().changeType(TextRepresentationBaseType.type);
                }

                if (textDomain.blankIsMissingValue() != null) {
                    textRep.setBlankIsMissingValue(Boolean.parseBoolean(textDomain.blankIsMissingValue()));
                }
                if (textDomain.maxLength() != null) {
                    textRep.setMaxLength(BigInteger.valueOf(textDomain.maxLength()));
                }
                if (textDomain.minLength() != null) {
                    textRep.setMinLength(BigInteger.valueOf(textDomain.minLength()));
                }
                if (textDomain.regExp() != null) {
                    textRep.setRegExp(textDomain.regExp());
                }
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
    }

    public String buildCodeListXml(Ddi4CodeList cl) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var clType = doc.addNewFragment().addNewCodeList();

        clType.setIsUniversallyUnique(Boolean.parseBoolean(cl.isUniversallyUnique()));
        clType.setVersionDate(cl.versionDate());
        clType.addNewURN().setStringValue(cl.urn());
        clType.addAgency(cl.agency());
        clType.addNewID().setStringValue(cl.id());
        clType.addVersion(cl.version());

        if (cl.label() != null && cl.label().content() != null) {
            ContentType content = clType.addNewLabel().addNewContent();
            content.setLang(cl.label().content().xmlLang());
            setContentText(content, cl.label().content().text());
        }

        if (cl.code() != null && !cl.code().isEmpty()) {
            for (Code code : cl.code()) {
                var codeType = clType.addNewCode();
                codeType.setIsUniversallyUnique(Boolean.parseBoolean(code.isUniversallyUnique()));
                codeType.addNewURN().setStringValue(code.urn());
                codeType.addAgency(code.agency());
                codeType.addNewID().setStringValue(code.id());
                codeType.addVersion(code.version());

                if (code.categoryReference() != null) {
                    ReferenceType ref = codeType.addNewCategoryReference();
                    ref.addAgency(code.categoryReference().agency());
                    ref.addNewID().setStringValue(code.categoryReference().id());
                    ref.addVersion(code.categoryReference().version());
                    ref.setTypeOfObject(TypeOfObjectType.Enum.forString(code.categoryReference().typeOfObject()));
                }

                if (code.value() != null && !code.value().isEmpty()) {
                    codeType.addNewValue().setStringValue(code.value());
                }
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
    }

    public String buildCategoryXml(Ddi4Category cat) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var catType = doc.addNewFragment().addNewCategory();

        catType.setIsUniversallyUnique(Boolean.parseBoolean(cat.isUniversallyUnique()));
        catType.setVersionDate(cat.versionDate());
        catType.setIsMissing(false);
        catType.addNewURN().setStringValue(cat.urn());
        catType.addAgency(cat.agency());
        catType.addNewID().setStringValue(cat.id());
        catType.addVersion(cat.version());

        if (cat.label() != null && cat.label().content() != null) {
            ContentType content = catType.addNewLabel().addNewContent();
            content.setLang(cat.label().content().xmlLang());
            setContentText(content, cat.label().content().text());
        }

        return doc.xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
    }

    public String buildFragmentInstanceDocument(Ddi3Response ddi3Response, TopLevelReference topLevelReference) {
        if (ddi3Response == null || ddi3Response.items() == null || ddi3Response.items().isEmpty()) {
            throw new IllegalArgumentException("Ddi3Response must contain at least one item");
        }

        FragmentInstanceDocument doc = FragmentInstanceDocument.Factory.newInstance();
        var fiType = doc.addNewFragmentInstance();

        Ddi3Response.Ddi3Item topLevelItem;
        String typeOfObject;

        if (topLevelReference != null) {
            final String tlrId = topLevelReference.id();
            final String tlrAgency = topLevelReference.agency();
            topLevelItem = ddi3Response.items().stream()
                    .filter(item -> tlrId.equals(item.identifier()) && tlrAgency.equals(item.agencyId()))
                    .findFirst()
                    .orElse(ddi3Response.items().getFirst());
            typeOfObject = topLevelReference.typeOfObject();
        } else {
            topLevelItem = ddi3Response.items().stream()
                    .filter(item -> itemTypes.get("PhysicalInstance").equals(item.itemType()))
                    .findFirst()
                    .orElse(ddi3Response.items().getFirst());
            typeOfObject = getTypeOfObjectFromItemType(topLevelItem.itemType());
        }

        ReferenceType tlRef = fiType.addNewTopLevelReference();
        tlRef.addAgency(topLevelItem.agencyId());
        tlRef.addNewID().setStringValue(topLevelItem.identifier());
        tlRef.addVersion(topLevelItem.version());
        tlRef.setTypeOfObject(TypeOfObjectType.Enum.forString(typeOfObject));

        for (Ddi3Response.Ddi3Item item : ddi3Response.items()) {
            if (item.item() != null && !item.item().isEmpty()) {
                try {
                    fiType.addNewFragment().set(FragmentDocument.Factory.parse(item.item()).getFragment());
                } catch (XmlException e) {
                    throw new IllegalArgumentException("Failed to parse fragment XML for item " + item.identifier(), e);
                }
            }
        }

        XmlOptions options = new XmlOptions();
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "ddi");
        prefixes.put(DDI_REUSABLE_NS, "r");
        options.setSaveSuggestedPrefixes(prefixes);

        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + doc.xmlText(options);
    }

    private XmlOptions fragmentXmlOptions(String contentNs) {
        HashMap<String, String> prefixes = new HashMap<>();
        prefixes.put(DDI_INSTANCE_NS, "");
        prefixes.put(contentNs, "");
        prefixes.put(DDI_REUSABLE_NS, "r");
        XmlOptions options = new XmlOptions();
        options.setSaveSuggestedPrefixes(prefixes);
        return options;
    }

    private void populateBasedOnObject(BasedOnObject basedOnObject, Supplier<ReferenceType> refSupplier) {
        if (basedOnObject == null || basedOnObject.basedOnReference() == null) return;
        BasedOnReference ref = basedOnObject.basedOnReference();
        ReferenceType refType = refSupplier.get();
        refType.addAgency(ref.agency());
        refType.addNewID().setStringValue(ref.id());
        refType.addVersion(ref.version());
        refType.setTypeOfObject(TypeOfObjectType.Enum.forString(ref.typeOfObject()));
    }

    private void setContentText(ContentType content, String text) {
        try (XmlCursor cursor = content.newCursor()) {
            cursor.toEndToken();
            cursor.insertChars(text);
        }
    }

    private String getTypeOfObjectFromItemType(String itemType) {
        return itemTypes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(itemType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("PhysicalInstance");
    }
}
