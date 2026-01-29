package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/**
 * Helper class to generate DDI3 XML fragments using StAX XMLStreamWriter
 * This is much cleaner and safer than StringBuilder concatenation
 */
public class Ddi3XmlWriter {

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";
    private static final String DDI_LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";
    public static final String FRAGMENT = "Fragment";
    public static final String PHYSICAL_INSTANCE = "PhysicalInstance";
    public static final String IS_UNIVERSALLY_UNIQUE = "isUniversallyUnique";
    public static final String VERSION_DATE = "versionDate";
    public static final String URN = "URN";
    public static final String AGENCY = "Agency";
    public static final String ID = "ID";
    public static final String VERSION = "Version";
    public static final String XML_LANG = "xml:lang";
    private static final String STRING_ELEMENT = "String";
    private static final String TYPE_OF_OBJECT = "TypeOfObject";
    private static final String LABEL = "Label";
    private static final String CONTENT = "Content";

    private final XMLOutputFactory xmlOutputFactory;

    public Ddi3XmlWriter() {
        this.xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    public String buildPhysicalInstanceXml(Ddi4PhysicalInstance pi) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        // Start Fragment
        writer.writeStartElement(FRAGMENT);
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        // Start PhysicalInstance
        writer.writeStartElement(PHYSICAL_INSTANCE);
        writer.writeDefaultNamespace(DDI_PHYSICAL_INSTANCE_NS);
        writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, pi.isUniversallyUnique());
        writer.writeAttribute(VERSION_DATE, pi.versionDate());

        // Write basic elements
        writeElement(writer, DDI_REUSABLE_NS, URN, pi.urn());
        writeElement(writer, DDI_REUSABLE_NS, AGENCY, pi.agency());
        writeElement(writer, DDI_REUSABLE_NS, ID, pi.id());
        writeElement(writer, DDI_REUSABLE_NS, VERSION, pi.version());

        // Write BasedOnObject if present
        writeBasedOnObject(writer, pi.basedOnObject());

        // Write Citation if present
        if (pi.citation() != null && pi.citation().title() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Citation");
            writer.writeStartElement(DDI_REUSABLE_NS, "Title");
            writer.writeStartElement(DDI_REUSABLE_NS, STRING_ELEMENT);
            writer.writeAttribute(XML_LANG, pi.citation().title().string().xmlLang());
            writer.writeCharacters(pi.citation().title().string().text());
            writer.writeEndElement(); // String
            writer.writeEndElement(); // Title
            writer.writeEndElement(); // Citation
        }

