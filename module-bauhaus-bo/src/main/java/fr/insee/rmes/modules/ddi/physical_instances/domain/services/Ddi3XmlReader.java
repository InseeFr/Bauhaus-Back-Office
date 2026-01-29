package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to parse DDI3 XML fragments using DOM API
 */
public class Ddi3XmlReader {

    public static final String IS_UNIVERSALLY_UNIQUE = "isUniversallyUnique";
    public static final String VERSION_DATE = "versionDate";
    public static final String URN = "URN";
    public static final String AGENCY = "Agency";
    public static final String ID = "ID";
    public static final String VERSION = "Version";
    private static final String STRING_ELEMENT = "String";
    private static final String XML_LANG_ATTRIBUTE = "xml:lang";
    private static final String TYPE_OF_OBJECT = "TypeOfObject";
    private final XmlHelper xmlHelper;

    public Ddi3XmlReader() {
        this.xmlHelper = new XmlHelper();
    }

    public Ddi4PhysicalInstance parsePhysicalInstance(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element piElement = (Element) doc.getElementsByTagNameNS("*", "PhysicalInstance").item(0);

        if (piElement == null) {
            throw new IllegalArgumentException("No PhysicalInstance element found");
        }

        return new Ddi4PhysicalInstance(
            xmlHelper.getAttribute(piElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getAttribute(piElement, VERSION_DATE),
            xmlHelper.getElementText(piElement, URN),
            xmlHelper.getElementText(piElement, AGENCY),
            xmlHelper.getElementText(piElement, ID),
            xmlHelper.getElementText(piElement, VERSION),
            parseBasedOnObject(piElement),
            parseCitation(piElement),
            parseDataRelationshipReference(piElement)
        );
    }

    public Ddi4DataRelationship parseDataRelationship(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element drElement = (Element) doc.getElementsByTagNameNS("*", "DataRelationship").item(0);

        if (drElement == null) {
            throw new IllegalArgumentException("No DataRelationship element found");
        }

        return new Ddi4DataRelationship(
            xmlHelper.getAttribute(drElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getAttribute(drElement, VERSION_DATE),
            xmlHelper.getElementText(drElement, URN),
            xmlHelper.getElementText(drElement, AGENCY),
            xmlHelper.getElementText(drElement, ID),
            xmlHelper.getElementText(drElement, VERSION),
            parseBasedOnObject(drElement),
            parseDataRelationshipName(drElement),
            parseLogicalRecord(drElement)
        );
    }

    public Ddi4Variable parseVariable(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element varElement = (Element) doc.getElementsByTagNameNS("*", "Variable").item(0);

        if (varElement == null) {
            throw new IllegalArgumentException("No Variable element found");
        }

        return new Ddi4Variable(
            xmlHelper.getAttribute(varElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getAttribute(varElement, VERSION_DATE),
            xmlHelper.getElementText(varElement, URN),
            xmlHelper.getElementText(varElement, AGENCY),
            xmlHelper.getElementText(varElement, ID),
            xmlHelper.getElementText(varElement, VERSION),
            parseBasedOnObject(varElement),
            parseVariableName(varElement),
            parseLabel(varElement),
            parseDescription(varElement),
            parseVariableRepresentation(varElement),
            xmlHelper.getAttribute(varElement, "isGeographic")
        );
    }

    public Ddi4CodeList parseCodeList(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element clElement = (Element) doc.getElementsByTagNameNS("*", "CodeList").item(0);

        if (clElement == null) {
            throw new IllegalArgumentException("No CodeList element found");
        }

        return new Ddi4CodeList(
            xmlHelper.getAttribute(clElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getAttribute(clElement, VERSION_DATE),
            xmlHelper.getElementText(clElement, URN),
            xmlHelper.getElementText(clElement, AGENCY),
            xmlHelper.getElementText(clElement, ID),
            xmlHelper.getElementText(clElement, VERSION),
            parseLabel(clElement),
            parseCodes(clElement)
        );
    }

    public Ddi4Category parseCategory(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element catElement = (Element) doc.getElementsByTagNameNS("*", "Category").item(0);

        if (catElement == null) {
            throw new IllegalArgumentException("No Category element found");
        }

        return new Ddi4Category(
            xmlHelper.getAttribute(catElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getAttribute(catElement, VERSION_DATE),
            xmlHelper.getElementText(catElement, URN),
            xmlHelper.getElementText(catElement, AGENCY),
            xmlHelper.getElementText(catElement, ID),
            xmlHelper.getElementText(catElement, VERSION),
            parseLabel(catElement)
        );
    }

    // Helper parsing methods

    private Citation parseCitation(Element parent) {
        Element citationElement = xmlHelper.getChildElement(parent, "Citation");
        if (citationElement == null) return null;

        Element titleElement = xmlHelper.getChildElement(citationElement, "Title");
        if (titleElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(titleElement, STRING_ELEMENT);
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, XML_LANG_ATTRIBUTE);
        String text = stringElement.getTextContent();

        return new Citation(new Title(new StringValue(lang, text)));
    }

    private DataRelationshipReference parseDataRelationshipReference(Element parent) {
        Element refElement = xmlHelper.getChildElement(parent, "DataRelationshipReference");
        if (refElement == null) return null;

        return new DataRelationshipReference(
            xmlHelper.getElementText(refElement, AGENCY),
            xmlHelper.getElementText(refElement, ID),
            xmlHelper.getElementText(refElement, VERSION),
            xmlHelper.getElementText(refElement, TYPE_OF_OBJECT)
        );
    }

    private BasedOnObject parseBasedOnObject(Element parent) {
        Element basedOnObjectElement = xmlHelper.getChildElement(parent, "BasedOnObject");
        if (basedOnObjectElement == null) return null;

        Element refElement = xmlHelper.getChildElement(basedOnObjectElement, "BasedOnReference");
        if (refElement == null) return null;

        BasedOnReference basedOnReference = new BasedOnReference(
            xmlHelper.getElementText(refElement, AGENCY),
            xmlHelper.getElementText(refElement, ID),
            xmlHelper.getElementText(refElement, VERSION),
            xmlHelper.getElementText(refElement, TYPE_OF_OBJECT)
        );

        return new BasedOnObject(basedOnReference);
    }

    private DataRelationshipName parseDataRelationshipName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "DataRelationshipName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, STRING_ELEMENT);
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, XML_LANG_ATTRIBUTE);
        String text = stringElement.getTextContent();

        return new DataRelationshipName(new StringValue(lang, text));
    }

    private VariableName parseVariableName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "VariableName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, STRING_ELEMENT);
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, XML_LANG_ATTRIBUTE);
        String text = stringElement.getTextContent();

        return new VariableName(new StringValue(lang, text));
    }

    private Label parseLabel(Element parent) {
        Element labelElement = xmlHelper.getChildElement(parent, "Label");
        if (labelElement == null) return null;

        Element contentElement = xmlHelper.getChildElement(labelElement, "Content");
        if (contentElement == null) return null;

        String lang = xmlHelper.getAttribute(contentElement, XML_LANG_ATTRIBUTE);
        String text = contentElement.getTextContent();

        return new Label(new Content(lang, text));
    }

    private Description parseDescription(Element parent) {
        Element descElement = xmlHelper.getChildElement(parent, "Description");
        if (descElement == null) return null;

        Element contentElement = xmlHelper.getChildElement(descElement, "Content");
        if (contentElement == null) return null;

        String lang = xmlHelper.getAttribute(contentElement, XML_LANG_ATTRIBUTE);
        String text = contentElement.getTextContent();

        return new Description(new Content(lang, text));
    }

    private LogicalRecord parseLogicalRecord(Element parent) {
        Element lrElement = xmlHelper.getChildElement(parent, "LogicalRecord");
        if (lrElement == null) return null;

        return new LogicalRecord(
            xmlHelper.getAttribute(lrElement, IS_UNIVERSALLY_UNIQUE),
            xmlHelper.getElementText(lrElement, URN),
            xmlHelper.getElementText(lrElement, AGENCY),
            xmlHelper.getElementText(lrElement, ID),
            xmlHelper.getElementText(lrElement, VERSION),
            parseLogicalRecordName(lrElement),
            parseVariablesInRecord(lrElement)
        );
    }

    private LogicalRecordName parseLogicalRecordName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "LogicalRecordName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, STRING_ELEMENT);
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, XML_LANG_ATTRIBUTE);
        String text = stringElement.getTextContent();

