package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.instance.FragmentInstanceDocument;
import fr.insee.ddi.lifecycle33.reusable.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Category;
import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.generated.DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;
import fr.insee.rmes.modules.ddi.physical_instances.generated.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.generated.PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Variable;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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

    @SuppressWarnings("unchecked")
    public String buildGroupXml(Group group) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var groupType = doc.addNewFragment().addNewGroup();

        Map<String, Object> ap = group.getAdditionalProperties();
        List<String> seriesIris = (List<String>) value(ap, "seriesIris");
        String typeOfGroup = (String) value(ap, "typeOfGroup");
        Citation citation = (Citation) value(ap, "Citation");
        List<StudyUnitReference> studyUnitReference = (List<StudyUnitReference>) value(ap, "StudyUnitReference");

        groupType.setIsUniversallyUnique(isUniversallyUnique(ap));
        groupType.setVersionDate(versionDate(ap));
        groupType.addNewURN().setStringValue(group.getURN());
        groupType.addAgency(group.getAgency());
        groupType.addNewID().setStringValue(group.getID());
        groupType.addVersion(group.getVersion());

        if (seriesIris != null) {
            for (String seriesIri : seriesIris) {
                var userId = groupType.addNewUserID();
                userId.setTypeOfUserID("URI");
                userId.setStringValue(seriesIri);
            }
        }

        if (typeOfGroup != null && !typeOfGroup.isEmpty()) {
            groupType.addNewTypeOfGroup().setStringValue(typeOfGroup);
        }

        if (citation != null && citation.title() != null) {
            writeString(groupType.addNewCitation().addNewTitle().addNewString(),
                    firstEntry(citation.title()));
        }

        if (studyUnitReference != null) {
            for (StudyUnitReference suRef : studyUnitReference) {
                ReferenceType refType = groupType.addNewStudyUnitReference();
                refType.addAgency(suRef.agency());
                refType.addNewID().setStringValue(suRef.id());
                refType.addVersion(suRef.version());
                refType.setTypeOfObject(TypeOfObjectType.Enum.forString(suRef.typeOfObject()));
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_GROUP_NS));
    }

    @SuppressWarnings("unchecked")
    public String buildStudyUnitXml(StudyUnit studyUnit) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var suType = doc.addNewFragment().addNewStudyUnit();

        Map<String, Object> ap = studyUnit.getAdditionalProperties();
        String operationIri = (String) value(ap, "operationIri");
        Citation citation = (Citation) value(ap, "Citation");
        List<DDIReference> physicalInstanceReferences =
                (List<DDIReference>) value(ap, "physicalInstanceReferences");

        suType.setIsUniversallyUnique(isUniversallyUnique(ap));
        suType.setVersionDate(versionDate(ap));
        suType.addNewURN().setStringValue(studyUnit.getURN());
        suType.addAgency(studyUnit.getAgency());
        suType.addNewID().setStringValue(studyUnit.getID());
        suType.addVersion(studyUnit.getVersion());

        if (operationIri != null && !operationIri.isEmpty()) {
            var userId = suType.addNewUserID();
            userId.setTypeOfUserID("URI");
            userId.setStringValue(operationIri);
        }

        if (citation != null && citation.title() != null) {
            writeString(suType.addNewCitation().addNewTitle().addNewString(),
                    firstEntry(citation.title()));
        }

        if (physicalInstanceReferences != null) {
            for (DDIReference piRef : physicalInstanceReferences) {
                ReferenceType refType = suType.addNewPhysicalInstanceReference();
                refType.addAgency(piRef.agency());
                refType.addNewID().setStringValue(piRef.id());
                refType.addVersion(piRef.version());
                refType.setTypeOfObject(TypeOfObjectType.Enum.forString("PhysicalInstance"));
            }
        }

        return doc.xmlText(fragmentXmlOptions(DDI_STUDY_UNIT_NS));
    }

    public String buildPhysicalInstanceXml(PhysicalInstance pi) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var piType = doc.addNewFragment().addNewPhysicalInstance();

        Map<String, Object> ap = pi.getAdditionalProperties();
        Citation citation = (Citation) value(ap, "Citation");
        DataRelationshipReference dataRelationshipReference =
                (DataRelationshipReference) value(ap, "DataRelationshipReference");

        piType.setIsUniversallyUnique(isUniversallyUnique(ap));
        piType.setVersionDate(versionDate(ap));
        piType.addNewURN().setStringValue(pi.getURN());
        piType.addAgency(pi.getAgency());
        piType.addNewID().setStringValue(pi.getID());
        piType.addVersion(pi.getVersion());

        populateBasedOnObject((BasedOnObject) value(ap, "BasedOnObject"),
                () -> piType.addNewBasedOnObject().addNewBasedOnReference());

        if (citation != null && citation.title() != null) {
            writeString(piType.addNewCitation().addNewTitle().addNewString(),
                    firstEntry(citation.title()));
        }

        if (dataRelationshipReference != null) {
            ReferenceType refType = piType.addNewDataRelationshipReference();
            refType.addAgency(dataRelationshipReference.agency());
            refType.addNewID().setStringValue(dataRelationshipReference.id());
            refType.addVersion(dataRelationshipReference.version());
            refType.setTypeOfObject(TypeOfObjectType.Enum.forString(dataRelationshipReference.typeOfObject()));
        }

        return doc.xmlText(fragmentXmlOptions(DDI_PHYSICAL_INSTANCE_NS));
    }

    public String buildDataRelationshipXml(DataRelationship dr) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var drType = doc.addNewFragment().addNewDataRelationship();

        Map<String, Object> ap = dr.getAdditionalProperties();
        drType.setIsUniversallyUnique(isUniversallyUnique(ap));
        drType.setVersionDate(versionDate(ap));
        drType.addNewURN().setStringValue(dr.getURN());
        drType.addAgency(dr.getAgency());
        drType.addNewID().setStringValue(dr.getID());
        drType.addVersion(dr.getVersion());

        populateBasedOnObject((BasedOnObject) value(ap, "BasedOnObject"),
                () -> drType.addNewBasedOnObject().addNewBasedOnReference());

        if (dr.getLabel() != null && !dr.getLabel().isEmpty()) {
            LangString firstLabel = firstEntry(dr.getLabel());
            writeString(drType.addNewDataRelationshipName().addNewString(), firstLabel);
            writeContent(drType.addNewLabel().addNewContent(), firstLabel);
        }

        LogicalRecord lr = (LogicalRecord) value(ap, "LogicalRecord");
        if (lr != null) {
            var lrType = drType.addNewLogicalRecord();
            lrType.setIsUniversallyUnique(Boolean.parseBoolean(lr.isUniversallyUnique()));
            lrType.addNewURN().setStringValue(lr.urn());
            lrType.addAgency(lr.agency());
            lrType.addNewID().setStringValue(lr.id());
            lrType.addVersion(lr.version());

            if (lr.label() != null && !lr.label().isEmpty()) {
                LangString firstLabel = firstEntry(lr.label());
                writeString(lrType.addNewLogicalRecordName().addNewString(), firstLabel);
                writeContent(lrType.addNewLabel().addNewContent(), firstLabel);
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

    public String buildVariableXml(Variable var) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var varType = doc.addNewFragment().addNewVariable();

        Map<String, Object> ap = var.getAdditionalProperties();
        String isGeographic = (String) value(ap, "@isGeographic");
        BasedOnObject basedOn = (BasedOnObject) value(ap, "BasedOnObject");
        VariableRepresentation representation = (VariableRepresentation) value(ap, "VariableRepresentation");

        varType.setIsUniversallyUnique(isUniversallyUnique(ap));
        varType.setVersionDate(versionDate(ap));
        if (isGeographic != null && !isGeographic.isEmpty()) {
            varType.setIsGeographic(Boolean.parseBoolean(isGeographic));
        }
        varType.addNewURN().setStringValue(var.getURN());
        varType.addAgency(var.getAgency());
        varType.addNewID().setStringValue(var.getID());
        varType.addVersion(var.getVersion());

        populateBasedOnObject(basedOn, () -> varType.addNewBasedOnObject().addNewBasedOnReference());

        if (var.getVariableName() != null && !var.getVariableName().isEmpty()) {
            writeString(varType.addNewVariableName().addNewString(),
                    firstEntry(var.getVariableName()));
        }

        if (var.getLabel() != null && !var.getLabel().isEmpty()) {
            writeContent(varType.addNewLabel().addNewContent(),
                    firstEntry(var.getLabel()));
        }

        if (var.getDescription() != null && !var.getDescription().isEmpty()) {
            writeContent(varType.addNewDescription().addNewContent(),
                    firstEntry(var.getDescription()));
        }

        var varRepType = varType.addNewVariableRepresentation();

        if (representation != null) {
            if (representation.variableRole() != null) {
                varRepType.addNewVariableRole().setStringValue(representation.variableRole());
            }

            if (representation.numericRepresentation() != null) {
                NumericRepresentation numRepDomain = representation.numericRepresentation();
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

            if (representation.codeRepresentation() != null) {
                CodeRepresentation codeRepDomain = representation.codeRepresentation();
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

            if (representation.dateTimeRepresentation() != null) {
                DateTimeRepresentation dateTimeDomain = representation.dateTimeRepresentation();
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

            if (representation.textRepresentation() != null) {
                TextRepresentation textDomain = representation.textRepresentation();
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

    public String buildCodeListXml(CodeList cl) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var clType = doc.addNewFragment().addNewCodeList();

        clType.setIsUniversallyUnique(isUniversallyUnique(cl.getAdditionalProperties()));
        clType.setVersionDate(versionDate(cl.getAdditionalProperties()));
        clType.addNewURN().setStringValue(cl.getURN());
        clType.addAgency(cl.getAgency());
        clType.addNewID().setStringValue(cl.getID());
        clType.addVersion(cl.getVersion());

        if (cl.getLabel() != null && !cl.getLabel().isEmpty()) {
            writeContent(clType.addNewLabel().addNewContent(),
                    firstEntry(cl.getLabel()));
        }

        List<Code> codes = codes(cl.getAdditionalProperties());
        if (codes != null && !codes.isEmpty()) {
            for (Code code : codes) {
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

    public String buildCategoryXml(Category cat) throws XMLStreamException {
        FragmentDocument doc = FragmentDocument.Factory.newInstance();
        var catType = doc.addNewFragment().addNewCategory();

        catType.setIsUniversallyUnique(isUniversallyUnique(cat.getAdditionalProperties()));
        catType.setVersionDate(versionDate(cat.getAdditionalProperties()));
        catType.setIsMissing(false);
        catType.addNewURN().setStringValue(cat.getURN());
        catType.addAgency(cat.getAgency());
        catType.addNewID().setStringValue(cat.getID());
        catType.addVersion(cat.getVersion());

        if (cat.getLabel() != null && !cat.getLabel().isEmpty()) {
            writeContent(catType.addNewLabel().addNewContent(),
                    firstEntry(cat.getLabel()));
        }

        return doc.xmlText(fragmentXmlOptions(DDI_LOGICAL_PRODUCT_NS));
    }

    /**
     * Relit l'attribut DDI3 {@code @isUniversallyUnique} porte par la Map additionalProperties
     * d'un POJO genere (cf. {@code Ddi3XmlReader#putDdi3Attributes}). Defaut {@code true}.
     */
    private static boolean isUniversallyUnique(Map<String, Object> additionalProperties) {
        Object value = additionalProperties == null ? null : additionalProperties.get("@isUniversallyUnique");
        return value == null || Boolean.parseBoolean(value.toString());
    }

    /** Relit l'attribut DDI3 {@code @versionDate} porte par la Map additionalProperties. */
    private static String versionDate(Map<String, Object> additionalProperties) {
        Object value = additionalProperties == null ? null : additionalProperties.get("@versionDate");
        return value == null ? null : value.toString();
    }

    /**
     * Relit la liste de {@link Code} (forme historique conservee) portee par la Map
     * additionalProperties d'une {@link CodeList} generee, sous la cle JSON "Code".
     */
    @SuppressWarnings("unchecked")
    private static List<Code> codes(Map<String, Object> additionalProperties) {
        Object value = additionalProperties == null ? null : additionalProperties.get("Code");
        return (List<Code>) value;
    }

    /** Lit une valeur quelconque portee par la Map additionalProperties (ex. record historique). */
    private static Object value(Map<String, Object> additionalProperties, String key) {
        return additionalProperties == null ? null : additionalProperties.get(key);
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

    private static LangString firstEntry(List<LangString> entries) {
        return entries.get(0);
    }

    private static void writeString(StringType target, LangString value) {
        target.setLang(value.getAtLanguage());
        target.setStringValue(value.getAtValue());
    }

    private void writeContent(ContentType target, LangString value) {
        target.setLang(value.getAtLanguage());
        setContentText(target, value.getAtValue());
    }

    private String getTypeOfObjectFromItemType(String itemType) {
        return itemTypes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(itemType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("PhysicalInstance");
    }
}