        // Write DataRelationshipReference if present
        if (pi.dataRelationshipReference() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "DataRelationshipReference");
            writeElement(writer, DDI_REUSABLE_NS, AGENCY, pi.dataRelationshipReference().agency());
            writeElement(writer, DDI_REUSABLE_NS, ID, pi.dataRelationshipReference().id());
            writeElement(writer, DDI_REUSABLE_NS, VERSION, pi.dataRelationshipReference().version());
            writeElement(writer, DDI_REUSABLE_NS, TYPE_OF_OBJECT, pi.dataRelationshipReference().typeOfObject());
            writer.writeEndElement(); // DataRelationshipReference
        }

        writer.writeEndElement(); // PhysicalInstance
        writer.writeEndElement(); // Fragment

        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    public String buildDataRelationshipXml(Ddi4DataRelationship dr) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        // Start Fragment
        writer.writeStartElement(FRAGMENT);
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        // Start DataRelationship
        writer.writeStartElement("DataRelationship");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, dr.isUniversallyUnique());
        writer.writeAttribute(VERSION_DATE, dr.versionDate());

        writeElement(writer, DDI_REUSABLE_NS, URN, dr.urn());
        writeElement(writer, DDI_REUSABLE_NS, AGENCY, dr.agency());
        writeElement(writer, DDI_REUSABLE_NS, ID, dr.id());
        writeElement(writer, DDI_REUSABLE_NS, VERSION, dr.version());

        // Write BasedOnObject if present
        writeBasedOnObject(writer, dr.basedOnObject());

        // Write DataRelationshipName if present
        if (dr.dataRelationshipName() != null) {
            writer.writeStartElement("DataRelationshipName");
            writer.writeStartElement(DDI_REUSABLE_NS, STRING_ELEMENT);
            writer.writeAttribute(XML_LANG, dr.dataRelationshipName().string().xmlLang());
            writer.writeCharacters(dr.dataRelationshipName().string().text());
            writer.writeEndElement(); // String
            writer.writeEndElement(); // DataRelationshipName
        }

        // Write LogicalRecord if present
        if (dr.logicalRecord() != null) {
            LogicalRecord lr = dr.logicalRecord();
            writer.writeStartElement("LogicalRecord");
            writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, lr.isUniversallyUnique());

            writeElement(writer, DDI_REUSABLE_NS, URN, lr.urn());
            writeElement(writer, DDI_REUSABLE_NS, AGENCY, lr.agency());
            writeElement(writer, DDI_REUSABLE_NS, ID, lr.id());
            writeElement(writer, DDI_REUSABLE_NS, VERSION, lr.version());

            if (lr.logicalRecordName() != null) {
                writer.writeStartElement("LogicalRecordName");
                writer.writeStartElement(DDI_REUSABLE_NS, STRING_ELEMENT);
                writer.writeAttribute(XML_LANG, lr.logicalRecordName().string().xmlLang());
                writer.writeCharacters(lr.logicalRecordName().string().text());
                writer.writeEndElement(); // String
                writer.writeEndElement(); // LogicalRecordName
            }

            if (lr.variablesInRecord() != null && lr.variablesInRecord().variableUsedReference() != null) {
                writer.writeStartElement("VariablesInRecord");
                for (VariableUsedReference ref : lr.variablesInRecord().variableUsedReference()) {
                    writer.writeStartElement("VariableUsedReference");
                    writeElement(writer, DDI_REUSABLE_NS, AGENCY, ref.agency());
                    writeElement(writer, DDI_REUSABLE_NS, ID, ref.id());
                    writeElement(writer, DDI_REUSABLE_NS, VERSION, ref.version());
                    writeElement(writer, DDI_REUSABLE_NS, TYPE_OF_OBJECT, ref.typeOfObject());
                    writer.writeEndElement(); // VariableUsedReference
                }
                writer.writeEndElement(); // VariablesInRecord
            }

            writer.writeEndElement(); // LogicalRecord
        }

        writer.writeEndElement(); // DataRelationship
        writer.writeEndElement(); // Fragment

        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    public String buildVariableXml(Ddi4Variable var) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        writer.writeStartElement(FRAGMENT);
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("Variable");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, var.isUniversallyUnique());
        writer.writeAttribute(VERSION_DATE, var.versionDate());
        if (var.isGeographic() != null && !var.isGeographic().isEmpty()) {
            writer.writeAttribute("isGeographic", var.isGeographic());
        }

        writeElement(writer, DDI_REUSABLE_NS, URN, var.urn());
        writeElement(writer, DDI_REUSABLE_NS, AGENCY, var.agency());
        writeElement(writer, DDI_REUSABLE_NS, ID, var.id());
        writeElement(writer, DDI_REUSABLE_NS, VERSION, var.version());

        // Write BasedOnObject if present
        writeBasedOnObject(writer, var.basedOnObject());

        if (var.variableName() != null) {
            writer.writeStartElement("VariableName");
            writer.writeStartElement(DDI_REUSABLE_NS, STRING_ELEMENT);
            writer.writeAttribute(XML_LANG, var.variableName().string().xmlLang());
            writer.writeCharacters(var.variableName().string().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (var.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, LABEL);
            writer.writeStartElement(DDI_REUSABLE_NS, CONTENT);
            writer.writeAttribute(XML_LANG, var.label().content().xmlLang());
            writer.writeCharacters(var.label().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (var.description() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Description");
            writer.writeStartElement(DDI_REUSABLE_NS, CONTENT);
            writer.writeAttribute(XML_LANG, var.description().content().xmlLang());
            writer.writeCharacters(var.description().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        writer.writeStartElement("VariableRepresentation");

        if (var.variableRepresentation() != null) {
            if (var.variableRepresentation().variableRole() != null) {
                writeElement(writer, null, "VariableRole", var.variableRepresentation().variableRole());
            }

            if (var.variableRepresentation().numericRepresentation() != null) {
                NumericRepresentation numRep = var.variableRepresentation().numericRepresentation();
                writer.writeStartElement(DDI_REUSABLE_NS, "NumericRepresentation");
                writer.writeAttribute("blankIsMissingValue", "false");

                if (numRep.numberRange() != null) {
                    writer.writeStartElement(DDI_REUSABLE_NS, "NumberRange");
                    if (numRep.numberRange().low() != null) {
                        writer.writeStartElement(DDI_REUSABLE_NS, "Low");
                        writer.writeAttribute("isInclusive", numRep.numberRange().low().isInclusive());
                        writer.writeCharacters(numRep.numberRange().low().text());
                        writer.writeEndElement();
                    }
                    if (numRep.numberRange().high() != null) {
                        writer.writeStartElement(DDI_REUSABLE_NS, "High");
                        writer.writeAttribute("isInclusive", numRep.numberRange().high().isInclusive());
                        writer.writeCharacters(numRep.numberRange().high().text());
                        writer.writeEndElement();
                    }
                    writer.writeEndElement(); // NumberRange
                }

                if (numRep.numericTypeCode() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "NumericTypeCode", numRep.numericTypeCode());
                }

                writer.writeEndElement(); // NumericRepresentation
            }

            if (var.variableRepresentation().codeRepresentation() != null) {
                CodeRepresentation codeRep = var.variableRepresentation().codeRepresentation();
                writer.writeStartElement(DDI_REUSABLE_NS, "CodeRepresentation");
                writer.writeAttribute("blankIsMissingValue", codeRep.blankIsMissingValue());

                if (codeRep.codeListReference() != null) {
                    writer.writeStartElement(DDI_REUSABLE_NS, "CodeListReference");
                    writeElement(writer, DDI_REUSABLE_NS, AGENCY, codeRep.codeListReference().agency());
                    writeElement(writer, DDI_REUSABLE_NS, ID, codeRep.codeListReference().id());
                    writeElement(writer, DDI_REUSABLE_NS, VERSION, codeRep.codeListReference().version());
                    writeElement(writer, DDI_REUSABLE_NS, TYPE_OF_OBJECT, codeRep.codeListReference().typeOfObject());
                    writer.writeEndElement(); // CodeListReference
                }

                writer.writeEndElement(); // CodeRepresentation
            }

            if (var.variableRepresentation().dateTimeRepresentation() != null) {
                DateTimeRepresentation dateTimeRep = var.variableRepresentation().dateTimeRepresentation();
                writer.writeStartElement(DDI_REUSABLE_NS, "DateTimeRepresentation");

                if (dateTimeRep.dateTypeCode() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "DateTypeCode", dateTimeRep.dateTypeCode());
                }

                if (dateTimeRep.dateFieldFormat() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "DateFieldFormat", dateTimeRep.dateFieldFormat());
                }

                writer.writeEndElement(); // DateTimeRepresentation
            }

            if (var.variableRepresentation().textRepresentation() != null) {
                TextRepresentation textRep = var.variableRepresentation().textRepresentation();
                writer.writeStartElement(DDI_REUSABLE_NS, "TextRepresentation");

                if (textRep.blankIsMissingValue() != null) {
                    writer.writeAttribute("blankIsMissingValue", textRep.blankIsMissingValue());
                }

                if (textRep.maxLength() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "MaxLength", String.valueOf(textRep.maxLength()));
                }

                if (textRep.minLength() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "MinLength", String.valueOf(textRep.minLength()));
                }

                if (textRep.regExp() != null) {
                    writeElement(writer, DDI_REUSABLE_NS, "RegExp", textRep.regExp());
                }

                writer.writeEndElement(); // TextRepresentation
            }
        }

        writer.writeEndElement(); // VariableRepresentation
        writer.writeEndElement(); // Variable
        writer.writeEndElement(); // Fragment

        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    public String buildCodeListXml(Ddi4CodeList cl) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        writer.writeStartElement(FRAGMENT);
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("CodeList");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, cl.isUniversallyUnique());
        writer.writeAttribute(VERSION_DATE, cl.versionDate());

        writeElement(writer, DDI_REUSABLE_NS, URN, cl.urn());
        writeElement(writer, DDI_REUSABLE_NS, AGENCY, cl.agency());
        writeElement(writer, DDI_REUSABLE_NS, ID, cl.id());
        writeElement(writer, DDI_REUSABLE_NS, VERSION, cl.version());

        if (cl.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, LABEL);
            writer.writeStartElement(DDI_REUSABLE_NS, CONTENT);
            writer.writeAttribute(XML_LANG, cl.label().content().xmlLang());
            writer.writeCharacters(cl.label().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (cl.code() != null && !cl.code().isEmpty()) {
            for (Code code : cl.code()) {
                writer.writeStartElement("Code");
                writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, code.isUniversallyUnique());

                writeElement(writer, DDI_REUSABLE_NS, URN, code.urn());
                writeElement(writer, DDI_REUSABLE_NS, AGENCY, code.agency());
                writeElement(writer, DDI_REUSABLE_NS, ID, code.id());
                writeElement(writer, DDI_REUSABLE_NS, VERSION, code.version());

                if (code.categoryReference() != null) {
                    writer.writeStartElement(DDI_REUSABLE_NS, "CategoryReference");
                    writeElement(writer, DDI_REUSABLE_NS, AGENCY, code.categoryReference().agency());
                    writeElement(writer, DDI_REUSABLE_NS, ID, code.categoryReference().id());
                    writeElement(writer, DDI_REUSABLE_NS, VERSION, code.categoryReference().version());
                    writeElement(writer, DDI_REUSABLE_NS, TYPE_OF_OBJECT, code.categoryReference().typeOfObject());
                    writer.writeEndElement(); // CategoryReference
                }

                writeElement(writer, DDI_REUSABLE_NS, "Value", code.value());
                writer.writeEndElement(); // Code
            }
        }

        writer.writeEndElement(); // CodeList
        writer.writeEndElement(); // Fragment

        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    public String buildCategoryXml(Ddi4Category cat) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        writer.writeStartElement(FRAGMENT);
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("Category");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute(IS_UNIVERSALLY_UNIQUE, cat.isUniversallyUnique());
        writer.writeAttribute(VERSION_DATE, cat.versionDate());
        writer.writeAttribute("isMissing", "false");

        writeElement(writer, DDI_REUSABLE_NS, URN, cat.urn());
        writeElement(writer, DDI_REUSABLE_NS, AGENCY, cat.agency());
        writeElement(writer, DDI_REUSABLE_NS, ID, cat.id());
        writeElement(writer, DDI_REUSABLE_NS, VERSION, cat.version());

        if (cat.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, LABEL);
            writer.writeStartElement(DDI_REUSABLE_NS, CONTENT);
            writer.writeAttribute(XML_LANG, cat.label().content().xmlLang());
            writer.writeCharacters(cat.label().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        writer.writeEndElement(); // Category
        writer.writeEndElement(); // Fragment

        writer.flush();
        writer.close();

        return stringWriter.toString();
    }

    private void writeElement(XMLStreamWriter writer, String namespace, String localName, String value) throws XMLStreamException {
        if (value != null && !value.isEmpty()) {
            if (namespace != null) {
                writer.writeStartElement(namespace, localName);
            } else {
                writer.writeStartElement(localName);
            }
            writer.writeCharacters(value);
            writer.writeEndElement();
        }
    }

    private void writeBasedOnObject(XMLStreamWriter writer, BasedOnObject basedOnObject) throws XMLStreamException {
        if (basedOnObject != null && basedOnObject.basedOnReference() != null) {
            BasedOnReference ref = basedOnObject.basedOnReference();
            writer.writeStartElement(DDI_REUSABLE_NS, "BasedOnObject");
            writer.writeStartElement(DDI_REUSABLE_NS, "BasedOnReference");
            writeElement(writer, DDI_REUSABLE_NS, AGENCY, ref.agency());
            writeElement(writer, DDI_REUSABLE_NS, ID, ref.id());
            writeElement(writer, DDI_REUSABLE_NS, VERSION, ref.version());
            writeElement(writer, DDI_REUSABLE_NS, TYPE_OF_OBJECT, ref.typeOfObject());
            writer.writeEndElement(); // BasedOnReference
            writer.writeEndElement(); // BasedOnObject
        }
    }

    /**
     * Builds a complete DDI 3.3 FragmentInstance XML document from a Ddi3Response.
     * This method creates the root FragmentInstance element, TopLevelReference, and all Fragment elements.
     *
     * @param ddi3Response The DDI3 response containing all items to include
     * @return Complete XML document as String with XML declaration
     */
    public String buildFragmentInstanceDocument(Ddi3Response ddi3Response) {
        return buildFragmentInstanceDocument(ddi3Response, null);
    }

    /**
     * Builds a complete DDI 3.3 FragmentInstance XML document from a Ddi3Response.
     * This method creates the root FragmentInstance element, TopLevelReference, and all Fragment elements.
     *
     * @param ddi3Response The DDI3 response containing all items to include
     * @param topLevelReference Optional TopLevelReference to use; if null, will determine from items
     * @return Complete XML document as String with XML declaration
     */
    public String buildFragmentInstanceDocument(Ddi3Response ddi3Response, TopLevelReference topLevelReference) {
        if (ddi3Response == null || ddi3Response.items() == null || ddi3Response.items().isEmpty()) {
            throw new IllegalArgumentException("Ddi3Response must contain at least one item");
        }

        StringBuilder xml = new StringBuilder();

        // Write XML declaration
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        // Start FragmentInstance root element
        xml.append("<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n");

        // Determine which item to use for TopLevelReference
        Ddi3Response.Ddi3Item topLevelItem;
        String typeOfObject;

        if (topLevelReference != null) {
            // Use the provided topLevelReference to find the matching item
            final String tlrId = topLevelReference.id();
            final String tlrAgency = topLevelReference.agency();

            topLevelItem = ddi3Response.items().stream()
                    .filter(item -> tlrId.equals(item.identifier()) && tlrAgency.equals(item.agencyId()))
                    .findFirst()
                    .orElse(ddi3Response.items().get(0));

            typeOfObject = topLevelReference.typeOfObject();
        } else {
            // Fallback behavior: find first PhysicalInstance or use first item
            topLevelItem = ddi3Response.items().stream()
                    .filter(item -> "a51e85bb-6259-4488-8df2-f08cb43485f8".equals(item.itemType()))
                    .findFirst()
                    .orElse(ddi3Response.items().get(0));

            // Determine TypeOfObject from itemType
            typeOfObject = getTypeOfObjectFromItemType(topLevelItem.itemType());
        }

        xml.append("  <ddi:TopLevelReference>\n");
        xml.append("    <r:Agency>").append(escapeXml(topLevelItem.agencyId())).append("</r:Agency>\n");
        xml.append("    <r:ID>").append(escapeXml(topLevelItem.identifier())).append("</r:ID>\n");
        xml.append("    <r:Version>").append(escapeXml(topLevelItem.version())).append("</r:Version>\n");
        xml.append("    <r:TypeOfObject>").append(escapeXml(typeOfObject)).append("</r:TypeOfObject>\n");
        xml.append("  </ddi:TopLevelReference>\n");

        // Write all fragments
        // Each item.item() already contains a complete <Fragment>...</Fragment> element
        // We prepend "ddi:" to Fragment and adjust the namespaces
        for (Ddi3Response.Ddi3Item item : ddi3Response.items()) {
            if (item.item() != null && !item.item().isEmpty()) {
                // The item.item() contains <Fragment>...</Fragment>
                // We need to convert it to <ddi:Fragment>...</ddi:Fragment>
                String fragment = item.item();
                fragment = fragment.replace("<Fragment", "<ddi:Fragment");
                fragment = fragment.replace("</Fragment>", "</ddi:Fragment>");
                xml.append("  ").append(fragment).append("\n");
            }
        }

        // Close FragmentInstance
        xml.append("</ddi:FragmentInstance>");

        return xml.toString();
    }

    /**
     * Maps DDI 3.3 Item Type UUID to TypeOfObject string
     */
    private String getTypeOfObjectFromItemType(String itemType) {
        return switch (itemType) {
            case "a51e85bb-6259-4488-8df2-f08cb43485f8" -> "PhysicalInstance";
            case "f39ff278-8500-45fe-a850-3906da2d242b" -> "DataRelationship";
            case "683889c6-f74b-4d5e-92ed-908c0a42bb2d" -> "Variable";
            case "8b108ef8-b642-4484-9c49-f88e4bf7cf1d" -> "CodeList";
            case "7e47c269-bcab-40f7-a778-af7bbc4e3d00" -> "Category";
            default -> "PhysicalInstance"; // Default fallback
        };
    }

    /**
     * Escapes special XML characters
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}