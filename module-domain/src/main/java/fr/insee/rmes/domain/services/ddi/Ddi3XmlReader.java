package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to parse DDI3 XML fragments using DOM API
 */
public class Ddi3XmlReader {

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
            xmlHelper.getAttribute(piElement, "isUniversallyUnique"),
            xmlHelper.getAttribute(piElement, "versionDate"),
            xmlHelper.getElementText(piElement, "URN"),
            xmlHelper.getElementText(piElement, "Agency"),
            xmlHelper.getElementText(piElement, "ID"),
            xmlHelper.getElementText(piElement, "Version"),
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
            xmlHelper.getAttribute(drElement, "isUniversallyUnique"),
            xmlHelper.getAttribute(drElement, "versionDate"),
            xmlHelper.getElementText(drElement, "URN"),
            xmlHelper.getElementText(drElement, "Agency"),
            xmlHelper.getElementText(drElement, "ID"),
            xmlHelper.getElementText(drElement, "Version"),
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
            xmlHelper.getAttribute(varElement, "isUniversallyUnique"),
            xmlHelper.getAttribute(varElement, "versionDate"),
            xmlHelper.getElementText(varElement, "URN"),
            xmlHelper.getElementText(varElement, "Agency"),
            xmlHelper.getElementText(varElement, "ID"),
            xmlHelper.getElementText(varElement, "Version"),
            parseVariableName(varElement),
            parseLabel(varElement),
            parseDescription(varElement),
            parseVariableRepresentation(varElement),
            null
        );
    }

    public Ddi4CodeList parseCodeList(String xmlFragment) throws Exception {
        Document doc = xmlHelper.parseXml(xmlFragment);
        Element clElement = (Element) doc.getElementsByTagNameNS("*", "CodeList").item(0);

        if (clElement == null) {
            throw new IllegalArgumentException("No CodeList element found");
        }

        return new Ddi4CodeList(
            xmlHelper.getAttribute(clElement, "isUniversallyUnique"),
            xmlHelper.getAttribute(clElement, "versionDate"),
            xmlHelper.getElementText(clElement, "URN"),
            xmlHelper.getElementText(clElement, "Agency"),
            xmlHelper.getElementText(clElement, "ID"),
            xmlHelper.getElementText(clElement, "Version"),
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
            xmlHelper.getAttribute(catElement, "isUniversallyUnique"),
            xmlHelper.getAttribute(catElement, "versionDate"),
            xmlHelper.getElementText(catElement, "URN"),
            xmlHelper.getElementText(catElement, "Agency"),
            xmlHelper.getElementText(catElement, "ID"),
            xmlHelper.getElementText(catElement, "Version"),
            parseLabel(catElement)
        );
    }

    // Helper parsing methods

    private Citation parseCitation(Element parent) {
        Element citationElement = xmlHelper.getChildElement(parent, "Citation");
        if (citationElement == null) return null;

        Element titleElement = xmlHelper.getChildElement(citationElement, "Title");
        if (titleElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(titleElement, "String");
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, "xml:lang");
        String text = stringElement.getTextContent();

        return new Citation(new Title(new StringValue(lang, text)));
    }

    private DataRelationshipReference parseDataRelationshipReference(Element parent) {
        Element refElement = xmlHelper.getChildElement(parent, "DataRelationshipReference");
        if (refElement == null) return null;

        return new DataRelationshipReference(
            xmlHelper.getElementText(refElement, "Agency"),
            xmlHelper.getElementText(refElement, "ID"),
            xmlHelper.getElementText(refElement, "Version"),
            xmlHelper.getElementText(refElement, "TypeOfObject")
        );
    }

    private DataRelationshipName parseDataRelationshipName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "DataRelationshipName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, "String");
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, "xml:lang");
        String text = stringElement.getTextContent();

        return new DataRelationshipName(new StringValue(lang, text));
    }

    private VariableName parseVariableName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "VariableName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, "String");
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, "xml:lang");
        String text = stringElement.getTextContent();

        return new VariableName(new StringValue(lang, text));
    }

    private Label parseLabel(Element parent) {
        Element labelElement = xmlHelper.getChildElement(parent, "Label");
        if (labelElement == null) return null;

        Element contentElement = xmlHelper.getChildElement(labelElement, "Content");
        if (contentElement == null) return null;

        String lang = xmlHelper.getAttribute(contentElement, "xml:lang");
        String text = contentElement.getTextContent();

        return new Label(new Content(lang, text));
    }

    private Description parseDescription(Element parent) {
        Element descElement = xmlHelper.getChildElement(parent, "Description");
        if (descElement == null) return null;

        Element contentElement = xmlHelper.getChildElement(descElement, "Content");
        if (contentElement == null) return null;

        String lang = xmlHelper.getAttribute(contentElement, "xml:lang");
        String text = contentElement.getTextContent();

        return new Description(new Content(lang, text));
    }

    private LogicalRecord parseLogicalRecord(Element parent) {
        Element lrElement = xmlHelper.getChildElement(parent, "LogicalRecord");
        if (lrElement == null) return null;

        return new LogicalRecord(
            xmlHelper.getAttribute(lrElement, "isUniversallyUnique"),
            xmlHelper.getElementText(lrElement, "URN"),
            xmlHelper.getElementText(lrElement, "Agency"),
            xmlHelper.getElementText(lrElement, "ID"),
            xmlHelper.getElementText(lrElement, "Version"),
            parseLogicalRecordName(lrElement),
            parseVariablesInRecord(lrElement)
        );
    }

    private LogicalRecordName parseLogicalRecordName(Element parent) {
        Element nameElement = xmlHelper.getChildElement(parent, "LogicalRecordName");
        if (nameElement == null) return null;

        Element stringElement = xmlHelper.getChildElement(nameElement, "String");
        if (stringElement == null) return null;

        String lang = xmlHelper.getAttribute(stringElement, "xml:lang");
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
                xmlHelper.getElementText(refElement, "Agency"),
                xmlHelper.getElementText(refElement, "ID"),
                xmlHelper.getElementText(refElement, "Version"),
                xmlHelper.getElementText(refElement, "TypeOfObject")
            ));
        }

        return new VariablesInRecord(refs);
    }

    private VariableRepresentation parseVariableRepresentation(Element parent) {
        Element vrElement = xmlHelper.getChildElement(parent, "VariableRepresentation");
        if (vrElement == null) return new VariableRepresentation(null, null, null);

        String role = xmlHelper.getElementText(vrElement, "VariableRole");
        CodeRepresentation codeRep = parseCodeRepresentation(vrElement);
        NumericRepresentation numRep = parseNumericRepresentation(vrElement);

        return new VariableRepresentation(
            role.isEmpty() ? null : role,
            codeRep,
            numRep
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
                xmlHelper.getElementText(refElement, "Agency"),
                xmlHelper.getElementText(refElement, "ID"),
                xmlHelper.getElementText(refElement, "Version"),
                xmlHelper.getElementText(refElement, "TypeOfObject")
            );
        }

        return new CodeRepresentation(blankIsMissing, codeListRef);
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
                    xmlHelper.getElementText(catRefElement, "Agency"),
                    xmlHelper.getElementText(catRefElement, "ID"),
                    xmlHelper.getElementText(catRefElement, "Version"),
                    xmlHelper.getElementText(catRefElement, "TypeOfObject")
                );
            }

            codes.add(new Code(
                xmlHelper.getAttribute(codeElement, "isUniversallyUnique"),
                xmlHelper.getElementText(codeElement, "URN"),
                xmlHelper.getElementText(codeElement, "Agency"),
                xmlHelper.getElementText(codeElement, "ID"),
                xmlHelper.getElementText(codeElement, "Version"),
                catRef,
                xmlHelper.getElementText(codeElement, "Value")
            ));
        }

        return codes;
    }
}