        return new LogicalRecordName(new StringValue(lang, text));
    }

    private VariablesInRecord parseVariablesInRecord(Element parent) {
        Element virElement = xmlHelper.getChildElement(parent, "VariablesInRecord");
        if (virElement == null) return null;

        NodeList varRefs = virElement.getElementsByTagNameNS("*", "VariableUsedReference");
        if (varRefs.getLength() == 0) return null;

        List<VariableUsedReference> refs = new ArrayList<>();
        for (int i = 0; i < varRefs.getLength(); i++) {
            Element refElement = (Element) varRefs.item(i);
            refs.add(new VariableUsedReference(
                xmlHelper.getElementText(refElement, AGENCY),
                xmlHelper.getElementText(refElement, ID),
                xmlHelper.getElementText(refElement, VERSION),
                xmlHelper.getElementText(refElement, TYPE_OF_OBJECT)
            ));
        }

        return new VariablesInRecord(refs);
    }

    private VariableRepresentation parseVariableRepresentation(Element parent) {
        Element vrElement = xmlHelper.getChildElement(parent, "VariableRepresentation");
        if (vrElement == null) return new VariableRepresentation(null, null, null, null, null);

        String role = xmlHelper.getElementText(vrElement, "VariableRole");
        CodeRepresentation codeRep = parseCodeRepresentation(vrElement);
        NumericRepresentation numRep = parseNumericRepresentation(vrElement);
        DateTimeRepresentation dateTimeRep = parseDateTimeRepresentation(vrElement);
        TextRepresentation textRep = parseTextRepresentation(vrElement);

        return new VariableRepresentation(
            role.isEmpty() ? null : role,
            codeRep,
            numRep,
            dateTimeRep,
            textRep
        );
    }

    private NumericRepresentation parseNumericRepresentation(Element parent) {
        Element numRepElement = xmlHelper.getChildElement(parent, "NumericRepresentation");
        if (numRepElement == null) return null;

        String typeCode = xmlHelper.getElementText(numRepElement, "NumericTypeCode");
        NumberRange numberRange = parseNumberRange(numRepElement);

        return new NumericRepresentation(
            typeCode.isEmpty() ? null : typeCode,
            numberRange
        );
    }

    private NumberRange parseNumberRange(Element parent) {
        Element rangeElement = xmlHelper.getChildElement(parent, "NumberRange");
        if (rangeElement == null) return null;

        RangeValue low = null;
        RangeValue high = null;

        Element lowElement = xmlHelper.getChildElement(rangeElement, "Low");
        if (lowElement != null) {
            String isInclusive = xmlHelper.getAttribute(lowElement, "isInclusive");
            String value = lowElement.getTextContent();
            low = new RangeValue(isInclusive, value);
        }

        Element highElement = xmlHelper.getChildElement(rangeElement, "High");
        if (highElement != null) {
            String isInclusive = xmlHelper.getAttribute(highElement, "isInclusive");
            String value = highElement.getTextContent();
            high = new RangeValue(isInclusive, value);
        }

        return new NumberRange(low, high);
    }

    private CodeRepresentation parseCodeRepresentation(Element parent) {
        Element codeRepElement = xmlHelper.getChildElement(parent, "CodeRepresentation");
        if (codeRepElement == null) return null;

        String blankIsMissing = xmlHelper.getAttribute(codeRepElement, "blankIsMissingValue");
        CodeListReference codeListRef = null;

        Element refElement = xmlHelper.getChildElement(codeRepElement, "CodeListReference");
        if (refElement != null) {
            codeListRef = new CodeListReference(
                xmlHelper.getElementText(refElement, AGENCY),
                xmlHelper.getElementText(refElement, ID),
                xmlHelper.getElementText(refElement, VERSION),
                xmlHelper.getElementText(refElement, TYPE_OF_OBJECT)
            );
        }

        return new CodeRepresentation(blankIsMissing, codeListRef);
    }

    private TextRepresentation parseTextRepresentation(Element parent) {
        Element textRepElement = xmlHelper.getChildElement(parent, "TextRepresentation");
        if (textRepElement == null) return null;

        String blankIsMissing = xmlHelper.getAttribute(textRepElement, "blankIsMissingValue");
        String minLengthStr = xmlHelper.getElementText(textRepElement, "MinLength");
        String maxLengthStr = xmlHelper.getElementText(textRepElement, "MaxLength");
        String regExp = xmlHelper.getElementText(textRepElement, "RegExp");

        Integer minLength = null;
        Integer maxLength = null;

        if (minLengthStr != null && !minLengthStr.isEmpty()) {
            try {
                minLength = Integer.parseInt(minLengthStr);
            } catch (NumberFormatException _) {
                // Ignore invalid numbers
            }
        }

        if (maxLengthStr != null && !maxLengthStr.isEmpty()) {
            try {
                maxLength = Integer.parseInt(maxLengthStr);
            } catch (NumberFormatException _) {
                // Ignore invalid numbers
            }
        }

        return new TextRepresentation(
            maxLength,
            minLength,
            regExp.isEmpty() ? null : regExp,
            blankIsMissing
        );
    }

    private DateTimeRepresentation parseDateTimeRepresentation(Element parent) {
        Element dateTimeRepElement = xmlHelper.getChildElement(parent, "DateTimeRepresentation");
        if (dateTimeRepElement == null) return null;

        String dateTypeCode = xmlHelper.getElementText(dateTimeRepElement, "DateTypeCode");
        String dateFieldFormat = xmlHelper.getElementText(dateTimeRepElement, "DateFieldFormat");

        return new DateTimeRepresentation(
            dateTypeCode.isEmpty() ? null : dateTypeCode,
            dateFieldFormat.isEmpty() ? null : dateFieldFormat
        );
    }

    private List<Code> parseCodes(Element parent) {
        NodeList codeNodes = parent.getElementsByTagNameNS("*", "Code");
        if (codeNodes.getLength() == 0) return null;

        List<Code> codes = new ArrayList<>();
        for (int i = 0; i < codeNodes.getLength(); i++) {
            Element codeElement = (Element) codeNodes.item(i);

            CategoryReference catRef = null;
            Element catRefElement = xmlHelper.getChildElement(codeElement, "CategoryReference");
            if (catRefElement != null) {
                catRef = new CategoryReference(
                    xmlHelper.getElementText(catRefElement, AGENCY),
                    xmlHelper.getElementText(catRefElement, ID),
                    xmlHelper.getElementText(catRefElement, VERSION),
                    xmlHelper.getElementText(catRefElement, TYPE_OF_OBJECT)
                );
            }

            codes.add(new Code(
                xmlHelper.getAttribute(codeElement, IS_UNIVERSALLY_UNIQUE),
                xmlHelper.getElementText(codeElement, URN),
                xmlHelper.getElementText(codeElement, AGENCY),
                xmlHelper.getElementText(codeElement, ID),
                xmlHelper.getElementText(codeElement, VERSION),
                catRef,
                xmlHelper.getElementText(codeElement, "Value")
            ));
        }

        return codes;
    }
}
