package fr.insee.rmes.domain.services.ddi;

import fr.insee.rmes.domain.model.ddi.*;

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

    private final XMLOutputFactory xmlOutputFactory;

    public Ddi3XmlWriter() {
        this.xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    public String buildPhysicalInstanceXml(Ddi4PhysicalInstance pi) throws XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        // Start Fragment
        writer.writeStartElement("Fragment");
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        // Start PhysicalInstance
        writer.writeStartElement("PhysicalInstance");
        writer.writeDefaultNamespace(DDI_PHYSICAL_INSTANCE_NS);
        writer.writeAttribute("isUniversallyUnique", pi.isUniversallyUnique());
        writer.writeAttribute("versionDate", pi.versionDate());

        // Write basic elements
        writeElement(writer, DDI_REUSABLE_NS, "URN", pi.urn());
        writeElement(writer, DDI_REUSABLE_NS, "Agency", pi.agency());
        writeElement(writer, DDI_REUSABLE_NS, "ID", pi.id());
        writeElement(writer, DDI_REUSABLE_NS, "Version", pi.version());

        // Write Citation if present
        if (pi.citation() != null && pi.citation().title() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Citation");
            writer.writeStartElement(DDI_REUSABLE_NS, "Title");
            writer.writeStartElement(DDI_REUSABLE_NS, "String");
            writer.writeAttribute("xml:lang", pi.citation().title().string().xmlLang());
            writer.writeCharacters(pi.citation().title().string().text());
            writer.writeEndElement(); // String
            writer.writeEndElement(); // Title
            writer.writeEndElement(); // Citation
        }

        // Write DataRelationshipReference if present
        if (pi.dataRelationshipReference() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "DataRelationshipReference");
            writeElement(writer, DDI_REUSABLE_NS, "Agency", pi.dataRelationshipReference().agency());
            writeElement(writer, DDI_REUSABLE_NS, "ID", pi.dataRelationshipReference().id());
            writeElement(writer, DDI_REUSABLE_NS, "Version", pi.dataRelationshipReference().version());
            writeElement(writer, DDI_REUSABLE_NS, "TypeOfObject", pi.dataRelationshipReference().typeOfObject());
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
        writer.writeStartElement("Fragment");
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        // Start DataRelationship
        writer.writeStartElement("DataRelationship");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute("isUniversallyUnique", dr.isUniversallyUnique());
        writer.writeAttribute("versionDate", dr.versionDate());

        writeElement(writer, DDI_REUSABLE_NS, "URN", dr.urn());
        writeElement(writer, DDI_REUSABLE_NS, "Agency", dr.agency());
        writeElement(writer, DDI_REUSABLE_NS, "ID", dr.id());
        writeElement(writer, DDI_REUSABLE_NS, "Version", dr.version());

        // Write DataRelationshipName if present
        if (dr.dataRelationshipName() != null) {
            writer.writeStartElement("DataRelationshipName");
            writer.writeStartElement(DDI_REUSABLE_NS, "String");
            writer.writeAttribute("xml:lang", dr.dataRelationshipName().string().xmlLang());
            writer.writeCharacters(dr.dataRelationshipName().string().text());
            writer.writeEndElement(); // String
            writer.writeEndElement(); // DataRelationshipName
        }

        // Write LogicalRecord if present
        if (dr.logicalRecord() != null) {
            LogicalRecord lr = dr.logicalRecord();
            writer.writeStartElement("LogicalRecord");
            writer.writeAttribute("isUniversallyUnique", lr.isUniversallyUnique());

            writeElement(writer, DDI_REUSABLE_NS, "URN", lr.urn());
            writeElement(writer, DDI_REUSABLE_NS, "Agency", lr.agency());
            writeElement(writer, DDI_REUSABLE_NS, "ID", lr.id());
            writeElement(writer, DDI_REUSABLE_NS, "Version", lr.version());

            if (lr.logicalRecordName() != null) {
                writer.writeStartElement("LogicalRecordName");
                writer.writeStartElement(DDI_REUSABLE_NS, "String");
                writer.writeAttribute("xml:lang", lr.logicalRecordName().string().xmlLang());
                writer.writeCharacters(lr.logicalRecordName().string().text());
                writer.writeEndElement(); // String
                writer.writeEndElement(); // LogicalRecordName
            }

            if (lr.variablesInRecord() != null && lr.variablesInRecord().variableUsedReference() != null) {
                writer.writeStartElement("VariablesInRecord");
                for (VariableUsedReference ref : lr.variablesInRecord().variableUsedReference()) {
                    writer.writeStartElement("VariableUsedReference");
                    writeElement(writer, DDI_REUSABLE_NS, "Agency", ref.agency());
                    writeElement(writer, DDI_REUSABLE_NS, "ID", ref.id());
                    writeElement(writer, DDI_REUSABLE_NS, "Version", ref.version());
                    writeElement(writer, DDI_REUSABLE_NS, "TypeOfObject", ref.typeOfObject());
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

        writer.writeStartElement("Fragment");
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("Variable");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute("isUniversallyUnique", var.isUniversallyUnique());
        writer.writeAttribute("versionDate", var.versionDate());

        writeElement(writer, DDI_REUSABLE_NS, "URN", var.urn());
        writeElement(writer, DDI_REUSABLE_NS, "Agency", var.agency());
        writeElement(writer, DDI_REUSABLE_NS, "ID", var.id());
        writeElement(writer, DDI_REUSABLE_NS, "Version", var.version());

        if (var.variableName() != null) {
            writer.writeStartElement("VariableName");
            writer.writeStartElement(DDI_REUSABLE_NS, "String");
            writer.writeAttribute("xml:lang", var.variableName().string().xmlLang());
            writer.writeCharacters(var.variableName().string().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (var.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Label");
            writer.writeStartElement(DDI_REUSABLE_NS, "Content");
            writer.writeAttribute("xml:lang", var.label().content().xmlLang());
            writer.writeCharacters(var.label().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (var.description() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Description");
            writer.writeStartElement(DDI_REUSABLE_NS, "Content");
            writer.writeAttribute("xml:lang", var.description().content().xmlLang());
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
                    writeElement(writer, DDI_REUSABLE_NS, "Agency", codeRep.codeListReference().agency());
                    writeElement(writer, DDI_REUSABLE_NS, "ID", codeRep.codeListReference().id());
                    writeElement(writer, DDI_REUSABLE_NS, "Version", codeRep.codeListReference().version());
                    writeElement(writer, DDI_REUSABLE_NS, "TypeOfObject", codeRep.codeListReference().typeOfObject());
                    writer.writeEndElement(); // CodeListReference
                }

                writer.writeEndElement(); // CodeRepresentation
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

        writer.writeStartElement("Fragment");
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("CodeList");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute("isUniversallyUnique", cl.isUniversallyUnique());
        writer.writeAttribute("versionDate", cl.versionDate());

        writeElement(writer, DDI_REUSABLE_NS, "URN", cl.urn());
        writeElement(writer, DDI_REUSABLE_NS, "Agency", cl.agency());
        writeElement(writer, DDI_REUSABLE_NS, "ID", cl.id());
        writeElement(writer, DDI_REUSABLE_NS, "Version", cl.version());

        if (cl.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Label");
            writer.writeStartElement(DDI_REUSABLE_NS, "Content");
            writer.writeAttribute("xml:lang", cl.label().content().xmlLang());
            writer.writeCharacters(cl.label().content().text());
            writer.writeEndElement();
            writer.writeEndElement();
        }

        if (cl.code() != null && !cl.code().isEmpty()) {
            for (Code code : cl.code()) {
                writer.writeStartElement("Code");
                writer.writeAttribute("isUniversallyUnique", code.isUniversallyUnique());

                writeElement(writer, DDI_REUSABLE_NS, "URN", code.urn());
                writeElement(writer, DDI_REUSABLE_NS, "Agency", code.agency());
                writeElement(writer, DDI_REUSABLE_NS, "ID", code.id());
                writeElement(writer, DDI_REUSABLE_NS, "Version", code.version());

                if (code.categoryReference() != null) {
                    writer.writeStartElement(DDI_REUSABLE_NS, "CategoryReference");
                    writeElement(writer, DDI_REUSABLE_NS, "Agency", code.categoryReference().agency());
                    writeElement(writer, DDI_REUSABLE_NS, "ID", code.categoryReference().id());
                    writeElement(writer, DDI_REUSABLE_NS, "Version", code.categoryReference().version());
                    writeElement(writer, DDI_REUSABLE_NS, "TypeOfObject", code.categoryReference().typeOfObject());
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

        writer.writeStartElement("Fragment");
        writer.writeDefaultNamespace(DDI_INSTANCE_NS);
        writer.writeNamespace("r", DDI_REUSABLE_NS);

        writer.writeStartElement("Category");
        writer.writeDefaultNamespace(DDI_LOGICAL_PRODUCT_NS);
        writer.writeAttribute("isUniversallyUnique", cat.isUniversallyUnique());
        writer.writeAttribute("versionDate", cat.versionDate());
        writer.writeAttribute("isMissing", "false");

        writeElement(writer, DDI_REUSABLE_NS, "URN", cat.urn());
        writeElement(writer, DDI_REUSABLE_NS, "Agency", cat.agency());
        writeElement(writer, DDI_REUSABLE_NS, "ID", cat.id());
        writeElement(writer, DDI_REUSABLE_NS, "Version", cat.version());

        if (cat.label() != null) {
            writer.writeStartElement(DDI_REUSABLE_NS, "Label");
            writer.writeStartElement(DDI_REUSABLE_NS, "Content");
            writer.writeAttribute("xml:lang", cat.label().content().xmlLang());
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
}